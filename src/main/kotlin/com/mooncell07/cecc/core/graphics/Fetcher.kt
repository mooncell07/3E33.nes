package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.*
import javafx.scene.paint.Color

class Fetcher(
    private val ppuReg: PPURegisters,
    private val vram: VRAM,
    private val chrrom: CHRROM,
    private val paletteRAM: PaletteRAM,
) {
    val NTBASE = ushortArrayOf(0x2000u, 0x2400u, 0x2800u, 0x2C00u)
    val COLORS = arrayOf(Color.BLACK, Color.DARKGRAY, Color.LIGHTGRAY, Color.WHITE)

    private var state = 0
    private var nt: UByte = 0x00u
    private var lo: UByte = 0x00u
    private var hi: UByte = 0x00u
    private var baseAddr: UShort = 0x0000u
    private var attr: UByte = 0x00u

    var shiftRegister = mutableListOf<Color>()
    var scanline = 261
    var dots = 320
    var frame = 1

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
                val attrAddr =
                    0x23C0 or (ppuReg.v and 0x0C00u).toInt() or ((ppuReg.v.toInt() shr 4) and 0x38) or ((ppuReg.v.toInt() shr 2) and 0x07)
                attr = vram.read(attrAddr.toUShort())
                state = 4
            }

            4 -> {
                val patternTable = testBit(ppuReg.PPUCTRL.toInt(), 4).toInt()
                baseAddr =
                    (
                        (patternTable shl 12)
                            or ((nt.toInt()) shl 4)
                            or ((ppuReg.v.toInt() and 0x7000) shr 12)
                    ).toUShort()
                lo = chrrom.read(baseAddr)
                state = 5
            }

            5 -> state = 6

            6 -> {
                hi = chrrom.read((baseAddr + 8u).toUShort())
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
