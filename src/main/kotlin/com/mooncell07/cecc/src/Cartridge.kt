package com.mooncell07.cecc.src

import java.io.File

class Cartridge(
    filepath: String,
) : AbstractDevice() {
    override val type = DT.CARTRIDGE
    override val size = 0x3FFF
    override val base = 0xC000
    val area: UByteArray = File(filepath).readBytes().toUByteArray()

    override fun read(address: UShort): UByte = area[(address - base.toUShort() + 0x10u).toInt()]

    override fun write(
        address: UShort,
        data: UByte,
    ) {
        area[(address - base.toUShort() + 0x10u).toInt()] = data
    }
}
