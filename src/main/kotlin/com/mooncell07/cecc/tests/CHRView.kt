package com.mooncell07.cecc.tests

import com.mooncell07.cecc.src.CHRROM
import com.mooncell07.cecc.src.Cartridge
import com.mooncell07.cecc.src.PPU.NTSC
import com.mooncell07.cecc.src.testBit
import com.mooncell07.cecc.src.toInt
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage

class CHRView : Application() {
    private val ntsc = NTSC(128, 128, 3.0)
    private val cart = Cartridge("roms/nestest.nes")
    private val chrrom = CHRROM(cart)

    private val COLORS = arrayOf(Color.BLACK, Color.DARKGRAY, Color.LIGHTGRAY, Color.WHITE)

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

    private fun renderTiles() {
        for (tile in 0..15) {
            val colStart = tile * 16
            for (row in 0..7) {
                for (col in colStart..(colStart + 15)) {
                    val addr = chrrom.base + (16 * col) + row
                    val loPlane = chrrom.area[addr]
                    val hiPlane = chrrom.area[addr + 8]
                    val pixelRow = genPixelRow(loPlane, hiPlane)
                    println(
                        """
                            |TILE START: $colStart, 
                            |TILE END: ${(colStart + 15)}, 
                            |TILE COLUMN: $col, 
                            |TILE ROW: $row, 
                            |LO: (${addr.toHexString()}, ${loPlane.toHexString()}), 
                            |HI: (${(addr + 8).toHexString()}, ${hiPlane.toHexString()}), 
                            |PIXEL ROW: 
                            |${pixelRow.map{ it.toString(2) }} 
                        """.trimMargin().replace("\n", ""),
                    )
                    for (x in pixelRow) {
                        ntsc.drawPixel(COLORS[x])
                    }
                }
            }
        }
        ntsc.render()
    }

    override fun start(primaryStage: Stage) {
        val scene = Scene(Group(ntsc.canvas))
        primaryStage.title = "3E33.nes"
        primaryStage.scene = scene
        primaryStage.show()
        renderTiles()
    }
}

fun main(args: Array<String>) {
    Application.launch(CHRView::class.java, *args)
}
