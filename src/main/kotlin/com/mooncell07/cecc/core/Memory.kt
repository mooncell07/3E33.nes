package com.mooncell07.cecc.core

class RAM : Device() {
    override val type = DT.RAM
    override val size = 0x1FFF
    override val base = 0x0000
    override val absl = 0x0800
    override val area: UByteArray = UByteArray(absl) { 0u }
}

class CHRROM(
    cart: Cartridge,
) : Device() {
    private val ref = 0x4010

    override val type = DT.CHRROM
    override val size = 0x1FFF
    override val base = 0x0000
    override val absl = size + 1
    override val area: UByteArray = cart.area.slice(ref..(ref + size)).toUByteArray()
}

class VRAM : Device() {
    override val type = DT.VRAM
    override val size = 0x0FFF
    override val base = 0x2000
    override val absl = size + 1
    override val area: UByteArray = UByteArray(absl) { 0u }
}

class PaletteRAM : Device() {
    override val type = DT.PALETTERAM
    override val size = 0x0020
    override val base = 0x3F00
    override val absl = size + 1
    override val area: UByteArray = UByteArray(absl) { 0u }
}
