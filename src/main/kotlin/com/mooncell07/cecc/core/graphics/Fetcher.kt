package com.mooncell07.cecc.core.graphics

import com.mooncell07.cecc.core.*

class Fetcher(
    private val ppuReg: PPURegisters,
) {
    val NTBASE = ushortArrayOf(0x2000u, 0x2400u, 0x2800u, 0x2C00u)

    private var state: FetcherState = FetcherState.NT
    private var throttle = 2
    private var shiftRegister = MutableList(0xF) { 0 }

    var dots = 21

    fun tick() {
        if (throttle != 0) {
            throttle--
            return
        } else {
            throttle = 2
        }

        when (state) {
            FetcherState.NT -> {
                state = FetcherState.AT
            }

            FetcherState.AT -> {
                state = FetcherState.BGLSBITS
            }

            FetcherState.BGLSBITS -> {
                state = FetcherState.BGMSBITS
            }

            FetcherState.BGMSBITS -> {
                state = FetcherState.NT
            }
        }
    }
}
