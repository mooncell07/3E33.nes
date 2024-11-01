package com.mooncell07.cecc.src

import com.mooncell07.cecc.src.PPU.RP2C02

class Clock(
    private val rp2c02: RP2C02? = null,
) {
    var cycles: Int = 6

    fun tick() {
        cycles++
        rp2c02?.tick()
        rp2c02?.tick()
        rp2c02?.tick()
    }
}
