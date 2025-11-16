package com.smile.karaoke.smileapps

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.karaoke.R
import com.smile.karaoke.ui.theme.KaraokePlayerTheme
import com.smile.karaoke.KaraokeComposable
import com.smile.karaoke.ui.theme.Yellow3
import androidx.core.view.WindowCompat
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.AppLinkUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmileAppsActivity : ComponentActivity() {

    companion object {
        private const val TAG = "SmileAppsActivity"
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@SmileAppsActivity)
        val toastTextSize = textFontSize * 0.7f
        KaraokeComposable.textFontSize = ScreenUtil.pixelToDp(textFontSize).sp
        KaraokeComposable.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp

        val backgroundColor = Yellow3

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val conf = LocalConfiguration.current
            val listWeight =
                if (conf.orientation == Configuration.ORIENTATION_PORTRAIT)
                9.0f else 8.0f
            val buttonWeight = 10.0f - listWeight
            KaraokePlayerTheme {
                Scaffold { innerPadding ->
                    Column(modifier = Modifier.fillMaxSize()
                        .padding(innerPadding)
                        .background(backgroundColor),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        AppListView(modifier = Modifier.weight(listWeight))
                        OkButton(modifier = Modifier.weight(buttonWeight))
                    }
                }
            }
        }
    }

    @Composable
    fun AppListView(modifier: Modifier) {
        val appList = AppLinkUtil.getAppList(this@SmileAppsActivity)
        val textLineHeight = (KaraokeComposable.textFontSize.value + 10.0f).sp
        Column(modifier = modifier) {
            Text(modifier = Modifier.align(Alignment.CenterHorizontally),
                text = getString(R.string.smileApps),
                color = Color.Red, fontWeight = FontWeight.Bold,
                fontSize = KaraokeComposable.textFontSize)
            HorizontalDivider(modifier = Modifier.fillMaxWidth(),
                thickness = 10.dp, color = Color.Black)
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(height = 20.dp))
            LazyColumn {
                items(items = appList) { item ->
                    var isFocused by remember { mutableStateOf(false) }
                    Column(modifier = Modifier
                        .onFocusChanged {
                            LogUtil.d(TAG, "AppListViewColumn.Column.onFocusChanged")
                            isFocused = it.hasFocus
                        }
                        .clickable(
                        onClick = {
                            // start the link
                            LogUtil.d(TAG, "AppListView.Column.onClick")
                            AppLinkUtil.startAppLinkOnStore(
                                this@SmileAppsActivity,
                                item.appUrl
                            )
                        })
                        .background(
                            if (isFocused) Color(SmileAppBase.accentColor)
                            else Color.Transparent
                        )
                    ) {
                        Row {
                            Text(
                                text = item.appName, color = Color.Blue,
                                fontWeight = FontWeight.Medium,
                                fontSize = KaraokeComposable.textFontSize
                            )
                            Image(modifier = Modifier
                                .size(
                                    KaraokeComposable
                                        .textUnitToDp(KaraokeComposable.textFontSize)
                                )
                                .padding(all = 0.dp),
                                painter = painterResource(item.icon),
                                contentDescription = "",
                                contentScale = ContentScale.FillBounds
                            )
                        }
                        Text(text = item.appUrl, color = Color.Black,
                            lineHeight = textLineHeight,
                            fontWeight = FontWeight.Normal,
                            fontSize = KaraokeComposable.textFontSize,
                        )
                        HorizontalDivider(modifier = Modifier.fillMaxWidth(),
                            thickness = 5.dp, color = Color.White)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth(),
                thickness = 5.dp, color = Color.Black)
        }
    }

    @Composable
    fun OkButton(modifier: Modifier) {
        val focusRequester = remember { FocusRequester() }
        Column(modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            var isClicked by remember { mutableStateOf(false) }
            Button(modifier = Modifier
                .onFocusChanged {
                    LogUtil.d(TAG, "OkButton.onFocusChanged")
                }
                .focusRequester(focusRequester),
                interactionSource = interactionSource,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        isClicked = true
                        delay(200)
                        isClicked = false
                        finish()
                    }
                },
                colors = ButtonColors(
                    containerColor = if (isFocused) {
                        Color(SmileAppBase.accentColor)
                    } else {
                        if (!isClicked) Color.Blue else Color.Cyan
                    },
                    disabledContainerColor = Color.DarkGray,
                    contentColor = Color.Green,
                    disabledContentColor = Color.LightGray
                )
            )
            { Text(text = getString(R.string.okString),
                fontSize = KaraokeComposable.textFontSize) }
        }

        LaunchedEffect(Unit) {
            delay(500L)
            focusRequester.requestFocus()
        }
    }
}
