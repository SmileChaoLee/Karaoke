package com.smile.karaokeplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.smile.karaokeplayer.exoplayer.ExoPlayerActivity
import com.smile.karaokeplayer.ui.theme.KaraokePlayerTheme
import com.smile.karaokeplayer.ui.theme.Yellow3
import com.smile.karaokeplayer.vlcplayer.VlcPlayerActivity
import com.smile.smilelibraries.utilities.ScreenUtil

class MainActivity : ComponentActivity() {

    private var permissionExternalStorage = false
    private var textFontSize = 0f
    private var toastTextSize = 0f
    // the following are for VLCPlayer
    private lateinit var vlcLauncher: ActivityResultLauncher<Intent>
    // the following are for ExoPlayer
    private lateinit var exoLauncher: ActivityResultLauncher<Intent>
    //

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

        super.onCreate(savedInstanceState)

        vlcLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(TAG, "vlcLauncher.result received")
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "vlcLauncher.RESULT_OK")
            }
        }

        exoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(TAG, "exoLauncher.result received")
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "exoLauncher.RESULT_OK")
            }
        }

        setContent {
            Log.d(TAG,"onCreate.setContent")
            KaraokePlayerTheme {
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

                val screen = ScreenUtil.getScreenSize(this@MainActivity)
                val maxWidth = ScreenUtil.pixelToDp(screen.x.toFloat())
                val maxHeight = ScreenUtil.pixelToDp(screen.y.toFloat())
                Log.d(TAG, "onCreate.setContent.maxHeight = $maxHeight")
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
                Log.d(TAG, "onCreate.setContent.buttonHeight = $buttonHeight")
                val backgroundColor = Yellow3
                val buttonBackground = Color.Transparent
                val buttonContentColor = Color.Green
                val buttonContainerColor = Color.Blue
                Column(modifier = Modifier.fillMaxSize()
                    .background(color = backgroundColor)) {
                    Spacer(modifier = Modifier.fillMaxWidth().weight(verSpacerWeight))
                    Row(modifier = Modifier.weight(10.0f - verSpacerWeight * 2.0f)) {
                        Spacer(modifier = Modifier.fillMaxHeight().weight(horSpacerWeight))
                        Column(modifier = Modifier
                            .weight(10.0f - horSpacerWeight * 2.0f)) {
                            Column(modifier = Modifier.weight(1.0f).
                                fillMaxHeight(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center) {
                                Button(
                                    onClick = { startExoActivity() },
                                    modifier = Modifier//.weight(1.0f)
                                        .width(width = buttonWidth.dp)
                                        .height(height = buttonHeight.dp)
                                        .background(color = buttonBackground),
                                    colors = ButtonColors(
                                        containerColor = buttonContainerColor,
                                        disabledContainerColor = buttonContainerColor,
                                        contentColor = buttonContentColor,
                                        disabledContentColor = buttonContentColor
                                    )
                                )
                                { Text(text = "ExoPlayer", fontSize = Composables.fontSize) }
                                Text(//modifier = Modifier.weight(2.0f),
                                    text = getString(R.string.exoDescription),
                                    color = Color.Red, fontSize = Composables.toastFontSize)
                            }
                            Column(modifier = Modifier.weight(1.0f),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center) {
                                Button(
                                    onClick = { startVlcActivity() },
                                    modifier = Modifier//.weight(1.0f)
                                        .width(width = buttonWidth.dp)
                                        .height(height = buttonHeight.dp)
                                        .background(color = buttonBackground),
                                    colors = ButtonColors(
                                        containerColor = buttonContainerColor,
                                        disabledContainerColor = buttonContainerColor,
                                        contentColor = buttonContentColor,
                                        disabledContentColor = buttonContentColor
                                    )
                                )
                                { Text(text = "VLCPlayer", fontSize = Composables.fontSize) }
                                Text(//modifier = Modifier.weight(2.0f),
                                    text = getString(R.string.vlcDescription),
                                    color = Color.Red, fontSize = Composables.toastFontSize)
                            }
                        }
                        Spacer(modifier = Modifier.fillMaxHeight().weight(horSpacerWeight))
                    }
                    Spacer(modifier = Modifier.fillMaxSize().weight(verSpacerWeight))
                }
            }

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
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

    private fun startVlcActivity() {
        Intent(
            this@MainActivity,
            VlcPlayerActivity::class.java
        ).also {
            vlcLauncher.launch(it)
        }
    }

    private fun startExoActivity() {
        Intent(
            this@MainActivity,
            ExoPlayerActivity::class.java
        ).also {
            exoLauncher.launch(it)
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
    }

    companion object {
        private const val TAG : String = "MainActivity"
        private const val PERMISSION_WRITE_EXTERNAL_CODE = 0x11
    }
}