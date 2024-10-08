package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.*

class PPU(
    private val screen: Screen,
    private val regs: PPURegisters,
    vram: VRAM,
    chrrom: CHRROM,
) {
    private var state: PPUState = PPUState.PRERENDER
    val fetcher: Fetcher = Fetcher(regs, vram, chrrom)

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
                    if (fetcher.dots == 255) {
                        if (regs.v.toInt() and 0x7000 != 0x7000) {
                            regs.v = (regs.v + 0x1000u).toUShort()
                        } else {
                            regs.v = regs.v and 0x8FFFu
                            var y = (regs.v and 0x03E0u).toInt() shr 5
                            when (y) {
                                29 -> {
                                    y = 0
                                    regs.v = regs.v xor 0x0800u
                                }
                                31 -> y = 0
                                else -> y++
                            }
                            regs.v = (regs.v and 0xFC1Fu) or (y shl 5).toUShort()
                        }
                    }
                    if (fetcher.dots == 256) {
                        regs.v = (regs.v and 0x7BE0u) or (regs.t and 0x041Fu)
                    }

                    if ((fetcher.shiftRegister.size > 0) and (fetcher.dots < 256)) {
                        screen.drawPixel(fetcher.shiftRegister.removeFirst())
                    }
                }

                if (fetcher.scanline == 240) {
                    fetcher.frame++
                    state = PPUState.POSTRENDER
                }
            }
            PPUState.POSTRENDER -> {
                if (fetcher.scanline == 241) {
                    screen.render()
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
