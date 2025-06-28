package com.smile.karaokeplayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object Composables {
    private const val TAG = "Composables"
    var fontSize = 24.sp
        set(value) {
            if (field != value) {
                field = value
            }
        }
    var toastFontSize = (fontSize.value * 0.7f).sp
        set(value) {
            if (field != value) {
                field = value
            }
        }

    @Composable
    fun textUnitToDp(sp: TextUnit): Dp {
        val dp = with(LocalDensity.current) {
            sp.toDp()
        }
        return dp
    }
}