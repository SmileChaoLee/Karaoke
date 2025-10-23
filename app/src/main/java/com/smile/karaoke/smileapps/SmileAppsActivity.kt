package com.smile.karaoke.smileapps

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smile.karaoke.R
import com.smile.karaoke.ui.theme.KaraokePlayerTheme
import com.smile.karaoke.KaraokeComposable
import com.smile.karaoke.ui.theme.Yellow3
import androidx.core.net.toUri
import com.smile.karaoke.utilities.LogUtil

class SmileAppsActivity : ComponentActivity() {

    private data class AndroidApp
        (val appName: String,
         val icon: Int,
         val appUrl: String)
    private val appsList = ArrayList<AndroidApp>()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appsList.add(AndroidApp(getString(R.string.karaoke_app_name),
            R.drawable.karaoke_app_icon,
            "https://play.google.com/store/apps/details?id=com.smile.karaokeplayer"))

        appsList.add(AndroidApp(getString(R.string.video_app_name),
            R.drawable.video_app_icon,
            "https://play.google.com/store/apps/details?id=com.smile.videoplayer"))

        appsList.add(AndroidApp(getString(R.string.karaoke_tv_app_name),
            R.drawable.karaoke_tv_app_icon,
            "https://play.google.com/store/apps/details?id=com.smile.karaoketvplayer"))

        appsList.add(AndroidApp(getString(R.string.app_name),
            R.drawable.color_balls_app_icon,
            "https://play.google.com/store/apps/details?id=com.smile.colorballs"))

        appsList.add(AndroidApp(getString(R.string.balls_remover_name),
            R.drawable.balls_remover_app_icon,
            "https://play.google.com/store/apps/details?id=com.smile.ballsremover"))

        val backgroundColor = Yellow3

        enableEdgeToEdge()
        setContent {
            KaraokePlayerTheme {
                Scaffold { innerPadding ->
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(backgroundColor)) {
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
                            items(items = appsList) { item ->
                                Column {
                                    Row {
                                        Text(
                                            text = item.appName,
                                            color = Color.Blue,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = KaraokeComposable.textFontSize
                                        )
                                        Image(
                                            modifier = Modifier
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
                                    Text(modifier = Modifier.clickable(
                                        onClick = {
                                            // start the link
                                            startAppLinkOnStore(item.appUrl)
                                        }),
                                        text = item.appUrl,
                                        color = Color.Black, fontWeight = FontWeight.Normal,
                                        fontSize = KaraokeComposable.textFontSize
                                    )
                                    HorizontalDivider(modifier = Modifier.fillMaxWidth(),
                                        thickness = 5.dp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startAppLinkOnStore(link: String) {
        LogUtil.i(TAG, "startAppLinkOnStore.link = $link")
        startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
    }

    companion object {
        private const val TAG = "SmileAppsActivity"
    }
}
