package com.mooncell07.cecc.core

data class INSTR(
    val insType: IT,
    val addrMode: AM,
    val regType: RT,
    val flagType: FT,
)

val INSTAB: Array<INSTR> = Array(256) { INSTR(IT.NONE, AM.NONE, RT.NONE, FT.NONE) }

fun buildInstructionTable() {
    INSTAB[0x00] = INSTR(IT.BRK, AM.IMPLIED, RT.NONE, FT.NONE)
    INSTAB[0x01] = INSTR(IT.ORA, AM.X_INDIRECT, RT.A, FT.NONE)
    INSTAB[0x05] = INSTR(IT.ORA, AM.ZEROPAGE, RT.A, FT.NONE)
    INSTAB[0x06] = INSTR(IT.ASL, AM.ZEROPAGE, RT.NONE, FT.C)
    INSTAB[0x08] = INSTR(IT.PUSH, AM.IMPLIED, RT.SR, FT.NONE)
    INSTAB[0x09] = INSTR(IT.ORA, AM.IMMEDIATE, RT.A, FT.NONE)
    INSTAB[0x0A] = INSTR(IT.ASL, AM.ACCUMULATOR, RT.NONE, FT.C)
    INSTAB[0x0D] = INSTR(IT.ORA, AM.ABSOLUTE, RT.A, FT.NONE)
    INSTAB[0x0E] = INSTR(IT.ASL, AM.ABSOLUTE, RT.NONE, FT.C)

    INSTAB[0x10] = INSTR(IT.BRCLR, AM.RELATIVE, RT.NONE, FT.N)
    INSTAB[0x11] = INSTR(IT.ORA, AM.INDIRECT_Y, RT.A, FT.NONE)
    INSTAB[0x15] = INSTR(IT.ORA, AM.ZEROPAGE_X, RT.A, FT.NONE)
    INSTAB[0x16] = INSTR(IT.ASL, AM.ZEROPAGE_X, RT.NONE, FT.C)
    INSTAB[0x18] = INSTR(IT.CLEAR, AM.IMPLIED, RT.NONE, FT.C)
    INSTAB[0x19] = INSTR(IT.ORA, AM.ABSOLUTE_Y, RT.A, FT.NONE)
    INSTAB[0x1D] = INSTR(IT.ORA, AM.ABSOLUTE_X, RT.A, FT.NONE)
    INSTAB[0x1E] = INSTR(IT.ASL, AM.ABSOLUTE_X, RT.NONE, FT.C)

    INSTAB[0x20] = INSTR(IT.JSR, AM.ABSOLUTE, RT.NONE, FT.NONE)
    INSTAB[0x21] = INSTR(IT.AND, AM.X_INDIRECT, RT.A, FT.NONE)
    INSTAB[0x24] = INSTR(IT.BIT, AM.ZEROPAGE, RT.SR, FT.NONE)
    INSTAB[0x25] = INSTR(IT.AND, AM.ZEROPAGE, RT.A, FT.NONE)
    INSTAB[0x26] = INSTR(IT.ROL, AM.ZEROPAGE, RT.NONE, FT.C)
    INSTAB[0x28] = INSTR(IT.PULL, AM.IMPLIED, RT.SR, FT.NONE)
    INSTAB[0x29] = INSTR(IT.AND, AM.IMMEDIATE, RT.A, FT.NONE)
    INSTAB[0x2A] = INSTR(IT.ROL, AM.ACCUMULATOR, RT.NONE, FT.C)
    INSTAB[0x2C] = INSTR(IT.BIT, AM.ABSOLUTE, RT.SR, FT.NONE)
    INSTAB[0x2D] = INSTR(IT.AND, AM.ABSOLUTE, RT.A, FT.NONE)
    INSTAB[0x2E] = INSTR(IT.ROL, AM.ABSOLUTE, RT.NONE, FT.C)

    INSTAB[0x30] = INSTR(IT.BRSET, AM.RELATIVE, RT.NONE, FT.N)
    INSTAB[0x31] = INSTR(IT.AND, AM.INDIRECT_Y, RT.A, FT.NONE)
    INSTAB[0x35] = INSTR(IT.AND, AM.ZEROPAGE_X, RT.A, FT.NONE)
    INSTAB[0x36] = INSTR(IT.ROL, AM.ZEROPAGE_X, RT.NONE, FT.C)
    INSTAB[0x38] = INSTR(IT.SET, AM.IMPLIED, RT.NONE, FT.C)
    INSTAB[0x39] = INSTR(IT.AND, AM.ABSOLUTE_Y, RT.A, FT.NONE)
    INSTAB[0x3D] = INSTR(IT.AND, AM.ABSOLUTE_X, RT.A, FT.NONE)
    INSTAB[0x3E] = INSTR(IT.ROL, AM.ABSOLUTE_X, RT.NONE, FT.C)

    INSTAB[0x40] = INSTR(IT.RTI, AM.IMPLIED, RT.NONE, FT.NONE)
    INSTAB[0x41] = INSTR(IT.EOR, AM.X_INDIRECT, RT.A, FT.NONE)
    INSTAB[0x45] = INSTR(IT.EOR, AM.ZEROPAGE, RT.A, FT.NONE)
    INSTAB[0x46] = INSTR(IT.LSR, AM.ZEROPAGE, RT.NONE, FT.C)
    INSTAB[0x48] = INSTR(IT.PUSH, AM.IMPLIED, RT.A, FT.NONE)
    INSTAB[0x49] = INSTR(IT.EOR, AM.IMMEDIATE, RT.A, FT.NONE)
    INSTAB[0x4A] = INSTR(IT.LSR, AM.ACCUMULATOR, RT.NONE, FT.C)
    INSTAB[0x4C] = INSTR(IT.JMP, AM.ABSOLUTE, RT.NONE, FT.NONE)
    INSTAB[0x4D] = INSTR(IT.EOR, AM.ABSOLUTE, RT.A, FT.NONE)
    INSTAB[0x4E] = INSTR(IT.LSR, AM.ABSOLUTE, RT.NONE, FT.C)

    INSTAB[0x50] = INSTR(IT.BRCLR, AM.RELATIVE, RT.NONE, FT.V)
    INSTAB[0x51] = INSTR(IT.EOR, AM.INDIRECT_Y, RT.A, FT.NONE)
    INSTAB[0x55] = INSTR(IT.EOR, AM.ZEROPAGE_X, RT.A, FT.NONE)
    INSTAB[0x56] = INSTR(IT.LSR, AM.ZEROPAGE_X, RT.NONE, FT.C)
    INSTAB[0x58] = INSTR(IT.CLEAR, AM.IMPLIED, RT.NONE, FT.I)
    INSTAB[0x59] = INSTR(IT.EOR, AM.ABSOLUTE_Y, RT.A, FT.NONE)
    INSTAB[0x5D] = INSTR(IT.EOR, AM.ABSOLUTE_X, RT.A, FT.NONE)
    INSTAB[0x5E] = INSTR(IT.LSR, AM.ABSOLUTE_X, RT.NONE, FT.C)

    INSTAB[0x60] = INSTR(IT.RTS, AM.IMPLIED, RT.NONE, FT.NONE)
    INSTAB[0x61] = INSTR(IT.ADC, AM.X_INDIRECT, RT.A, FT.C)
    INSTAB[0x65] = INSTR(IT.ADC, AM.ZEROPAGE, RT.A, FT.C)
    INSTAB[0x66] = INSTR(IT.ROR, AM.ZEROPAGE, RT.NONE, FT.C)
    INSTAB[0x68] = INSTR(IT.PULL, AM.IMPLIED, RT.A, FT.NONE)
    INSTAB[0x69] = INSTR(IT.ADC, AM.IMMEDIATE, RT.A, FT.C)
    INSTAB[0x6A] = INSTR(IT.ROR, AM.ACCUMULATOR, RT.NONE, FT.C)
    INSTAB[0x6C] = INSTR(IT.JMP, AM.INDIRECT, RT.NONE, FT.NONE)
    INSTAB[0x6D] = INSTR(IT.ADC, AM.ABSOLUTE, RT.A, FT.C)
    INSTAB[0x6E] = INSTR(IT.ROR, AM.ABSOLUTE, RT.NONE, FT.C)

    INSTAB[0x70] = INSTR(IT.BRSET, AM.RELATIVE, RT.NONE, FT.V)
    INSTAB[0x71] = INSTR(IT.ADC, AM.INDIRECT_Y, RT.A, FT.C)
    INSTAB[0x75] = INSTR(IT.ADC, AM.ZEROPAGE_X, RT.A, FT.C)
    INSTAB[0x76] = INSTR(IT.ROR, AM.ZEROPAGE_X, RT.NONE, FT.C)
    INSTAB[0x78] = INSTR(IT.SET, AM.IMPLIED, RT.NONE, FT.I)
    INSTAB[0x79] = INSTR(IT.ADC, AM.ABSOLUTE_Y, RT.A, FT.C)
    INSTAB[0x7D] = INSTR(IT.ADC, AM.ABSOLUTE_X, RT.A, FT.C)
    INSTAB[0x7E] = INSTR(IT.ROR, AM.ABSOLUTE_X, RT.NONE, FT.C)

    INSTAB[0x81] = INSTR(IT.STORE, AM.X_INDIRECT, RT.A, FT.NONE)
    INSTAB[0x84] = INSTR(IT.STORE, AM.ZEROPAGE, RT.Y, FT.NONE)
    INSTAB[0x85] = INSTR(IT.STORE, AM.ZEROPAGE, RT.A, FT.NONE)
    INSTAB[0x86] = INSTR(IT.STORE, AM.ZEROPAGE, RT.X, FT.NONE)
    INSTAB[0x88] = INSTR(IT.DECREMENT, AM.IMPLIED, RT.Y, FT.NONE)
    INSTAB[0x8A] = INSTR(IT.TXA, AM.IMPLIED, RT.A, FT.NONE)
    INSTAB[0x8C] = INSTR(IT.STORE, AM.ABSOLUTE, RT.Y, FT.NONE)
    INSTAB[0x8D] = INSTR(IT.STORE, AM.ABSOLUTE, RT.A, FT.NONE)
    INSTAB[0x8E] = INSTR(IT.STORE, AM.ABSOLUTE, RT.X, FT.NONE)

    INSTAB[0x90] = INSTR(IT.BRCLR, AM.RELATIVE, RT.NONE, FT.C)
    INSTAB[0x91] = INSTR(IT.STORE, AM.INDIRECT_Y, RT.A, FT.NONE)
    INSTAB[0x94] = INSTR(IT.STORE, AM.ZEROPAGE_X, RT.Y, FT.NONE)
    INSTAB[0x95] = INSTR(IT.STORE, AM.ZEROPAGE_X, RT.A, FT.NONE)
    INSTAB[0x96] = INSTR(IT.STORE, AM.ZEROPAGE_Y, RT.X, FT.NONE)
    INSTAB[0x98] = INSTR(IT.TYA, AM.IMPLIED, RT.A, FT.NONE)
    INSTAB[0x99] = INSTR(IT.STORE, AM.ABSOLUTE_Y, RT.A, FT.NONE)
    INSTAB[0x9A] = INSTR(IT.TXS, AM.IMPLIED, RT.SP, FT.NONE)
    INSTAB[0x9D] = INSTR(IT.STORE, AM.ABSOLUTE_X, RT.A, FT.NONE)

    INSTAB[0xA0] = INSTR(IT.LOAD, AM.IMMEDIATE, RT.Y, FT.NONE)
    INSTAB[0xA1] = INSTR(IT.LOAD, AM.X_INDIRECT, RT.A, FT.NONE)
    INSTAB[0xA2] = INSTR(IT.LOAD, AM.IMMEDIATE, RT.X, FT.NONE)
    INSTAB[0xA4] = INSTR(IT.LOAD, AM.ZEROPAGE, RT.Y, FT.NONE)
    INSTAB[0xA5] = INSTR(IT.LOAD, AM.ZEROPAGE, RT.A, FT.NONE)
    INSTAB[0xA6] = INSTR(IT.LOAD, AM.ZEROPAGE, RT.X, FT.NONE)
    INSTAB[0xA8] = INSTR(IT.TAY, AM.IMPLIED, RT.Y, FT.NONE)
    INSTAB[0xA9] = INSTR(IT.LOAD, AM.IMMEDIATE, RT.A, FT.NONE)
    INSTAB[0xAA] = INSTR(IT.TAX, AM.IMPLIED, RT.X, FT.NONE)
    INSTAB[0xAC] = INSTR(IT.LOAD, AM.ABSOLUTE, RT.Y, FT.NONE)
    INSTAB[0xAD] = INSTR(IT.LOAD, AM.ABSOLUTE, RT.A, FT.NONE)
    INSTAB[0xAE] = INSTR(IT.LOAD, AM.ABSOLUTE, RT.X, FT.NONE)

    INSTAB[0xB0] = INSTR(IT.BRSET, AM.RELATIVE, RT.NONE, FT.C)
    INSTAB[0xB1] = INSTR(IT.LOAD, AM.INDIRECT_Y, RT.A, FT.NONE)
    INSTAB[0xB4] = INSTR(IT.LOAD, AM.ZEROPAGE_X, RT.Y, FT.NONE)
    INSTAB[0xB5] = INSTR(IT.LOAD, AM.ZEROPAGE_X, RT.A, FT.NONE)
    INSTAB[0xB6] = INSTR(IT.LOAD, AM.ZEROPAGE_Y, RT.X, FT.NONE)
    INSTAB[0xB8] = INSTR(IT.CLEAR, AM.IMPLIED, RT.NONE, FT.V)
    INSTAB[0xB9] = INSTR(IT.LOAD, AM.ABSOLUTE_Y, RT.A, FT.NONE)
    INSTAB[0xBA] = INSTR(IT.TSX, AM.IMPLIED, RT.X, FT.NONE)
    INSTAB[0xBC] = INSTR(IT.LOAD, AM.ABSOLUTE_X, RT.Y, FT.NONE)
    INSTAB[0xBD] = INSTR(IT.LOAD, AM.ABSOLUTE_X, RT.A, FT.NONE)
    INSTAB[0xBE] = INSTR(IT.LOAD, AM.ABSOLUTE_Y, RT.X, FT.NONE)

    INSTAB[0xC0] = INSTR(IT.COMPARE, AM.IMMEDIATE, RT.Y, FT.NONE)
    INSTAB[0xC1] = INSTR(IT.COMPARE, AM.X_INDIRECT, RT.A, FT.NONE)
    INSTAB[0xC4] = INSTR(IT.COMPARE, AM.ZEROPAGE, RT.Y, FT.NONE)
    INSTAB[0xC5] = INSTR(IT.COMPARE, AM.ZEROPAGE, RT.A, FT.NONE)
    INSTAB[0xC6] = INSTR(IT.DECREMENT, AM.ZEROPAGE, RT.NONE, FT.NONE)
    INSTAB[0xC8] = INSTR(IT.INCREMENT, AM.IMPLIED, RT.Y, FT.NONE)
    INSTAB[0xC9] = INSTR(IT.COMPARE, AM.IMMEDIATE, RT.A, FT.NONE)
    INSTAB[0xCA] = INSTR(IT.DECREMENT, AM.IMPLIED, RT.X, FT.NONE)
    INSTAB[0xCC] = INSTR(IT.COMPARE, AM.ABSOLUTE, RT.Y, FT.NONE)
    INSTAB[0xCD] = INSTR(IT.COMPARE, AM.ABSOLUTE, RT.A, FT.NONE)
    INSTAB[0xCE] = INSTR(IT.DECREMENT, AM.ABSOLUTE, RT.NONE, FT.NONE)

    INSTAB[0xD0] = INSTR(IT.BRCLR, AM.RELATIVE, RT.NONE, FT.Z)
    INSTAB[0xD1] = INSTR(IT.COMPARE, AM.INDIRECT_Y, RT.A, FT.NONE)
    INSTAB[0xD5] = INSTR(IT.COMPARE, AM.ZEROPAGE_X, RT.A, FT.NONE)
    INSTAB[0xD6] = INSTR(IT.DECREMENT, AM.ZEROPAGE_X, RT.NONE, FT.NONE)
    INSTAB[0xD8] = INSTR(IT.CLEAR, AM.IMPLIED, RT.NONE, FT.D)
    INSTAB[0xD9] = INSTR(IT.COMPARE, AM.ABSOLUTE_Y, RT.A, FT.NONE)
    INSTAB[0xDD] = INSTR(IT.COMPARE, AM.ABSOLUTE_X, RT.A, FT.NONE)
    INSTAB[0xDE] = INSTR(IT.DECREMENT, AM.ABSOLUTE_X, RT.NONE, FT.NONE)

    INSTAB[0xE0] = INSTR(IT.COMPARE, AM.IMMEDIATE, RT.X, FT.NONE)
    INSTAB[0xE1] = INSTR(IT.SBC, AM.X_INDIRECT, RT.A, FT.NONE)
    INSTAB[0xE4] = INSTR(IT.COMPARE, AM.ZEROPAGE_X, RT.X, FT.NONE)
    INSTAB[0xE5] = INSTR(IT.SBC, AM.ZEROPAGE, RT.A, FT.NONE)
    INSTAB[0xE6] = INSTR(IT.INCREMENT, AM.ZEROPAGE, RT.NONE, FT.NONE)
    INSTAB[0xE8] = INSTR(IT.INCREMENT, AM.IMPLIED, RT.X, FT.NONE)
    INSTAB[0xE9] = INSTR(IT.SBC, AM.IMMEDIATE, RT.A, FT.NONE)
    INSTAB[0xEA] = INSTR(IT.NOP, AM.IMPLIED, RT.NONE, FT.NONE)
    INSTAB[0xEC] = INSTR(IT.COMPARE, AM.ABSOLUTE, RT.X, FT.NONE)
    INSTAB[0xED] = INSTR(IT.SBC, AM.ABSOLUTE, RT.A, FT.NONE)
    INSTAB[0xEE] = INSTR(IT.INCREMENT, AM.ABSOLUTE, RT.X, FT.NONE)

    INSTAB[0xF0] = INSTR(IT.BRSET, AM.RELATIVE, RT.NONE, FT.Z)
    INSTAB[0xF1] = INSTR(IT.SBC, AM.INDIRECT_Y, RT.A, FT.NONE)
    INSTAB[0xF5] = INSTR(IT.SBC, AM.ZEROPAGE_X, RT.A, FT.NONE)
    INSTAB[0xF6] = INSTR(IT.INCREMENT, AM.ZEROPAGE_X, RT.NONE, FT.NONE)
    INSTAB[0xF8] = INSTR(IT.SET, AM.IMPLIED, RT.NONE, FT.D)
    INSTAB[0xF9] = INSTR(IT.SBC, AM.ABSOLUTE_Y, RT.A, FT.NONE)
    INSTAB[0xFD] = INSTR(IT.SBC, AM.ABSOLUTE_X, RT.A, FT.NONE)
    INSTAB[0xFE] = INSTR(IT.INCREMENT, AM.ABSOLUTE_X, RT.NONE, FT.NONE)
}
