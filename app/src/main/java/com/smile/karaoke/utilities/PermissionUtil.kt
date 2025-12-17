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

    private const val TAG = "PermissionUtil"
    private const val PERMISSION_WRITE_EXTERNAL_CODE = 0x11

    private var permissionExternalStorage = false

    fun askPermissions(activity: Activity, checkMediaPermission: Boolean = true): Boolean {
        val logStr = "askPermissions"
        LogUtil.d(TAG, "$logStr.checkMediaPermission = $checkMediaPermission")
        permissionExternalStorage = if (checkMediaPermission) {
            (ActivityCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            true    // no need to ask permission
        }
        LogUtil.d(TAG, "$logStr.permissionExternalStorage = $permissionExternalStorage")
        if (!permissionExternalStorage) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                PERMISSION_WRITE_EXTERNAL_CODE
            )
        }

        askIgnoreOptimizationsBattery(activity)

        return permissionExternalStorage
    }

    fun onRequestPermResult(requestCode: Int,
                            grantResults: IntArray): Boolean {
        LogUtil.d(TAG, "onRequestPermResult")
        if (requestCode == PERMISSION_WRITE_EXTERNAL_CODE) {
            LogUtil.d(TAG, "onRequestPermResult.requestCode")
            val rLen = grantResults.size
            permissionExternalStorage =
                rLen > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        LogUtil.d(TAG, "onRequestPermResult.permissionExternalStorage = $permissionExternalStorage")
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