package karaoketvplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import com.smile.karaoke.R
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.utilities.DeviceTypeUtil
import com.smile.karaoke.utilities.FontUtil
import com.smile.karaoke.utilities.LogUtil
import karaokeplayer.ExoPlayerActivity
import karaoketvplayer.ui.theme.KaraokePlayerTheme
import karaoketvplayer.ui.theme.Yellow3
import videoplayer.VlcPlayerActivity
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class PhPlayerActivity : ComponentActivity() {

    private var mTAG : String = "PhPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }
    private var screenSize = Point(0, 0)
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

    @SuppressLint("ConfigurationScreenWidthHeight", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(mTAG,"onCreate")
        super.onCreate(savedInstanceState)
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        screenSize = ScreenUtil.getScreenSize(this@PhPlayerActivity)
        val intentAction = intent.action
        LogUtil.d(mTAG, "onCreate.intentAction = $intentAction")
        val intentCategories = intent.categories
        LogUtil.d(mTAG, "onCreate.intentCategories = $intentCategories")
        if (intentCategories != null && intentCategories.isNotEmpty()) {
            for (category in intentCategories) {
                LogUtil.d(mTAG, "onCreate.category = $category")
            }
        } else {
            LogUtil.d(mTAG, "No categories in intent")
        }

        textFontSize = FontUtil.getTextFontSizeNeeded(this@PhPlayerActivity)
        toastTextSize = textFontSize * 0.7f
        fontSize = FontUtil.getFontSize(this@PhPlayerActivity)
        SmileAppBase.textFontSize = textFontSize
        SmileAppBase.toastTextSize = toastTextSize
        SmileAppBase.fontSize = fontSize
        KaraokeComposable.fontSize = ScreenUtil.pixelToDp(textFontSize).sp
        KaraokeComposable.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp

        SmileAppBase.deviceType = DeviceTypeUtil.getDeviceType(this@PhPlayerActivity)

        vlcLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(mTAG, "vlcLauncher.result received")
            loadingMessage.value = ""
            // restartApp()
        }

        exoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(mTAG, "exoLauncher.result received")
            loadingMessage.value = ""
            // restartApp()
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

        setContent {
            LogUtil.d(mTAG,"onCreate.setContent")
            KaraokePlayerTheme {
                Box {
                    DisplayLoading()
                    CreateMainUI()
                }
            }
            /*
            if (SmileAppBase.deviceType == CommonConstants.DEVICE_TYPE_PHONE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            */

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(mTAG, "handleOnBackPressed")
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
            LogUtil.d(mTAG, "onRequestPermissionsResult.permissions = $str")
        }
        if (requestCode == PERMISSION_WRITE_EXTERNAL_CODE) {
            val rLen = grantResults.size
            permissionExternalStorage = rLen > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            LogUtil.d(mTAG, "onRequestPermissionsResult.permissionExternalStorage = $permissionExternalStorage")
        }
        if (!permissionExternalStorage) {
            ScreenUtil.showToast(this, "Permission Denied", 60f, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
            LogUtil.d(mTAG, "onRequestPermissionsResult.Permission Denied")
            exitApp()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            // Add other network types if needed
            else -> false
        }
    }


    private fun exitApp() {
        LogUtil.d(mTAG, "exitApp")
        finish()
    }

    private fun startExoActivity() {
        Intent(
            this@PhPlayerActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    private fun startVlcActivity() {
        Intent(
            this@PhPlayerActivity,
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
                fontSize = KaraokeComposable.fontSize.times(2.0f))
        }
    }

    @Composable
    fun ExoPlayerButton(modifier: Modifier = Modifier,
                        buttonWidth: Float,
                        buttonHeight: Float,
                        textLineHeight: TextUnit) {
        LogUtil.d(mTAG, "ExoPlayerButton")
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
            { Text(text = "ExoPlayer", fontSize = KaraokeComposable.fontSize) }
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
        LogUtil.d(mTAG, "VlcPlayerButton")
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
            { Text(text = "VLCPlayer", fontSize = KaraokeComposable.fontSize) }
            Text(//modifier = Modifier.weight(2.0f),
                lineHeight = textLineHeight,
                text = getString(R.string.vlcDescription),
                color = Color.Red, fontSize = KaraokeComposable.toastFontSize)
        }
    }

    @Composable
    fun CreateMainUI() {
        LogUtil.d(mTAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val maxWidth = ScreenUtil.pixelToDp(screenSize.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screenSize.y.toFloat())
        LogUtil.d(mTAG, "CreateMainUI.maxHeight = $maxHeight")
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
        LogUtil.d(mTAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val backgroundColor = Yellow3
        val textLineHeight = (KaraokeComposable.toastFontSize.value + 5.0f).sp
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
        LogUtil.d(mTAG, "onResume")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        LogUtil.d(mTAG, "onSaveInstanceState()")
    }

    private fun stopCast() {
        LogUtil.d(mTAG, "stopCast")
        (application as SmileAppBase).castContext?.apply {
            // stop casting
            LogUtil.d(mTAG, "stopCasting.endCurrentSession")
            sessionManager.endCurrentSession(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(mTAG, "onDestroy")
        stopCast()
    }

    private fun restartApp() {
        LogUtil.d(mTAG, "restartApp")
        stopCast()
        finish()
        // then restart in 1 seconds
        val tmpHandler = Handler(Looper.getMainLooper())
        val tmpRunnable = Runnable {
            LogUtil.d(mTAG, "restartApp.tmpRunnable()")
            tmpHandler.removeCallbacksAndMessages(null)
            val i: Intent? = baseContext.packageManager
                .getLaunchIntentForPackage(baseContext.packageName)
            i?.let {
                LogUtil.d(mTAG, "restartApp.startActivity()")
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