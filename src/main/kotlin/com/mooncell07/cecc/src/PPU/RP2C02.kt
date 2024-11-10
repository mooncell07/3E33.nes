package com.mooncell07.cecc.src.PPU

import com.mooncell07.cecc.src.CHRROM
import com.mooncell07.cecc.src.OAM
import com.mooncell07.cecc.src.PPURegisters
import com.mooncell07.cecc.src.PPUState
import com.mooncell07.cecc.src.PaletteRAM
import com.mooncell07.cecc.src.VRAM
import com.mooncell07.cecc.src.clearBit
import com.mooncell07.cecc.src.setBit
import com.mooncell07.cecc.src.testBit

data class Sprite(
    val yPos: UByte,
    val tileNum: UByte,
    val attributes: UByte,
    val xPos: UByte,
)

class RP2C02(
    private val ntsc: NTSC,
    private val regs: PPURegisters,
    private val paletteRAM: PaletteRAM,
    private val oam: OAM,
    vram: VRAM,
    chrrom: CHRROM,
) {
    val fetcher: Fetcher = Fetcher(regs, vram, chrrom)
    private var state: PPUState = PPUState.PRERENDER
    private val spriteList = mutableListOf<Sprite>()

    private fun spriteEval() {
        for (i in 0..63) {
            val yPos = oam.read((i * 4).toUShort())
            val tileNum = oam.read((i * 4 + 1).toUShort())
            val attributes = oam.read((i * 4 + 2).toUShort())
            val xPos = oam.read((i * 4 + 3).toUShort())

            if ((yPos.toInt() <= fetcher.scanline) and (spriteList.size < 9)) {
                val sprite = Sprite(yPos, tileNum, attributes, xPos)
                spriteList.addLast(sprite)
            }
        }
    }

    fun tick() {
        fetcher.dots++

        if (fetcher.dots == 341) {
            fetcher.dots = 0
            fetcher.scanline++
        }

        when (state) {
            PPUState.RENDER -> {
                if (testBit(regs.PPUMASK.toInt(), 3)) {
                    fetcher.tick()

                    if ((fetcher.shiftRegister.size > 0) and (fetcher.dots < 256)) {
                        val entry = fetcher.shiftRegister.removeFirst()
                        val paletteAddress = 0x3F00 or (entry.palette shl 2) or entry.pixel
                        val colorIndex = paletteRAM.read(paletteAddress.toUShort())
                        val colorValue = PALETTE[colorIndex.toInt()]
                        val color = getColor(colorValue)
                        ntsc.drawPixel(color)
                    }

                    if (fetcher.dots == 256) {
                        spriteEval()
                    }

                    if (fetcher.dots >= 255) fetcher.hblank()
                }

                if (fetcher.scanline == 240) {
                    fetcher.frame++
                    state = PPUState.POSTRENDER
                }
            }
            PPUState.POSTRENDER -> {
                if (fetcher.scanline == 241) {
                    ntsc.render()
                    state = PPUState.VBLANK
                }
            }
            PPUState.VBLANK -> {
                if ((fetcher.scanline == 241) and (fetcher.dots == 1)) {
                    regs.nmiOccured = true
                    regs.PPUSTATUS = setBit(regs.PPUSTATUS.toInt(), 7).toUByte()
                }
                if (fetcher.scanline == 261) {
                    state = PPUState.PRERENDER
                }
            }
            PPUState.PRERENDER -> {
                if (fetcher.dots == 1) {
                    regs.nmiOccured = false
                    regs.PPUSTATUS = clearBit(regs.PPUSTATUS.toInt(), 7).toUByte()
                }

                if (fetcher.scanline == 262) {
                    state = PPUState.RENDER
                    fetcher.scanline = 0
                    return
                }

                if (testBit(regs.PPUMASK.toInt(), 3)) {
                    fetcher.tick()
                    if ((fetcher.dots >= 280) and (fetcher.dots <= 304)) {
                        regs.v = (regs.v and 0x041Fu) or (regs.t and 0x7BE0u)
                    }
                }
            }
        }
    }
}
