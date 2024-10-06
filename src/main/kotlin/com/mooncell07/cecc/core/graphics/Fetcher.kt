package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.*
import javafx.scene.paint.Color

class Fetcher(
    private val ppuReg: PPURegisters,
    private val vram: VRAM,
    private val chrrom: CHRROM,
) {
    val NTBASE = ushortArrayOf(0x2000u, 0x2400u, 0x2800u, 0x2C00u)
    private val COLORS = arrayOf(Color.BLACK, Color.DARKGRAY, Color.LIGHTGRAY, Color.WHITE)

    private var state = 0
    private var nt: UByte = 0x00u
    private var lo: UByte = 0x00u
    private var hi: UByte = 0x00u
    private var baseAddr: UShort = 0x0000u
    var shiftRegister = mutableListOf<Color>()
    var scanline = 261
    var dots = 320

    private fun genPixelRow(
        lo: UByte,
        hi: UByte,
    ): IntArray {
        val pixelRow = IntArray(8) { 0 }
        for ((i, x) in (7 downTo 0).withIndex()) {
            pixelRow[i] = (testBit(hi.toInt(), x).toInt() shl 1) or testBit(lo.toInt(), x).toInt()
        }
        return pixelRow
    }

    fun tick() {
        if ((dots >= 256) and (dots <= 320)) {
            shiftRegister.clear()
            state = 0
            return
        }

        when (state) {
            0 -> state = 1
            1 -> {
                nt = vram.read(0x2000u.toUShort() or (ppuReg.v and 0x0FFFu))
                // [336, 340] are unused NT fetches, to handle this we just dont let the
                // state machine to progress further from here.
                state = if ((dots >= 336) and (dots <= 340)) 0 else 2
            }

            2 -> state = 3

            3 -> {
                // AT
                state = 4
            }

            4 -> {
                baseAddr = (((nt.toInt()) shl 4) or (ppuReg.v.toInt() shr 12)).toUShort()
                lo = chrrom.area[baseAddr.toInt()]
                state = 5
            }

            5 -> state = 6

            6 -> {
                hi = chrrom.area[(baseAddr + 8u).toInt()]
                state = 7
            }

            7 -> {
                if (shiftRegister.size > 8) {
                    return
                }

                val pixelRow = genPixelRow(lo, hi)
                for (x in pixelRow) {
                    shiftRegister.addLast(COLORS[x])
                }

                if ((ppuReg.v and 0x001Fu).toInt() == 31) {
                    ppuReg.v = ppuReg.v and 0xFFE0u
                    ppuReg.v = ppuReg.v xor 0x0400u
                } else {
                    ppuReg.v++
                }
                state = 0
            }
        }
    }
}
