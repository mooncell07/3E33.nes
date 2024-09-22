package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.AbstractDevice
import com.mooncell07.cecc.core.CHRROM
import com.mooncell07.cecc.core.DT
import com.mooncell07.cecc.core.VRAM

class GBUS(
    private val chrrom: CHRROM,
    private val vram: VRAM,
) : AbstractDevice() {
    override val type = DT.GBUS
    override val size = 0x3FFF
    override val base = 0x0000

    override fun read(address: UShort): UByte {
        if (address <= (chrrom.size + chrrom.base).toUShort()) {
            return chrrom.read(address)
        }

        if (address <= (vram.size + vram.base).toUShort()) {
            return vram.read(address)
        }

        return 0xFFu
    }

    override fun write(
        address: UShort,
        data: UByte,
    ) {
        if (address <= (chrrom.size + chrrom.base).toUShort()) {
            chrrom.write(address, data)
        } else if (address <= (vram.size + vram.base).toUShort()) {
            vram.write(address, data)
        }
    }
}
