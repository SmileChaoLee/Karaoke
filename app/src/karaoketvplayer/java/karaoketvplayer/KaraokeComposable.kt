package karaoketvplayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object KaraokeComposable {

    var textFontSize = 24.sp
        set(value) {
            if (field != value) {
                field = value
            }
        }
    var toastFontSize = (textFontSize.value * 0.7f).sp
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