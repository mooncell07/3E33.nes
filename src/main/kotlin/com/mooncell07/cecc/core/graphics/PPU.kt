package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.PPURegisters
import com.mooncell07.cecc.core.PPUState

class PPU(
    private val gbus: GBUS,
    private val regs: PPURegisters,
) {
    private var scanline = 0
    private var state: PPUState = PPUState.PRERENDER
    private val fetcher: Fetcher = Fetcher(regs)

    fun tick() {
        fetcher.dots++
        if (fetcher.dots == 341) {
            scanline++
            fetcher.dots = 0
        }

        when (state) {
            PPUState.RENDER -> {
                if (scanline == 240) {
                    state = PPUState.POSTRENDER
                    return
                }

                fetcher.tick()
            }
            PPUState.POSTRENDER -> {
                if (scanline == 241) state = PPUState.VBLANK
            }
            PPUState.VBLANK -> {
                if (scanline == 261) state = PPUState.PRERENDER
            }
            PPUState.PRERENDER -> {
                if (scanline == 262) {
                    state = PPUState.RENDER
                    scanline = 0
                }
            }
        }
    }
}
