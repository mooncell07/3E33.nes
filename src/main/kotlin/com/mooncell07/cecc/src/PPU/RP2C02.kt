package com.mooncell07.cecc.src.PPU

import com.mooncell07.cecc.src.CHRROM
import com.mooncell07.cecc.src.PPURegisters
import com.mooncell07.cecc.src.PPUState
import com.mooncell07.cecc.src.PaletteRAM
import com.mooncell07.cecc.src.VRAM
import com.mooncell07.cecc.src.clearBit
import com.mooncell07.cecc.src.setBit
import com.mooncell07.cecc.src.testBit

class RP2C02(
    private val NTSC: NTSC,
    private val regs: PPURegisters,
    private val paletteRAM: PaletteRAM,
    vram: VRAM,
    chrrom: CHRROM,
) {
    val fetcher: Fetcher = Fetcher(regs, vram, chrrom)
    private var state: PPUState = PPUState.PRERENDER

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
                        NTSC.drawPixel(color)
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
                    NTSC.render()
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
