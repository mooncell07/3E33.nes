package com.mooncell07.cecc.tests

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mooncell07.cecc.src.AbstractDevice
import com.mooncell07.cecc.src.Bus
import com.mooncell07.cecc.src.CPU.RP2A03
import com.mooncell07.cecc.src.CPU.buildInstructionTable
import com.mooncell07.cecc.src.Clock
import com.mooncell07.cecc.src.DT
import com.mooncell07.cecc.src.OAM
import com.mooncell07.cecc.src.RT
import java.io.File
import kotlin.system.exitProcess

@Suppress("ktlint")
val illegalOpcodes = arrayOf(
    "02", "03", "04", "07", "0B", "0C", "0F", "12", "13", "14",
    "17", "1A", "1B", "1C", "1F", "22", "23", "27", "2B", "2F",
    "32", "33", "34", "37", "3A", "3B", "3C", "3F", "42", "43",
    "44", "47", "4B", "4F", "52", "53", "54", "57", "5A", "5B",
    "5C", "5F", "62", "63", "64", "67", "6B", "6F", "72", "73",
    "74", "77", "7A", "7B", "7C", "7F", "80", "82", "83", "87",
    "89", "8B", "8F", "92", "93", "97", "9B", "9C", "9E", "9F",
    "A3", "A7", "AB", "AF", "B2", "B3", "B7", "BB", "BF", "C2",
    "C3", "C7", "CB", "CF", "D2", "D3", "D4", "D7", "DA", "DB",
    "DC", "DF", "E2", "E3", "E7", "EB", "EF", "F2", "F3", "F4",
    "F7", "FA", "FB", "FC", "FF"
)

data class State(
    @SerializedName("pc") var PC: UShort,
    @SerializedName("s") var SP: UByte,
    @SerializedName("a") var A: UByte,
    @SerializedName("x") var X: UByte,
    @SerializedName("y") var Y: UByte,
    @SerializedName("p") var SR: UByte,
    @SerializedName("ram") var ram: MutableList<List<Int>>,
) {
    override fun toString(): String =
        """State(PC=${PC.toHexString(HexFormat.UpperCase)}, 
            |SP=${SP.toHexString(HexFormat.UpperCase)}, 
            |A=${A.toHexString(HexFormat.UpperCase)}, 
            |X=${X.toHexString(HexFormat.UpperCase)}, 
            |Y=${Y.toHexString(HexFormat.UpperCase)}, 
            |SR=${SR.toHexString(HexFormat.UpperCase)}, 
            |ram=${ ram.map {innerList ->
            innerList.map { it.toUShort().toHexString(HexFormat.UpperCase) }
        }})
        """.trimMargin().replace("\n", "")
}

data class Test(
    @SerializedName("name") val name: String,
    @SerializedName("initial") val initial: State,
    @SerializedName("final") val final: State,
    @SerializedName("cycles") val cycles: List<List<Any>>,
)

class DebugDevice : AbstractDevice() {
    override val type = DT.DEBUG
    override val size = 0xFFFF
    override val base = 0x0000

    private val area: UByteArray = UByteArray(0x10000) { 0u }
    private var noCycle: Boolean = false

    var cycles: MutableList<MutableList<Any>> = mutableListOf(mutableListOf(Any()))

    fun stopLogging() {
        noCycle = true
    }

    fun startLogging() {
        noCycle = false
    }

    override fun read(address: UShort): UByte {
        val result = area[address.toInt()]
        if (!noCycle) {
            cycles.add(mutableListOf(address.toDouble(), result.toDouble(), "read"))
        }
        return result
    }

    override fun write(
        address: UShort,
        data: UByte,
    ) {
        area[address.toInt()] = data
        if (!noCycle) {
            cycles.add(mutableListOf(address.toDouble(), data.toDouble(), "write"))
        }
    }
}

