package com.smile.karaoke

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.smile.karaoke.smileapps.SmileAppsActivity
import com.smile.karaoke.ui.theme.ColorPrimaryDark
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.ui.theme.KaraokePlayerTheme
import com.smile.karaoke.ui.theme.Yellow3
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BasePlayerActivity : ComponentActivity() {

    abstract fun getAppName(): String
    abstract fun startExoPlayer()
    abstract fun startVlcPlayer()

    private var screenSize = Point(0, 0)
    private var permissionExternalStorage = false
    // the following are for ExoPlayer
    protected lateinit var exoLauncher: ActivityResultLauncher<Intent>
    // the following are for VLCPlayer
    protected lateinit var vlcLauncher: ActivityResultLauncher<Intent>
    private lateinit var smileAppsLauncher: ActivityResultLauncher<Intent>
    //
    protected val loadingMessage = mutableStateOf("")
    val backgroundColor = Yellow3

    @SuppressLint("ConfigurationScreenWidthHeight", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG,"onCreate")
        super.onCreate(savedInstanceState)
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        screenSize = ScreenUtil.getScreenSize(this@BasePlayerActivity)
        val intentAction = intent.action
        LogUtil.d(TAG, "onCreate.intentAction = $intentAction")
        val intentCategories = intent.categories
        LogUtil.d(TAG, "onCreate.intentCategories = $intentCategories")
        if (intentCategories != null && intentCategories.isNotEmpty()) {
            for (category in intentCategories) {
                LogUtil.d(TAG, "onCreate.category = $category")
            }
        } else {
            LogUtil.d(TAG, "No categories in intent")
        }

        SmileAppBase.textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@BasePlayerActivity)
        SmileAppBase.toastTextSize = SmileAppBase.textFontSize * 0.7f
        SmileAppBase.fontScale = ScreenUtil.getPxFontScale(this@BasePlayerActivity)
        KaraokeComposable.textFontSize = ScreenUtil.pixelToDp(SmileAppBase.textFontSize).sp
        KaraokeComposable.toastFontSize = ScreenUtil.pixelToDp(SmileAppBase.toastTextSize).sp

        SmileAppBase.deviceType = ScreenUtil.getDeviceType(this@BasePlayerActivity)

        exoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "exoLauncher.result received")
            loadingMessage.value = ""
        }

        vlcLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "vlcLauncher.result received")
            loadingMessage.value = ""
        }

        smileAppsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "smileAppsLauncher.result received")
            loadingMessage.value = ""
        }

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

        enableEdgeToEdge()
        setContent {
            LogUtil.d(TAG,"onCreate.setContent")
            KaraokePlayerTheme {
                Scaffold {innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)
                        .background(backgroundColor),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        SetMainUiTitle()
                        Box {
                            DisplayLoading()
                            CreateMainUINew()
                        }
                    }
                }
            }

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "handleOnBackPressed")
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
            LogUtil.d(TAG, "onRequestPermissionsResult.permissions = $str")
        }
        if (requestCode == PERMISSION_WRITE_EXTERNAL_CODE) {
            val rLen = grantResults.size
            permissionExternalStorage = rLen > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            LogUtil.d(TAG, "onRequestPermissionsResult.permissionExternalStorage = $permissionExternalStorage")
        }
        if (!permissionExternalStorage) {
            ScreenUtil.showToast(this, "Permission Denied", 60f, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
            LogUtil.d(TAG, "onRequestPermissionsResult.Permission Denied")
            exitApp()
        }
    }

    private fun exitApp() {
        LogUtil.d(TAG, "exitApp")
        finish()
    }

    private fun startExoActivity() {
        startExoPlayer()
    }

    private fun startVlcActivity() {
        startVlcPlayer()
    }

    private fun showSmileAppsActivity() {
        Intent(
            this@BasePlayerActivity,
            SmileAppsActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            smileAppsLauncher.launch(it)
        }
    }

    @Composable
    fun SetMainUiTitle() {
        Text(modifier = Modifier.padding(all = 0.dp)
            .background(Color.Transparent),
            text = getAppName(), color = ColorPrimaryDark,
            fontSize = KaraokeComposable.textFontSize)
    }

    @Composable
    fun DisplayLoading() {
        if (loadingMessage.value.isEmpty()) {
            return
        }
        Column(modifier = Modifier
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = getString(R.string.loadingStr),
                color = Color.Blue, fontWeight = FontWeight.Bold,
                fontSize = KaraokeComposable.textFontSize.times(2.0f))
        }
    }

    @Composable
    fun ExoPlayerButton(modifier: Modifier = Modifier,
                        buttonWidth: Float,
                        buttonHeight: Float,
                        textLineHeight: TextUnit) {
        LogUtil.d(TAG, "ExoPlayerButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            val focusRequester = remember { FocusRequester() }
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
                    .background(color = buttonBackground)
                    .focusRequester(focusRequester),
                interactionSource = interactionSource,
                colors = ButtonColors(
                    containerColor =
                        if (!exoClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!isFocused && !exoClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            { Text(text = "ExoPlayer", fontSize = KaraokeComposable.textFontSize) }
            Text(//modifier = Modifier.weight(2.0f),
                lineHeight = textLineHeight,
                text = getString(R.string.exoDescription),
                color = Color.Red, fontSize = KaraokeComposable.toastFontSize)
        }
    }

    @Composable
    fun VlcPlayerButton(modifier: Modifier = Modifier,
                        buttonWidth: Float,
                        buttonHeight: Float,
                        textLineHeight: TextUnit) {
        LogUtil.d(TAG, "VlcPlayerButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
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
                interactionSource = interactionSource,
                colors = ButtonColors(
                    containerColor =
                        if (!vlcClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!isFocused && !vlcClicked.value)
                            buttonContentColor
                        else Color.Red,
                    disabledContentColor = buttonContentColor
                )
            )
            { Text(text = "VLCPlayer", fontSize = KaraokeComposable.textFontSize) }
            Text(//modifier = Modifier.weight(2.0f),
                lineHeight = textLineHeight,
                text = getString(R.string.vlcDescription),
                color = Color.Red, fontSize = KaraokeComposable.toastFontSize)
        }
    }

    @Composable
    fun SmileAppsButton(modifier: Modifier = Modifier,
                        buttonWidth: Float,
                        buttonHeight: Float,
                        textLineHeight: TextUnit) {
        LogUtil.d(TAG, "SmileAppsButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val isClicked = remember { mutableStateOf(false) }
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        isClicked.value = true
                        delay(200)
                        showSmileAppsActivity()
                        isClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!isClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!isClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            { Text(text = getString(R.string.smileApps),
                fontSize = KaraokeComposable.textFontSize) }
        }
    }

    @Composable
    fun CreateMainUI() {
        LogUtil.d(TAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val maxWidth = ScreenUtil.pixelToDp(screenSize.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screenSize.y.toFloat())
        LogUtil.d(TAG, "CreateMainUI.maxHeight = $maxHeight")
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
        LogUtil.d(TAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val textLineHeight = (KaraokeComposable.toastFontSize.value + 5.0f).sp
        Column(modifier = Modifier
            .fillMaxSize()) {
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

    @Composable
    fun CreateMainUINew() {
        LogUtil.i(TAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val maxWidth = ScreenUtil.pixelToDp(screenSize.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screenSize.y.toFloat())
        LogUtil.d(TAG, "CreateMainUI.maxHeight = $maxHeight")
        var verSpacerWeight = 1.0f
        var horSpacerWeight = 1.0f
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            verSpacerWeight = 0.2f
            horSpacerWeight = 2.5f
        }
        val buttonWidth = maxWidth * ((10.0f - horSpacerWeight * 2.0f) / 10.0f)
        LogUtil.i(TAG, "CreateMainUI.buttonWidth = $buttonWidth")
        // 1 in 5
        val buttonHeight = maxHeight * ((10.0f - verSpacerWeight * 2.0f) / 10.0f) / 5.0f
        LogUtil.i(TAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val textLineHeight = (KaraokeComposable.toastFontSize.value + 5.0f).sp
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            ExoPlayerButton(modifier = Modifier.weight(1.0f),
                buttonWidth, buttonHeight, textLineHeight)
            VlcPlayerButton(modifier = Modifier.weight(1.0f),
                buttonWidth, buttonHeight, textLineHeight)
            SmileAppsButton(modifier = Modifier.weight(1.0f),
                buttonWidth, buttonHeight, textLineHeight)
        }
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        LogUtil.d(TAG, "onSaveInstanceState()")
    }

    private fun stopCast() {
        LogUtil.d(TAG, "stopCast")
        (application as SmileAppBase).castContext?.apply {
            // stop casting
            LogUtil.d(TAG, "stopCasting.endCurrentSession")
            sessionManager.endCurrentSession(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
        stopCast()
    }

    companion object {
        private const val TAG : String = "BasePlayerActivity"
        private const val PERMISSION_WRITE_EXTERNAL_CODE = 0x11
    }
}