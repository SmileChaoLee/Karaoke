package com.smile.karaoke

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.smile.karaoke.exoplayer.ExoPlayerActivity
import com.smile.karaoke.ui.theme.KaraokePlayerTheme
import com.smile.karaoke.ui.theme.Yellow3
import com.smile.karaoke.vlcplayer.VlcPlayerActivity
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class PlayerActivity : ComponentActivity() {

    private var mTAG : String = "PlayerActivity"
    fun setTag(tag: String) {
        Log.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }
    private var permissionExternalStorage = false
    private var textFontSize = 0f
    private var toastTextSize = 0f
    private var fontSize = 0f
    // the following are for VLCPlayer
    private lateinit var vlcLauncher: ActivityResultLauncher<Intent>
    // the following are for ExoPlayer
    private lateinit var exoLauncher: ActivityResultLauncher<Intent>
    //
    private val loadingMessage = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(mTAG,"onCreate")

        val intentAction = intent.action
        Log.d(mTAG, "onCreate.intentAction = $intentAction")
        val intentCategories = intent.categories
        Log.d(mTAG, "onCreate.intentCategories = $intentCategories")
        if (intentCategories != null && intentCategories.isNotEmpty()) {
            for (category in intentCategories) {
                Log.d(mTAG, "onCreate.category = $category")
            }
        } else {
            Log.d(mTAG, "No categories in intent")
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this@PlayerActivity,
            ScreenUtil.FontSize_Pixel_Type, null)
        textFontSize = ScreenUtil.suitableFontSize(this@PlayerActivity,
            defaultTextFontSize,
            ScreenUtil.FontSize_Pixel_Type,0.0f)
        toastTextSize = textFontSize * 0.7f
        fontSize = ScreenUtil.suitableFontScale(this@PlayerActivity,
            ScreenUtil.FontSize_Pixel_Type, 0.0f)
        SmileAppBase.textFontSize = textFontSize
        SmileAppBase.toastTextSize = toastTextSize
        SmileAppBase.fontSize = fontSize
        Composables.fontSize = ScreenUtil.pixelToDp(textFontSize).sp
        Composables.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp

        permissionExternalStorage =
            (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
        if (!permissionExternalStorage) {
            val permissions : Array<String> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            ActivityCompat.requestPermissions(this,
                permissions,
                PERMISSION_WRITE_EXTERNAL_CODE
            )
        }
        askIgnoreOptimizationsBattery()

        super.onCreate(savedInstanceState)

        vlcLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(mTAG, "vlcLauncher.result received")
            // loadingMessage.value = ""
            restartApp()
        }

        exoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(mTAG, "exoLauncher.result received")
            // loadingMessage.value = ""
            restartApp()
        }
        setContent {
            Log.d(mTAG,"onCreate.setContent")
            KaraokePlayerTheme {
                Box {
                    DisplayLoading()
                    CreateMainUI()
                }
            }

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(mTAG, "handleOnBackPressed")
                    exitApp()
                }
            })
        }
    }

    @SuppressLint("BatteryLife")
    private fun askIgnoreOptimizationsBattery() {
        val pm = getSystemService(POWER_SERVICE) as? PowerManager
        if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent()
            val pName = packageName
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = "package:$pName".toUri()
            startActivity(intent)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (str : String? in permissions) {
            Log.d(mTAG, "onRequestPermissionsResult.permissions = $str")
        }
        if (requestCode == PERMISSION_WRITE_EXTERNAL_CODE) {
            val rLen = grantResults.size
            permissionExternalStorage = rLen > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            Log.d(mTAG, "onRequestPermissionsResult.permissionExternalStorage = $permissionExternalStorage")
        }
        if (!permissionExternalStorage) {
            ScreenUtil.showToast(this, "Permission Denied", 60f, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
            Log.d(mTAG, "onRequestPermissionsResult.Permission Denied")
            exitApp()
        }
    }

    private fun exitApp() {
        Log.d(mTAG, "exitApp")
        finish()
    }

    private fun startExoActivity() {
        Intent(
            this@PlayerActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    private fun startVlcActivity() {
        Intent(
            this@PlayerActivity,
            VlcPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            vlcLauncher.launch(it)
        }
    }

    @Composable
    fun DisplayLoading() {
        if (loadingMessage.value.isEmpty()) {
            return
        }
        val backgroundColor = Yellow3
        Column(modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = getString(R.string.loadingStr),
                color = Color.Blue, fontWeight = FontWeight.Bold,
                fontSize = Composables.fontSize.times(2.0f))
        }
    }

    @Composable
    fun ExoPlayerButton(modifier: Modifier = Modifier,
                        buttonWidth: Float,
                        buttonHeight: Float,
                        textLineHeight: TextUnit) {
        Log.d(mTAG, "ExoPlayerButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val exoClicked = remember { mutableStateOf(false) }
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        exoClicked.value = true
                        delay(200)
                        startExoActivity()
                        exoClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!exoClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!exoClicked.value) buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            { Text(text = "ExoPlayer", fontSize = Composables.fontSize) }
            Text(//modifier = Modifier.weight(2.0f),
                lineHeight = textLineHeight,
                text = getString(R.string.exoDescription),
                color = Color.Red, fontSize = Composables.toastFontSize)
        }
    }

    @Composable
    fun VlcPlayerButton(modifier: Modifier = Modifier,
                        buttonWidth: Float,
                        buttonHeight: Float,
                        textLineHeight: TextUnit) {
        Log.d(mTAG, "VlcPlayerButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val vlcClicked = remember { mutableStateOf(false) }
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        vlcClicked.value = true
                        delay(200)
                        startVlcActivity()
                        vlcClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!vlcClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!vlcClicked.value) buttonContentColor
                        else Color.Red,
                    disabledContentColor = buttonContentColor
                )
            )
            { Text(text = "VLCPlayer", fontSize = Composables.fontSize) }
            Text(//modifier = Modifier.weight(2.0f),
                lineHeight = textLineHeight,
                text = getString(R.string.vlcDescription),
                color = Color.Red, fontSize = Composables.toastFontSize)
        }
    }

    @Composable
    fun CreateMainUI() {
        Log.d(mTAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val screen = ScreenUtil.getScreenSize(this@PlayerActivity)
        val maxWidth = ScreenUtil.pixelToDp(screen.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screen.y.toFloat())
        Log.d(mTAG, "CreateMainUI.maxHeight = $maxHeight")
        var verSpacerWeight = 1.0f
        var horSpacerWeight = 1.0f
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            verSpacerWeight = 0.2f
            horSpacerWeight = 2.5f
        }
        val buttonWidth = maxWidth * (10.0f - horSpacerWeight * 2.0f)
        // 1 in 5
        val buttonHeight = maxHeight * (10.0f - (verSpacerWeight * 2.0f)) / 50.0f
        Log.d(mTAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val backgroundColor = Yellow3
        val textLineHeight = (Composables.toastFontSize.value + 5.0f).sp
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)) {
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .weight(verSpacerWeight))
            Row(modifier = Modifier.weight(10.0f - verSpacerWeight * 2.0f)) {
                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .weight(horSpacerWeight))
                Column(modifier = Modifier
                    .weight(10.0f - horSpacerWeight * 2.0f)) {
                    ExoPlayerButton(modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight)
                    VlcPlayerButton(modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight)
                }
                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .weight(horSpacerWeight))
            }
            Spacer(modifier = Modifier
                .fillMaxSize()
                .weight(verSpacerWeight))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(mTAG, "onResume")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(mTAG, "onSaveInstanceState()")
    }

    private fun stopCast() {
        Log.d(mTAG, "stopCast")
        (application as SmileAppBase).castContext?.apply {
            // stop casting
            Log.d(mTAG, "stopCasting.endCurrentSession")
            sessionManager.endCurrentSession(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(mTAG, "onDestroy")
        stopCast()
    }

    private fun restartApp() {
        Log.d(mTAG, "restartApp")
        stopCast()
        finish()
        // then restart in 1 seconds
        val tmpHandler = Handler(Looper.getMainLooper())
        val tmpRunnable = Runnable {
            Log.d(mTAG, "restartApp.tmpRunnable()")
            tmpHandler.removeCallbacksAndMessages(null)
            val i: Intent? = baseContext.packageManager
                .getLaunchIntentForPackage(baseContext.packageName)
            i?.let {
                Log.d(mTAG, "restartApp.startActivity()")
                startActivity(Intent.makeRestartActivityTask(it.component))
            }
            // Runtime.getRuntime().exit(0)
            // exitProcess(0);
            android.os.Process.killProcess(android.os.Process.myPid())
        }
        tmpHandler.postDelayed(tmpRunnable, 1000)
    }

    companion object {
        private const val PERMISSION_WRITE_EXTERNAL_CODE = 0x11
    }
}