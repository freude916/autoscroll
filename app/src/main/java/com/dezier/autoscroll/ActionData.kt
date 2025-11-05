package com.dezier.autoscroll

import androidx.compose.ui.geometry.Offset
import kotlin.time.Duration

sealed interface ActionData {
    data class Press(
        val downDuration: Duration,
        val upDuration: Duration,
        val repeatCount: Int,
        val randomDelay: Duration,
        val randomOffset: Offset,
    )
}