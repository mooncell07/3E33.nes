package com.mooncell07.cecc.src

class DMA(
    private val bus: Bus,
    private val oam: OAM,
) {
    var currentByte: UByte = 0x00u
    var currentIndex: UShort = 0x0000u

    fun run(hi: UByte) {
        for (lo in 0..255) {
            currentIndex = concat(hi, lo.toUByte())
            currentByte = bus.read(currentIndex)
            oam.write(lo.toUShort(), currentByte)
            bus.clock.tick()
            bus.clock.tick()
        }
    }
}
