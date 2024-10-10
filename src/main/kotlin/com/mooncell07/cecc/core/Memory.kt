package com.mooncell07.cecc.core

class ZeroPage : Device() {
    override val type = DT.ZEROPAGE
    override val size = 0x00FF
    override val base = 0x0000
    override val area: UByteArray = UByteArray(size + 1) { 0u }
}

class Stack : Device() {
    override val type = DT.STACK
    override val size = 0x00FF
    override val base = 0x0100
    override val area: UByteArray = UByteArray(size + 1) { 0u }
}

class RAMEx : Device() {
    override val type = DT.RAMEx
    override val size = 0x05FF
    override val base = 0x0200
    override val area: UByteArray = UByteArray(size + 1) { 0u }
}

class CHRROM(
    cart: Cartridge,
) : Device() {
    override val type = DT.CHRROM
    override val size = 0x1FFF
    override val base = 0x0000
    private val abs = 0x8010
    override val area: UByteArray = cart.area.slice(abs..(abs + size)).toUByteArray()
}

class VRAM : Device() {
    override val type = DT.VRAM
    override val size = 0x0FFF
    override val base = 0x2000
    override val area: UByteArray = UByteArray(size + 1) { 0u }
}

class PaletteRAM : Device() {
    override val type = DT.PALETTERAM
    override val size = 0x0020
    override val base = 0x3F00
    override val area: UByteArray = UByteArray(size + 1) { 0u }
}
