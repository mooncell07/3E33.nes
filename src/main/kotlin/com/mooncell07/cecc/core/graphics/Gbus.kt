package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.AbstractDevice
import com.mooncell07.cecc.core.Cartridge
import com.mooncell07.cecc.core.DT
import com.mooncell07.cecc.core.Device

class CHRROM(
    cart: Cartridge,
) : Device() {
    override val type = DT.CHRROM
    override val size = 0x1FFF
    override val base = 0x4010
    override val area: UByteArray = cart.area
}

class VRAM(
    cart: Cartridge,
) : Device() {
    override val type = DT.VRAM
    override val size = 0x0FFF
    override val base = 0x600F
    override val area: UByteArray = cart.area
}

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
        if (address <= chrrom.size.toUShort()) {
            chrrom.write(address, data)
        } else if (address <= vram.size.toUShort()) {
            vram.write(address, data)
        }
    }
}
