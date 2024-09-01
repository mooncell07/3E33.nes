package com.mooncell07.cecc.core

class PPURegisters : AbstractDevice() {
    override val type = DT.PPUREGISTERS
    override val size = 0x0007
    override val base = 0x2000

    var PPUCTRL: UByte = 0x00u
    var PPUMASK: UByte = 0x00u
    var PPUSTATUS: UByte = 0x00u
    var OAMADDR: UByte = 0x00u
    var OAMDATA: UByte = 0x00u
    var PPUSCROLL: UByte = 0x00u
    var PPUADDR: UByte = 0x00u
    var PPUDATA: UByte = 0x00u

    override fun read(address: UShort): UByte =
        when (address.toInt() and 0xF) {
            0x0 -> PPUCTRL
            0x1 -> PPUMASK
            0x2 -> PPUSTATUS
            0x3 -> OAMADDR
            0x4 -> OAMDATA
            0x5 -> PPUSCROLL
            0x6 -> PPUADDR
            0x7 -> PPUDATA
            else -> throw IllegalAccessError("Bad PPU Register lookup: ${address.toHexString()}")
        }

    override fun write(
        address: UShort,
        data: UByte,
    ) = when (address.toInt() and 0xF) {
        0x0 -> PPUCTRL = data
        0x1 -> PPUMASK = data
        0x2 -> PPUSTATUS = data
        0x3 -> OAMADDR = data
        0x4 -> OAMDATA = data
        0x5 -> PPUSCROLL = data
        0x6 -> PPUADDR = data
        0x7 -> PPUDATA = data
        else -> throw IllegalAccessError("Bad PPU Register lookup: ${address.toHexString()}")
    }
}
