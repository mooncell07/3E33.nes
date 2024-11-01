package com.mooncell07.cecc.src

class PPURegisters(
    private val vram: VRAM,
    private val paletteRAM: PaletteRAM,
) : AbstractDevice() {
    override val type = DT.PPUREGISTERS
    override val size = 0x0007
    override val base = 0x2000

    var PPUCTRL: UByte = 0x00u
    var PPUMASK: UByte = 0x00u
    var PPUSTATUS: UByte = 0x00u
    var OAMADDR: UByte = 0x00u
    var OAMDATA: UByte = 0x00u
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
                /*
                $2002 (PPUSTATUS) read
                w:                  <- 0
                 */
                PPUSTATUS = handleBit(PPUSTATUS.toInt(), 7, nmiOccured).toUByte()
                nmiOccured = false
                w = false
                PPUSTATUS
            }
            0x3 -> OAMADDR
            0x4 -> OAMDATA
            0x7 -> {
                println("PPUDATA HAS BEEN READ IMPLEMENT IT")
                PPUDATA
            }
            else -> throw IllegalAccessError("Bad PPU Register lookup: ${address.toHexString()}")
        }

    override fun write(
        address: UShort,
        data: UByte,
    ) = when ((address).toInt() and 0xF) {
        0x0 -> {
            /*
            $2000 (PPUCTRL) write
            t: ... GH.. .... .... <- d: ......GH
            <used elsewhere>      <- d: ABCDEF..
             */
            t = (t and 0xF3FFu) or ((data and 0x03u).toInt() shl 10).toUShort()
            PPUCTRL = data
            nmiOutput = testBit(PPUCTRL.toInt(), 7)
        }
        0x1 -> PPUMASK = data
        0x2 -> PPUSTATUS = data
        0x3 -> OAMADDR = data
        0x4 -> OAMDATA = data
        0x5 -> {
            /*
            $2005 (PPUSCROLL) first write (w is 0)
            t: ....... ...ABCDE <- d: ABCDE...
            x:              FGH <- d: .....FGH
            w:                  <- 1

            $2005 (PPUSCROLL) second write (w is 1)
            t: FGH..AB CDE..... <- d: ABCDEFGH
            w:                  <- 0
             */

            if (!w) {
                t = ((data and 0xF8u).toInt() shr 3).toUShort()
                x = (data and 7u)
                w = true
            } else {
                t = (((data and 7u).toInt() shl 12).toUShort() or ((data and 0xF8u).toInt() shl 2).toUShort() or t)
                w = false
            }
        }

        0x6 -> {
            /*
            $2006 (PPUADDR) first write (w is 0)
            t: .CDEFGH ........ <- d: ..CDEFGH
            <unused>            <- d: AB......
            t: Z...... ........ <- 0 (bit Z is cleared)
            w:                  <- 1

            $2006 (PPUADDR) second write (w is 1)
            t: ....... ABCDEFGH <- d: ABCDEFGH
            v: <...all bits...> <- t: <...all bits...>
            w:                  <- 0
             */

            if (!w) {
                t = (data and 0x3Fu).toUShort()
                w = true
            } else {
                t = concat(t.toUByte(), data)
                v = t
                w = false
            }
        }

        0x7 -> {
            PPUDATA = data
            // implement attr table
            if (v >= 0x3F00u) {
                paletteRAM.write(v, data)
            } else {
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
    override val absl = size + 1
    override val area = UByteArray(absl) { 0u }
}