open class BaseEmulator {
    private val clock = Clock()
    val debugDevice = DebugDevice()
    val bus = Bus(clock, OAM(), debugDevice)
    val re6502 = RP2A03(bus)

    init {
        buildInstructionTable()
        re6502.regs.PC = 0xC000u
    }
}

class InstructionTest(
    file: File,
    private val opcode: String,
) : BaseEmulator() {
    private var tests: Array<Test>
    private val after: State = State(0u, 0u, 0u, 0u, 0u, 0u, mutableListOf(listOf()))

    init {
        val gson = Gson()
        tests = gson.fromJson(file.readText(), Array<Test>::class.java)
    }

    private fun setEmuState(test: Test) {
        re6502.regs.PC = test.initial.PC
        re6502.regs[RT.A] = test.initial.A
        re6502.regs[RT.X] = test.initial.X
        re6502.regs[RT.Y] = test.initial.Y
        re6502.regs[RT.SP] = test.initial.SP
        re6502.regs[RT.SR] = test.initial.SR

        for (ramState in test.initial.ram) {
            bus.write(ramState[0].toUShort(), ramState[1].toUByte())
        }
        debugDevice.cycles.clear()
    }

    private fun parseState(test: Test) {
        after.PC = re6502.regs.PC
        after.A = re6502.regs[RT.A]
        after.X = re6502.regs[RT.X]
        after.Y = re6502.regs[RT.Y]
        after.SP = re6502.regs[RT.SP]
        after.SR = re6502.regs[RT.SR]

        after.ram = MutableList(test.final.ram.size) { listOf(2) }
        for ((i, ramState) in test.final.ram.withIndex()) {
            after.ram[i] = listOf(ramState[0], bus.read(ramState[0].toUShort()).toInt())
        }
    }

    private fun prettyCycles(c: List<List<Any>>) =
        "${c.map {
            Triple(
                (it[0] as Number).toInt().toUShort().toHexString(HexFormat.UpperCase),
                (it[1] as Number).toInt().toUByte().toHexString(HexFormat.UpperCase),
                it[2],
            )
        }}"

    private fun compareStates(
        index: Int,
        test: Test,
    ) {
        val dataTest = test.final != after
        val cycleTest = (debugDevice.cycles != test.cycles)
        if (dataTest or cycleTest) {
            println(
                """[
                    |$$opcode FAILED @ 
                    |<TEST: $index 
                    |NAME: ${test.name.uppercase()} 
                    |MISMATCH TYPE: ${if (dataTest and cycleTest) {
                    "BOTH"
                } else if (dataTest) {
                    "DATA"
                } else {
                    "CYCLE"
                }}
                    |>]
                """.trimMargin().replace("\n", ""),
            )
            println(
                """
                |DATA LOG:
                |MINE: ${test.final}
                |YOURS: $after
                |
                |CYCLE LOG:
                |MINE: ${prettyCycles(test.cycles)}
                |YOURS: ${prettyCycles(debugDevice.cycles)}
                """.trimMargin(),
            )
            exitProcess(1)
        }
    }

    private fun compare(
        index: Int,
        test: Test,
    ) {
        parseState(test)
        compareStates(index, test)
    }

    fun run() {
        for ((i, test) in tests.withIndex()) {
            debugDevice.stopLogging()
            setEmuState(test)
            debugDevice.startLogging()
            re6502.tick()
            debugDevice.stopLogging()
            compare(i, test)
        }
        println("[$$opcode]: PASSED!")
    }
}

fun step(t: File) {
    val opcode = t.name.removeSuffix(".json").uppercase()
    if (!t.isDirectory and (opcode !in illegalOpcodes)) {
        val iTest = InstructionTest(t, opcode)
        iTest.run()
    }
}

fun main(args: Array<String>) {
    if (args[0] == "--batch") {
        for (t in File(args[1]).listFiles()!!) {
            step(t)
        }
    } else {
        step(File(args[0]))
    }
    println("\nALL TESTS PASSED!")
}
