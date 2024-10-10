package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.*
import javafx.scene.paint.Color

class Fetcher(
    private val regs: PPURegisters,
    private val vram: VRAM,
    private val chrrom: CHRROM,
    private val paletteRAM: PaletteRAM,
) {
    val COLORS = arrayOf(Color.BLACK, Color.DARKGRAY, Color.LIGHTGRAY, Color.WHITE)

    private var state = 0
    private var address: UShort = 0x0000u
    private var ntByte: UByte = 0x00u
    private var atByte: UByte = 0x00u
    private var loPlane: UByte = 0x00u
    private var hiPlane: UByte = 0x00u

    var shiftRegister = mutableListOf<Color>()
    var scanline = 261
    var dots = 320
    var frame = 1

    private fun genPixelRow(
        loPlane: UByte,
        hiPlane: UByte,
    ): IntArray {
        val pixelRow = IntArray(8) { 0 }
        for ((i, x) in (7 downTo 0).withIndex()) {
            pixelRow[i] = (testBit(hiPlane.toInt(), x).toInt() shl 1) or testBit(loPlane.toInt(), x).toInt()
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
            0 -> {
                address = 0x2000u.toUShort() or (regs.v and 0x0FFFu)
                state = 1
            }
            1 -> {
                ntByte = vram.read(address)
                // [336, 340] are unused NT fetches, to handle this we just dont let the
                // state machine to progress further from here.
                state = if ((dots >= 336) and (dots <= 340)) 0 else 2
            }

            2 -> {
                address =
                    (
                        0x23C0
                            or (regs.v and 0x0C00u).toInt()
                            or ((regs.v.toInt() shr 4) and 0x38)
                            or ((regs.v.toInt() shr 2) and 0x07)
                    ).toUShort()
                state = 3
            }

            3 -> {
                atByte = vram.read(address)
                state = 4
            }

            4 -> {
                address =
                    (
                        ((testBit(regs.PPUCTRL.toInt(), 4).toInt()) shl 12)
                            or ((ntByte.toInt()) shl 4)
                            or ((regs.v.toInt() and 0x7000) shr 12)
                    ).toUShort()
                state = 5
            }

            5 -> {
                loPlane = chrrom.read(address)
                state = 6
            }

            6 -> {
                hiPlane = chrrom.read((address + 8u).toUShort())
                state = 7
            }

            7 -> {
                val pixelRow = genPixelRow(loPlane, hiPlane)
                for (x in pixelRow) {
                    shiftRegister.addLast(COLORS[x])
                }

                incHorizontal()

                state = 0
            }
        }
    }

    private fun incVertical() {
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

    private fun incHorizontal() {
        if ((regs.v and 0x001Fu).toInt() == 31) {
            regs.v = regs.v and 0xFFE0u
            regs.v = regs.v xor 0x0400u
        } else {
            regs.v++
        }
    }

    private fun resetV() {
        regs.v = (regs.v and 0x7BE0u) or (regs.t and 0x041Fu)
    }

    fun hblank() {
        if (dots == 255) incVertical()
        if (dots == 256) resetV()
    }
}
