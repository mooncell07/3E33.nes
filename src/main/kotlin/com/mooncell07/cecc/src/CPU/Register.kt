package com.mooncell07.cecc.src.CPU

import com.mooncell07.cecc.src.FT
import com.mooncell07.cecc.src.RT
import com.mooncell07.cecc.src.handleBit
import com.mooncell07.cecc.src.testBit

object Register {
    var PC: UShort = 0x0000u

    // First value is unused.
    private val regs: Array<UByte> = arrayOf(0x00u, 0x02u, 0xFFu, 0x02u, 0xFAu, 0x05u)

    operator fun get(regType: RT): UByte = regs[regType.ordinal]

    operator fun set(
        regType: RT,
        data: UByte,
    ) {
        regs[regType.ordinal] = data
    }

    operator fun get(flagType: FT): Boolean = testBit(regs[RT.SR.ordinal].toInt(), getFlagOrdinal(flagType))

    operator fun set(
        flagType: FT,
        flagv: Boolean,
    ) {
        val idx = RT.SR.ordinal
        regs[idx] = handleBit(regs[idx].toInt(), getFlagOrdinal(flagType), flagv).toUByte()
    }

    @JvmName("kotlin-setPC")
    fun setPC(v: UShort) {
        PC = v
    }

    fun getFlagOrdinal(f: FT) = f.ordinal - 1
}
