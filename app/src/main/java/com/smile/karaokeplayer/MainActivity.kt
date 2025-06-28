package com.smile.karaokeplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.smile.karaokeplayer.exoplayer.ExoPlayerActivity
import com.smile.karaokeplayer.ui.theme.KaraokePlayerTheme
import com.smile.karaokeplayer.ui.theme.Yellow3
import com.smile.karaokeplayer.vlcplayer.VlcPlayerActivity
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var permissionExternalStorage = false
    private var textFontSize = 0f
    private var toastTextSize = 0f
    // the following are for VLCPlayer
    private lateinit var vlcLauncher: ActivityResultLauncher<Intent>
    // the following are for ExoPlayer
    private lateinit var exoLauncher: ActivityResultLauncher<Intent>
    //
    private val loadingMessage = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"onCreate")
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this@MainActivity,
            SmileApp.FontSize_Scale_Type, null)
        textFontSize = ScreenUtil.suitableFontSize(this@MainActivity,
            defaultTextFontSize,
            SmileApp.FontSize_Scale_Type,0.0f)
        toastTextSize = textFontSize * 0.7f
        SmileApp.textFontSize = textFontSize
        SmileApp.toastTextSize = toastTextSize
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
            Log.d(TAG, "vlcLauncher.result received")
            loadingMessage.value = ""
        }

        exoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(TAG, "exoLauncher.result received")
            loadingMessage.value = ""
        }
        setContent {
            Log.d(TAG,"onCreate.setContent")
            KaraokePlayerTheme {
                Box {
                    DisplayLoading()
                    CreateMainUI()
                }
            }

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(TAG, "handleOnBackPressed")
                    exitApp()
                }
            })
        }
    }

    // @RequiresApi(api = Build.VERSION_CODES.M)
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
            Log.d(TAG, "onRequestPermissionsResult.permissions = $str")
        }
        if (requestCode == PERMISSION_WRITE_EXTERNAL_CODE) {
            val rLen = grantResults.size
            permissionExternalStorage = rLen > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "onRequestPermissionsResult.permissionExternalStorage = $permissionExternalStorage")
        }
        if (!permissionExternalStorage) {
            ScreenUtil.showToast(this, "Permission Denied", 60f, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
            Log.d(TAG, "onRequestPermissionsResult.Permission Denied")
            exitApp()
        }
    }

    private fun exitApp() {
        Log.d(TAG, "exitApp")
        finish()
    }

    private fun showColorWhenClick(isClicked: MutableState<Boolean>) {
        CoroutineScope(Dispatchers.Default).launch {
            isClicked.value = true
            delay(500)
            isClicked.value = false
        }
    }

    private fun startExoActivity() {
        Intent(
            this@MainActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    private fun startVlcActivity() {
        Intent(
            this@MainActivity,
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
        Column(modifier = Modifier.fillMaxSize()
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
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val exoClicked = remember { mutableStateOf(false) }
            Button(
                onClick = {
                    showColorWhenClick(exoClicked)
                    startExoActivity()
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
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val vlcClicked = remember { mutableStateOf(false) }
            Button(
                onClick = {
                    showColorWhenClick(vlcClicked)
                    startVlcActivity()
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
        Log.d(TAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val screen = ScreenUtil.getScreenSize(this@MainActivity)
        val maxWidth = ScreenUtil.pixelToDp(screen.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screen.y.toFloat())
        Log.d(TAG, "CreateMainUI.maxHeight = $maxHeight")
        var verSpacerWeight = 2.0f
        var horSpacerWeight = 1.0f
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            verSpacerWeight = 1.0f
            horSpacerWeight = 2.5f
        }
        val buttonWidth = maxWidth * (10.0f - horSpacerWeight * 2.0f)
        // 1 in 5
        val buttonHeight = maxHeight * (10.0f - (verSpacerWeight * 2.0f)) / 50.0f
        Log.d(TAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val backgroundColor = Yellow3
        val textLineHeight = (Composables.toastFontSize.value + 5.0f).sp
        Column(modifier = Modifier.fillMaxSize()
            .background(color = backgroundColor)) {
            Spacer(modifier = Modifier.fillMaxWidth().weight(verSpacerWeight))
            Row(modifier = Modifier.weight(10.0f - verSpacerWeight * 2.0f)) {
                Spacer(modifier = Modifier.fillMaxHeight().weight(horSpacerWeight))
                Column(modifier = Modifier
                    .weight(10.0f - horSpacerWeight * 2.0f)) {
                    ExoPlayerButton(modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight)
                    VlcPlayerButton(modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight)
                }
                Spacer(modifier = Modifier.fillMaxHeight().weight(horSpacerWeight))
            }
            Spacer(modifier = Modifier.fillMaxSize().weight(verSpacerWeight))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(TAG, "onSaveInstanceState()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        // Log.d(TAG, "onDestroy().Process.killProcess()")
        // Process.killProcess(Process.myPid())
        // exitProcess(0);
    }

    companion object {
        private const val TAG : String = "MainActivity"
        private const val PERMISSION_WRITE_EXTERNAL_CODE = 0x11
    }
}