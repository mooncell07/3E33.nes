package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.PPURegisters
import com.mooncell07.cecc.core.PPUState
import com.mooncell07.cecc.core.clearBit
import com.mooncell07.cecc.core.setBit
import com.mooncell07.cecc.core.testBit

class PPU(
    private val gbus: GBUS,
    private val regs: PPURegisters,
    private val vram: VRAM
) {
    var scanline = 262
    var state: PPUState = PPUState.PRERENDER
    val fetcher: Fetcher = Fetcher(regs)
    var frame = 0
    fun tick() {
        fetcher.dots++
        if (fetcher.dots == 341) {
            scanline++
            fetcher.dots = 0
        }

        when (state) {
            PPUState.RENDER -> {
                fetcher.tick()
                if (scanline == 240) {
                    frame++
                    state = PPUState.POSTRENDER
                }
            }
            PPUState.POSTRENDER -> {
                if (scanline == 241) {
                    state = PPUState.VBLANK
                }
            }
            PPUState.VBLANK -> {
                if ((scanline == 241) and (fetcher.dots == 1)){
                    regs.nmiOccured = true
                    regs.PPUSTATUS = setBit(regs.PPUSTATUS.toInt(), 7).toUByte()
                }
                if (scanline == 261) {
                    state = PPUState.PRERENDER
                }
            }
            PPUState.PRERENDER -> {
                if (fetcher.dots == 1){
                    regs.nmiOccured = false
                    regs.PPUSTATUS = clearBit(regs.PPUSTATUS.toInt(), 7).toUByte()
                }

                if (scanline == 262) {
                    state = PPUState.RENDER
                    scanline = 0
                }
            }
        }
    }
}
