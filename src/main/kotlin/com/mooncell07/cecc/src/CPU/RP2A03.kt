package com.mooncell07.cecc.src.CPU

import com.mooncell07.cecc.src.AM
import com.mooncell07.cecc.src.Bus
import com.mooncell07.cecc.src.FT
import com.mooncell07.cecc.src.IT
import com.mooncell07.cecc.src.LSB
import com.mooncell07.cecc.src.MSB
import com.mooncell07.cecc.src.RT
import com.mooncell07.cecc.src.concat
import com.mooncell07.cecc.src.setBit
import com.mooncell07.cecc.src.testBit

class RP2A03(
    private val bus: Bus,
) {
    var regs: Register = Register()
    var opcode: Int = 0xEA
    var instr: INSTR = INSTAB[opcode]
    private var busAddr: UShort = 0x0000u
    private var pageCheck: Boolean = false

    init {
        regs.PC = bus.readWord(0xFFFCu)
    }

    private fun fetch(): UByte = bus.read(regs.PC++)

    private fun fetchWord(): UShort {
        val lo: UByte = fetch()
        val hi: UByte = fetch()

        return concat(hi, lo)
    }

    private fun handleInvalidAddress(
        base: UShort,
        effective: UShort,
    ) {
        val msbBase = MSB(base).toInt()
        val msbEffective = MSB(effective).toInt()
        pageCheck = msbBase != msbEffective
        if (pageCheck) {
            val addr = effective + (if (((msbEffective - msbBase) and 0xFF) == 0xFF) 0x100 else -0x100).toUShort()
            bus.dummyRead(addr.toUShort())
        }
    }

    private fun INT(vec: UShort) {
        var v = (regs.PC + 1u).toUShort()

        if (vec == 0xFFFA.toUShort()) {
            bus.dummyRead(regs.PC)
            v--
        }
        bus.dummyRead(regs.PC)

        push(MSB(v))
        push(LSB(v))
        push(setBit(regs[RT.SR].toInt(), FT.B.ordinal - 1).toUByte())
        regs.setPC(bus.readWord(vec))

        regs[FT.I] = true
    }

    private fun getIMPL(): UByte = regs[instr.regType]

    private fun getIMM(): UByte = fetch()

    private fun getACC(): UByte = regs[RT.A]

    private fun getABS(): UShort = fetchWord()

    private fun getZP(): UShort = fetch().toUShort()

    private fun getZPX(): UShort {
        val base = fetch()
        val v = ((base + regs[RT.X]) % 0x100u).toUShort()
        bus.dummyRead(base.toUShort())
        return v
    }

    private fun getZPY(): UShort {
        val base = fetch()
        val v = ((base + regs[RT.Y]) % 0x100u).toUShort()
        bus.dummyRead(base.toUShort())
        return v
    }

    private fun getABSX(): UShort {
        val base = fetchWord()
        val effective = (base + regs[RT.X]).toUShort()
        handleInvalidAddress(base, effective)
        return effective
    }

    private fun getABSY(): UShort {
        val base = fetchWord()
        val effective = (base + regs[RT.Y]).toUShort()
        handleInvalidAddress(base, effective)
        return effective
    }

    private fun getREL(): UShort = fetch().toByte().toUShort()

    private fun getIND(): UShort {
        val base = fetchWord()
        val lo = bus.read(base)
        val hi =
            if (LSB(base).toUInt() == 0xFFu) {
                bus.read((base and 0xFF00u))
            } else {
                bus.read((base + 1u).toUShort())
            }
        return concat(hi, lo)
    }

    private fun getXIND(): UShort {
        val addr = fetch()
        bus.dummyRead(addr.toUShort())
        val base = (addr + regs[RT.X]) % 0x100u
        val lo = bus.read(base.toUShort())
        val hiAddr = (base + 1u) % 0x100u
        val hi = bus.read(hiAddr.toUShort())
        return concat(hi, lo)
    }

    private fun getINDY(): UShort {
        val ptr = fetch()
        val lo = bus.read(ptr.toUShort())
        val hi = bus.read(((ptr + 1u) % 0x100u).toUShort())
        val base = concat(hi, lo)
        val effective = (base + regs[RT.Y].toUShort()).toUShort()
        handleInvalidAddress(base, effective)
        return effective
    }

    private fun readSource(mode: AM): UByte {
        if (mode == AM.INDIRECT || mode == AM.RELATIVE) {
            throw IllegalArgumentException("readSrc() does not support INDIRECT and RELATIVE modes.")
        }
        return when (mode) {
            AM.IMMEDIATE -> getIMM()
            AM.ACCUMULATOR -> getACC()
            AM.IMPLIED -> getIMPL()
            else -> {
                busAddr = decoders[mode]!!.invoke()
                bus.read(busAddr)
            }
        }
    }

    private fun readSourceWord(mode: AM): UShort {
        val v = decoders[mode]!!.invoke()
        if (!pageCheck) {
            when (mode) {
                AM.INDIRECT_Y, AM.ABSOLUTE_Y, AM.ABSOLUTE_X -> bus.dummyRead(v)
                else -> {}
            }
        }
        return v
    }

    private fun push(
        data: UByte,
        dummy: Boolean = false,
    ) {
        bus.write((regs[RT.SP] + 0x100u).toUShort(), data)
        if (!dummy) regs[RT.SP]--
    }

    private fun pop(dummy: Boolean = false): UByte {
        if (!dummy) regs[RT.SP]++
        val v = bus.read((regs[RT.SP] + 0x100u).toUShort())
        return v
    }

    private fun adjust(incr: Boolean) {
        val m = readSource(instr.addrMode)
        val v = (if (incr) (m + 1u) else (m - 1u)).toUByte()
        when (instr.addrMode) {
            AM.IMPLIED -> {
                regs[instr.regType] = v
                bus.dummyRead(regs.PC)
            }
            else -> {
                if ((instr.addrMode == AM.ABSOLUTE_X) and !pageCheck) {
                    bus.dummyRead(busAddr)
                }
                bus.dummyWrite(busAddr, m)
                bus.write(busAddr, v)
            }
        }
        regs[FT.N] = testBit(v.toInt(), 7)
        regs[FT.Z] = v.toInt() == 0
    }

    private fun opLOAD() {
        val m = readSource(instr.addrMode)
        regs[instr.regType] = m
        regs[FT.N] = testBit(m.toInt(), 7)
        regs[FT.Z] = m.toInt() == 0
    }

    private fun opSTORE() {
        bus.write(readSourceWord(instr.addrMode), regs[instr.regType])
    }

    private fun opTRANSFER() {
        val r =
            when (instr.insType) {
                IT.TXA -> regs[RT.X]
                IT.TYA -> regs[RT.Y]
                IT.TXS -> regs[RT.X]
                IT.TAY -> regs[RT.A]
                IT.TAX -> regs[RT.A]
                IT.TSX -> regs[RT.SP]
                else -> throw IllegalArgumentException("Unsupported Instruction Type: ${instr.insType}")
            }
        regs[instr.regType] = r

        if (instr.insType != IT.TXS) {
            regs[FT.N] = testBit(r.toInt(), 7)
            regs[FT.Z] = r.toInt() == 0
        }

        bus.dummyRead(regs.PC)
    }

    private fun opSET() {
        regs[instr.flagType] = true
        bus.dummyRead(regs.PC)
    }

    private fun opCLEAR() {
        regs[instr.flagType] = false
        bus.dummyRead(regs.PC)
    }

    private fun opBRANCH() {
        val offset = readSourceWord(instr.addrMode)
        val f =
            when (instr.insType) {
                IT.BRSET -> regs[instr.flagType]
                IT.BRCLR -> !regs[instr.flagType]
                else -> throw IllegalArgumentException("Unsupported Instruction Type: ${instr.insType}")
            }

        if (f) {
            bus.dummyRead(regs.PC)
            val effective = (regs.PC + offset).toUShort()
            handleInvalidAddress(regs.PC, effective)
            regs.setPC(effective)
        }
    }

    private fun opCOMPARE() {
        val r = regs[instr.regType]
        val m = readSource(instr.addrMode)
        val v = r - m
        regs[FT.C] = r >= m
        regs[FT.Z] = r == m
        regs[FT.N] = testBit(v.toInt(), 7)
    }

    private fun opLOGICAL() {
        val v =
            when (instr.insType) {
                IT.AND -> regs[RT.A] and readSource(instr.addrMode)
                IT.ORA -> regs[RT.A] or readSource(instr.addrMode)
                IT.EOR -> regs[RT.A] xor readSource(instr.addrMode)
                else -> throw IllegalArgumentException("Unsupported Instruction Type: ${instr.insType}")
            }
        regs[RT.A] = v
        regs[FT.N] = testBit(v.toInt(), 7)
        regs[FT.Z] = v.toInt() == 0
    }

    private fun opSHIFT() {
        val m = readSource(instr.addrMode).toUInt()
        val v: UInt

        when (instr.insType) {
            IT.ASL -> {
                v = m shl 1
                regs[FT.C] = testBit(m.toInt(), 7)
            }
            IT.LSR -> {
                v = m shr 1
                regs[FT.C] = testBit(m.toInt(), 0)
            }
            IT.ROL -> {
                val c = (if (regs[FT.C]) 1 else 0)
                v = (m shl 1) or c.toUInt()
                regs[FT.C] = testBit(m.toInt(), 7)
            }
            IT.ROR -> {
                val c = (if (regs[FT.C]) 1 else 0)
                v = (m shr 1) or (c.toUInt() shl 7)
                regs[FT.C] = testBit(m.toInt(), 0)
            }
            else -> throw IllegalArgumentException("Unsupported Instruction Type: ${instr.insType}")
        }

        when (instr.addrMode) {
            AM.ACCUMULATOR -> {
                bus.dummyRead(regs.PC)
                regs[RT.A] = v.toUByte()
            }
            else -> {
                if ((instr.addrMode == AM.ABSOLUTE_X) and !pageCheck) {
                    bus.dummyRead(busAddr)
                }
                bus.dummyWrite(busAddr, m.toUByte())
                bus.write(busAddr, v.toUByte())
            }
        }

        regs[FT.N] = testBit(v.toInt(), 7)
        regs[FT.Z] = (v % 0x100u).toInt() == 0
    }

    private fun opPUSH() {
        var r = regs[instr.regType]
        if (instr.regType == RT.SR) {
            r = setBit(r.toInt(), regs.getFlagOrdinal(FT.B)).toUByte()
        }
        bus.dummyRead(regs.PC)
        push(r)
    }

    private fun opPULL() {
        bus.dummyRead(regs.PC)
        pop(dummy = true)

        val r = pop()
        regs[instr.regType] = r

        when (instr.regType) {
            RT.SR -> {
                regs[FT.B] = false
                regs[FT.UNUSED2_IGN] = true
            }

            else -> {
                regs[FT.N] = testBit(r.toInt(), 7)
                regs[FT.Z] = r.toInt() == 0
            }
        }
    }

    private fun opINCREMENT() {
        adjust(incr = true)
    }

    private fun opDECREMENT() {
        adjust(incr = false)
    }

    private fun opNOP() {
        bus.dummyRead(regs.PC)
    }

    private fun opJMP() {
        val addr = readSourceWord(instr.addrMode)
        regs.setPC(addr)
    }

    private fun opJSR() {
        val lo = fetch()
        pop(dummy = true)
        push(MSB(regs.PC))
        push(LSB(regs.PC))
        val hi = bus.read(regs.PC)
        regs.setPC(concat(hi, lo))
    }

    private fun opADC() {
        val a = regs[RT.A]
        val m = readSource(instr.addrMode)
        val c = (if (regs[FT.C]) 1u else 0u)
        val v = (m + a + c).toInt()

        regs[RT.A] = v.toUByte()
        regs[FT.C] = v > 0xFF
        regs[FT.Z] = (v % 0x100) == 0
        regs[FT.N] = testBit(v, 7)

        val aBit = testBit(a.toInt(), 7)
        val mBit = testBit(m.toInt(), 7)
        val vBit = testBit(v, 7)
        regs[FT.V] = (aBit == mBit) and (vBit != aBit)
    }

    private fun opSBC() {
        val a = regs[RT.A]
        val m = readSource(instr.addrMode)
        val c = (if (regs[FT.C]) 0u else 1u)
        val v = (a - m - c).toInt()

        regs[RT.A] = v.toUByte()
        regs[FT.C] = v >= 0
        regs[FT.Z] = (v % 0x100) == 0
        regs[FT.N] = testBit(v, 7)

        val aBit = testBit(a.toInt(), 7)
        val mBit = testBit(m.toInt(), 7)
        val vBit = testBit(v, 7)
        regs[FT.V] = (aBit != mBit) and (vBit == mBit)
    }

    private fun opBIT() {
        val a = regs[RT.A]
        val m = readSource(instr.addrMode)
        regs[FT.Z] = (a and m).toInt() == 0
        regs[FT.N] = testBit(m.toInt(), 7)
        regs[FT.V] = testBit(m.toInt(), 6)
    }

    private fun opBRK() = INT(0xFFFEu)

    fun NMI() = INT(0xFFFAu)

    private fun opRTI() {
        bus.dummyRead(regs.PC)
        pop(dummy = true)

        regs[RT.SR] = pop()
        val lo = pop()
        val hi = pop()

        regs[FT.B] = false
        regs[FT.UNUSED2_IGN] = true

        regs.setPC(concat(hi, lo))
    }

    private fun opRTS() {
        bus.dummyRead(regs.PC)
        pop(dummy = true)

        val lo = pop()
        val hi = pop()

        regs.setPC(concat(hi, lo))
        bus.dummyRead(regs.PC++)
    }

    fun tick() {
        opcode = fetch().toInt()
        instr = INSTAB[opcode]
        assert(instr.insType != IT.NONE) { "${instr.insType} is an illegal instruction type." }
        executors[instr.insType]?.invoke()
    }

    private val executors: Map<IT, () -> Unit> =
        mapOf(
            IT.LOAD to { opLOAD() },
            IT.STORE to { opSTORE() },
            IT.JMP to { opJMP() },
            IT.JSR to { opJSR() },
            IT.NOP to { opNOP() },
            IT.SET to { opSET() },
            IT.BRCLR to { opBRANCH() },
            IT.BRSET to { opBRANCH() },
            IT.CLEAR to { opCLEAR() },
            IT.TAX to { opTRANSFER() },
            IT.TAY to { opTRANSFER() },
            IT.TSX to { opTRANSFER() },
            IT.TXA to { opTRANSFER() },
            IT.TYA to { opTRANSFER() },
            IT.TXS to { opTRANSFER() },
            IT.PUSH to { opPUSH() },
            IT.PULL to { opPULL() },
            IT.INCREMENT to { opINCREMENT() },
            IT.DECREMENT to { opDECREMENT() },
            IT.ADC to { opADC() },
            IT.SBC to { opSBC() },
            IT.AND to { opLOGICAL() },
            IT.EOR to { opLOGICAL() },
            IT.ORA to { opLOGICAL() },
            IT.ASL to { opSHIFT() },
            IT.LSR to { opSHIFT() },
            IT.ROL to { opSHIFT() },
            IT.ROR to { opSHIFT() },
            IT.COMPARE to { opCOMPARE() },
            IT.BIT to { opBIT() },
            IT.BRK to { opBRK() },
            IT.RTI to { opRTI() },
            IT.RTS to { opRTS() },
        )

    private val decoders: Map<AM, () -> UShort> =
        mapOf(
            AM.ABSOLUTE to { getABS() },
            AM.ABSOLUTE_X to { getABSX() },
            AM.ABSOLUTE_Y to { getABSY() },
            AM.X_INDIRECT to { getXIND() },
            AM.INDIRECT_Y to { getINDY() },
            AM.ZEROPAGE to { getZP() },
            AM.ZEROPAGE_X to { getZPX() },
            AM.ZEROPAGE_Y to { getZPY() },
            AM.RELATIVE to { getREL() },
            AM.INDIRECT to { getIND() },
        )
}
