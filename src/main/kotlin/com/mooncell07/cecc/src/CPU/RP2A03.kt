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
    // var Register: Register = Register
    var opcode: Int = 0xEA
    var instr: INSTR = INSTAB[opcode]
    private var busAddr: UShort = 0x0000u
    private var pageCheck: Boolean = false

    init {
        Register.PC = bus.readWord(0xFFFCu)
    }

    private fun fetch(): UByte = bus.read(Register.PC++)

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
        var v = (Register.PC + 1u).toUShort()

        if (vec == 0xFFFA.toUShort()) {
            bus.dummyRead(Register.PC)
            v--
        }
        bus.dummyRead(Register.PC)

        push(MSB(v))
        push(LSB(v))
        push(setBit(Register[RT.SR].toInt(), FT.B.ordinal - 1).toUByte())
        Register.setPC(bus.readWord(vec))

        Register[FT.I] = true
    }

    private fun getIMPL(): UByte = Register[instr.regType]

    private fun getIMM(): UByte = fetch()

    private fun getACC(): UByte = Register[RT.A]

    private fun getABS(): UShort = fetchWord()

    private fun getZP(): UShort = fetch().toUShort()

    private fun getZPX(): UShort {
        val base = fetch()
        val v = ((base + Register[RT.X]) % 0x100u).toUShort()
        bus.dummyRead(base.toUShort())
        return v
    }

    private fun getZPY(): UShort {
        val base = fetch()
        val v = ((base + Register[RT.Y]) % 0x100u).toUShort()
        bus.dummyRead(base.toUShort())
        return v
    }

    private fun getABSX(): UShort {
        val base = fetchWord()
        val effective = (base + Register[RT.X]).toUShort()
        handleInvalidAddress(base, effective)
        return effective
    }

    private fun getABSY(): UShort {
        val base = fetchWord()
        val effective = (base + Register[RT.Y]).toUShort()
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
        val base = (addr + Register[RT.X]) % 0x100u
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
        val effective = (base + Register[RT.Y].toUShort()).toUShort()
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
        bus.write((Register[RT.SP] + 0x100u).toUShort(), data)
        if (!dummy) Register[RT.SP]--
    }

    private fun pop(dummy: Boolean = false): UByte {
        if (!dummy) Register[RT.SP]++
        val v = bus.read((Register[RT.SP] + 0x100u).toUShort())
        return v
    }

    private fun adjust(incr: Boolean) {
        val m = readSource(instr.addrMode)
        val v = (if (incr) (m + 1u) else (m - 1u)).toUByte()
        when (instr.addrMode) {
            AM.IMPLIED -> {
                Register[instr.regType] = v
                bus.dummyRead(Register.PC)
            }
            else -> {
                if ((instr.addrMode == AM.ABSOLUTE_X) and !pageCheck) {
                    bus.dummyRead(busAddr)
                }
                bus.dummyWrite(busAddr, m)
                bus.write(busAddr, v)
            }
        }
        Register[FT.N] = testBit(v.toInt(), 7)
        Register[FT.Z] = v.toInt() == 0
    }

    private fun opLOAD() {
        val m = readSource(instr.addrMode)
        Register[instr.regType] = m
        Register[FT.N] = testBit(m.toInt(), 7)
        Register[FT.Z] = m.toInt() == 0
    }

    private fun opSTORE() {
        bus.write(readSourceWord(instr.addrMode), Register[instr.regType])
    }

    private fun opTRANSFER() {
        val r =
            when (instr.insType) {
                IT.TXA -> Register[RT.X]
                IT.TYA -> Register[RT.Y]
                IT.TXS -> Register[RT.X]
                IT.TAY -> Register[RT.A]
                IT.TAX -> Register[RT.A]
                IT.TSX -> Register[RT.SP]
                else -> throw IllegalArgumentException("Unsupported Instruction Type: ${instr.insType}")
            }
        Register[instr.regType] = r

        if (instr.insType != IT.TXS) {
            Register[FT.N] = testBit(r.toInt(), 7)
            Register[FT.Z] = r.toInt() == 0
        }

        bus.dummyRead(Register.PC)
    }

    private fun opSET() {
        Register[instr.flagType] = true
        bus.dummyRead(Register.PC)
    }

    private fun opCLEAR() {
        Register[instr.flagType] = false
        bus.dummyRead(Register.PC)
    }

    private fun opBRANCH() {
        val offset = readSourceWord(instr.addrMode)
        val f =
            when (instr.insType) {
                IT.BRSET -> Register[instr.flagType]
                IT.BRCLR -> !Register[instr.flagType]
                else -> throw IllegalArgumentException("Unsupported Instruction Type: ${instr.insType}")
            }

        if (f) {
            bus.dummyRead(Register.PC)
            val effective = (Register.PC + offset).toUShort()
            handleInvalidAddress(Register.PC, effective)
            Register.setPC(effective)
        }
    }

    private fun opCOMPARE() {
        val r = Register[instr.regType]
        val m = readSource(instr.addrMode)
        val v = r - m
        Register[FT.C] = r >= m
        Register[FT.Z] = r == m
        Register[FT.N] = testBit(v.toInt(), 7)
    }

    private fun opLOGICAL() {
        val v =
            when (instr.insType) {
                IT.AND -> Register[RT.A] and readSource(instr.addrMode)
                IT.ORA -> Register[RT.A] or readSource(instr.addrMode)
                IT.EOR -> Register[RT.A] xor readSource(instr.addrMode)
                else -> throw IllegalArgumentException("Unsupported Instruction Type: ${instr.insType}")
            }
        Register[RT.A] = v
        Register[FT.N] = testBit(v.toInt(), 7)
        Register[FT.Z] = v.toInt() == 0
    }

    private fun opSHIFT() {
        val m = readSource(instr.addrMode).toUInt()
        val v: UInt

        when (instr.insType) {
            IT.ASL -> {
                v = m shl 1
                Register[FT.C] = testBit(m.toInt(), 7)
            }
            IT.LSR -> {
                v = m shr 1
                Register[FT.C] = testBit(m.toInt(), 0)
            }
            IT.ROL -> {
                val c = (if (Register[FT.C]) 1 else 0)
                v = (m shl 1) or c.toUInt()
                Register[FT.C] = testBit(m.toInt(), 7)
            }
            IT.ROR -> {
                val c = (if (Register[FT.C]) 1 else 0)
                v = (m shr 1) or (c.toUInt() shl 7)
                Register[FT.C] = testBit(m.toInt(), 0)
            }
            else -> throw IllegalArgumentException("Unsupported Instruction Type: ${instr.insType}")
        }

        when (instr.addrMode) {
            AM.ACCUMULATOR -> {
                bus.dummyRead(Register.PC)
                Register[RT.A] = v.toUByte()
            }
            else -> {
                if ((instr.addrMode == AM.ABSOLUTE_X) and !pageCheck) {
                    bus.dummyRead(busAddr)
                }
                bus.dummyWrite(busAddr, m.toUByte())
                bus.write(busAddr, v.toUByte())
            }
        }

        Register[FT.N] = testBit(v.toInt(), 7)
        Register[FT.Z] = (v % 0x100u).toInt() == 0
    }

    private fun opPUSH() {
        var r = Register[instr.regType]
        if (instr.regType == RT.SR) {
            r = setBit(r.toInt(), Register.getFlagOrdinal(FT.B)).toUByte()
        }
        bus.dummyRead(Register.PC)
        push(r)
    }

    private fun opPULL() {
        bus.dummyRead(Register.PC)
        pop(dummy = true)

        val r = pop()
        Register[instr.regType] = r

        when (instr.regType) {
            RT.SR -> {
                Register[FT.B] = false
                Register[FT.UNUSED2_IGN] = true
            }

            else -> {
                Register[FT.N] = testBit(r.toInt(), 7)
                Register[FT.Z] = r.toInt() == 0
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
        bus.dummyRead(Register.PC)
    }

    private fun opJMP() {
        val addr = readSourceWord(instr.addrMode)
        Register.setPC(addr)
    }

    private fun opJSR() {
        val lo = fetch()
        pop(dummy = true)
        push(MSB(Register.PC))
        push(LSB(Register.PC))
        val hi = bus.read(Register.PC)
        Register.setPC(concat(hi, lo))
    }

    private fun opADC() {
        val a = Register[RT.A]
        val m = readSource(instr.addrMode)
        val c = (if (Register[FT.C]) 1u else 0u)
        val v = (m + a + c).toInt()

        Register[RT.A] = v.toUByte()
        Register[FT.C] = v > 0xFF
        Register[FT.Z] = (v % 0x100) == 0
        Register[FT.N] = testBit(v, 7)

        val aBit = testBit(a.toInt(), 7)
        val mBit = testBit(m.toInt(), 7)
        val vBit = testBit(v, 7)
        Register[FT.V] = (aBit == mBit) and (vBit != aBit)
    }

    private fun opSBC() {
        val a = Register[RT.A]
        val m = readSource(instr.addrMode)
        val c = (if (Register[FT.C]) 0u else 1u)
        val v = (a - m - c).toInt()

        Register[RT.A] = v.toUByte()
        Register[FT.C] = v >= 0
        Register[FT.Z] = (v % 0x100) == 0
        Register[FT.N] = testBit(v, 7)

        val aBit = testBit(a.toInt(), 7)
        val mBit = testBit(m.toInt(), 7)
        val vBit = testBit(v, 7)
        Register[FT.V] = (aBit != mBit) and (vBit == mBit)
    }

    private fun opBIT() {
        val a = Register[RT.A]
        val m = readSource(instr.addrMode)
        Register[FT.Z] = (a and m).toInt() == 0
        Register[FT.N] = testBit(m.toInt(), 7)
        Register[FT.V] = testBit(m.toInt(), 6)
    }

    private fun opBRK() = INT(0xFFFEu)

    fun NMI() = INT(0xFFFAu)

    private fun opRTI() {
        bus.dummyRead(Register.PC)
        pop(dummy = true)

        Register[RT.SR] = pop()
        val lo = pop()
        val hi = pop()

        Register[FT.B] = false
        Register[FT.UNUSED2_IGN] = true

        Register.setPC(concat(hi, lo))
    }

    private fun opRTS() {
        bus.dummyRead(Register.PC)
        pop(dummy = true)

        val lo = pop()
        val hi = pop()

        Register.setPC(concat(hi, lo))
        bus.dummyRead(Register.PC++)
    }

    fun tick() {
        opcode = fetch().toInt()
        instr = INSTAB[opcode]
        executors[instr.insType]?.invoke()
    }

    private val executors: Map<IT, () -> Unit> =
        mapOf(
            IT.LOAD to ::opLOAD,
            IT.STORE to ::opSTORE,
            IT.JMP to ::opJMP,
            IT.JSR to ::opJSR,
            IT.NOP to ::opNOP,
            IT.SET to ::opSET,
            IT.BRCLR to ::opBRANCH,
            IT.BRSET to ::opBRANCH,
            IT.CLEAR to ::opCLEAR,
            IT.TAX to ::opTRANSFER,
            IT.TAY to ::opTRANSFER,
            IT.TSX to ::opTRANSFER,
            IT.TXA to ::opTRANSFER,
            IT.TYA to ::opTRANSFER,
            IT.TXS to ::opTRANSFER,
            IT.PUSH to ::opPUSH,
            IT.PULL to ::opPULL,
            IT.INCREMENT to ::opINCREMENT,
            IT.DECREMENT to ::opDECREMENT,
            IT.ADC to ::opADC,
            IT.SBC to ::opSBC,
            IT.AND to ::opLOGICAL,
            IT.EOR to ::opLOGICAL,
            IT.ORA to ::opLOGICAL,
            IT.ASL to ::opSHIFT,
            IT.LSR to ::opSHIFT,
            IT.ROL to ::opSHIFT,
            IT.ROR to ::opSHIFT,
            IT.COMPARE to ::opCOMPARE,
            IT.BIT to ::opBIT,
            IT.BRK to ::opBRK,
            IT.RTI to ::opRTI,
            IT.RTS to ::opRTS,
        )

    private val decoders: Map<AM, () -> UShort> =
        mapOf(
            AM.ABSOLUTE to ::getABS,
            AM.ABSOLUTE_X to ::getABSX,
            AM.ABSOLUTE_Y to ::getABSY,
            AM.X_INDIRECT to ::getXIND,
            AM.INDIRECT_Y to ::getINDY,
            AM.ZEROPAGE to ::getZP,
            AM.ZEROPAGE_X to ::getZPX,
            AM.ZEROPAGE_Y to ::getZPY,
            AM.RELATIVE to ::getREL,
            AM.INDIRECT to ::getIND,
        )
}
