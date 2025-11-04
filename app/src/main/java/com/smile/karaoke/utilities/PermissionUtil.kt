package com.smile.karaoke.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri

object PermissionUtil {

    const val PERMISSION_WRITE_EXTERNAL_CODE = 0x11

    private var permissionExternalStorage = false

    fun askPermissions(activity: Activity): Boolean {
        permissionExternalStorage =
            (ActivityCompat.checkSelfPermission(activity.applicationContext,
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
            ActivityCompat.requestPermissions(activity,
                permissions,
                PERMISSION_WRITE_EXTERNAL_CODE
            )
        }
        askIgnoreOptimizationsBattery(activity)

        return permissionExternalStorage
    }

    fun onRequestPermResult(requestCode: Int,
                            grantResults: IntArray): Boolean {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_CODE) {
            val rLen = grantResults.size
            permissionExternalStorage =
                rLen > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        return permissionExternalStorage
    }

    @SuppressLint("BatteryLife")
    private fun askIgnoreOptimizationsBattery(activity: Activity) {
        val pm = activity.getSystemService(POWER_SERVICE) as? PowerManager
        if (pm != null && !pm.isIgnoringBatteryOptimizations(activity.packageName)) {
            val intent = Intent()
            val pName = activity.packageName
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = "package:$pName".toUri()
            activity.startActivity(intent)
        }
    }
}