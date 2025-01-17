package com.mooncell07.cecc.src

abstract class AbstractDevice {
    abstract val type: DeviceType
    abstract val size: Int
    abstract val base: Int

    abstract fun read(address: UShort): UByte

    abstract fun write(
        address: UShort,
        data: UByte,
    )
}

open class Device : AbstractDevice() {
    override val type = DT.EMPTY
    override val size = -1
    override val base = -1
    open val absl = -1
    open val area: UByteArray = ubyteArrayOf()

    override fun write(
        address: UShort,
        data: UByte,
    ) {
        area[(address - base.toUShort()).toInt() % absl] = data
    }

    override fun read(address: UShort): UByte = area[(address - base.toUShort()).toInt() % absl]
}
