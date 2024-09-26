package com.mooncell07.cecc.core

class Bus(
    private val clock: Clock,
    private vararg val deviceMap: AbstractDevice,
) : AbstractDevice() {
    override val type = DT.BUS
    override val size = 0xFFFF
    override val base = 0x0000

    init {
        deviceMap.sortBy { it.base }
        for (device in deviceMap) {
            println("LOADED DEVICE: ${device.type.name} @ $${(device.base + device.size).toUShort().toHexString()}")
        }
    }

    override fun read(address: UShort): UByte {
        clock.tick()
        return deviceMap.find { address.toInt() <= (it.base + it.size) }!!.read(address)
    }

    override fun write(
        address: UShort,
        data: UByte,
    ) {
        if (address == 0x4014.toUShort()) {
            println("Skipping DMA")
            for (i in 0..514) {
                clock.tick()
            }
            return
        }
        clock.tick()
        deviceMap.find { address.toInt() <= (it.base + it.size) }!!.write(address, data)
    }

    fun readWord(address: UShort): UShort {
        val lo = read(address)
        val hi = read((address + 1u).toUShort())
        return concat(hi, lo)
    }

    fun dummyRead(address: UShort) = read(address)

    fun dummyWrite(
        address: UShort,
        data: UByte,
    ) = write(address, data)
}
