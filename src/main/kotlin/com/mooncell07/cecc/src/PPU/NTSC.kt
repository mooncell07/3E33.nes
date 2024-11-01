package com.mooncell07.cecc.src.PPU

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelWriter
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color

class NTSC(
    private val width: Int = 256,
    height: Int = 240,
    scale: Double = 2.0,
) {
    val canvas: Canvas = Canvas(width * scale, height * scale)
    private val graphicsContext: GraphicsContext = canvas.graphicsContext2D
    private val buffer: WritableImage = WritableImage(width, height)
    private val pixelWriter: PixelWriter = buffer.pixelWriter
    private var bufferIndex = 0

    init {
        graphicsContext.isImageSmoothing = false
        graphicsContext.scale(scale, scale)
    }

    fun drawPixel(color: Color) {
        pixelWriter.setColor(bufferIndex % width, bufferIndex / width, color)
        bufferIndex += 1
    }

    fun render() {
        graphicsContext.drawImage(buffer, 0.0, 0.0)
        bufferIndex = 0
    }
}
