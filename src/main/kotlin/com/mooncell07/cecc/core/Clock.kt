package com.mooncell07.cecc.core

import com.mooncell07.cecc.core.graphics.PPU

class Clock(
    private val ppu: PPU? = null,
) {
    var cycles: Int = 0

    fun tick() {
        cycles++
        ppu?.tick()
        ppu?.tick()
        ppu?.tick()
    }
}
