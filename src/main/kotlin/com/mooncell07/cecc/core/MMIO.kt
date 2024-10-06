package com.mooncell07.cecc.core

class PPURegisters(
    private val vram: VRAM,
) : AbstractDevice() {
    override val type = DT.PPUREGISTERS
    override val size = 0x0007
    override val base = 0x2000

    var PPUCTRL: UByte = 0x00u
    var PPUMASK: UByte = 0x00u
    var PPUSTATUS: UByte = 0x00u
    var OAMADDR: UByte = 0x00u
    var OAMDATA: UByte = 0x00u
    var PPUSCROLL: UByte = 0x00u
    var PPUDATA: UByte = 0x00u

    // Internal Registers

    // PPUSTATUS[7] = nmi occured
    var nmiOccured: Boolean = false

    // PPUCTRL[7] = nmi output
    var nmiOutput: Boolean = false

    // During rendering, used for the scroll position. Outside of rendering, used as the current
    // VRAM address.
    var v: UShort = 0x2000u

    // During rendering, specifies the starting coarse-x scroll for the next scanline and the
    // starting y scroll for the screen. Outside of rendering, holds the scroll or VRAM address
    // before transferring it to v.
    var t: UShort = 0x0000u

    // The fine-x position of the current scroll, used during rendering alongside v.
    var x: UByte = 0x00u

    // Toggles on each write to either PPUSCROLL or PPUADDR, indicating whether this is the first
    // or second write. Clears on reads of PPUSTATUS. Sometimes called the 'write latch' or 'write toggle'.
    var w: Boolean = false

    override fun read(address: UShort): UByte =
        when (address.toInt() and 0xF) {
            0x0 -> PPUCTRL
            0x1 -> PPUMASK
            0x2 -> {
                PPUSTATUS = handleBit(PPUSTATUS.toInt(), 7, nmiOccured).toUByte()
                nmiOccured = false
                w = false
                PPUSTATUS
            }
            0x3 -> OAMADDR
            0x4 -> OAMDATA
            0x5 -> PPUSCROLL
            0x7 -> {
                println("PPUDATA HAS BEEN READ IMPLEMENT IT")
                PPUDATA
            }
            else -> throw IllegalAccessError("Bad PPU Register lookup: ${address.toHexString()}")
        }

    override fun write(
        address: UShort,
        data: UByte,
    ) = when (address.toInt() and 0xF) {
        0x0 -> {
            PPUCTRL = data
            nmiOutput = testBit(PPUCTRL.toInt(), 7)
        }
        0x1 -> PPUMASK = data
        0x2 -> PPUSTATUS = data
        0x3 -> OAMADDR = data
        0x4 -> OAMDATA = data
        0x5 -> PPUSCROLL = data
        0x6 -> {
            if (w) {
                t = concat(t.toUByte(), data)
                v = t
                w = false
            } else {
                t = data.toUShort()
                w = true
            }
        }
        0x7 -> {
            PPUDATA = data
            // implement attr table
            if (v < 0x3000u) {
                vram.write(v, data)
            }

            v =
                if (!testBit(PPUCTRL.toInt(), 2)) {
                    // going across
                    (v + 1u).toUShort()
                } else {
                    // going down
                    (v + 32u).toUShort()
                }
        }
        else -> throw IllegalAccessError("Bad PPU Register lookup: ${address.toHexString()}")
    }
}

class APURegisters : Device() {
    override val type = DT.APUREGISTERS
    override val size = 0x0017
    override val base = 0x4000

    override val area = UByteArray(size + 1) { 0u }
}
