package com.smile.karaoke.utilities

import android.app.Activity
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.Coil.imageLoader
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size

object ImageUtil {

    private const val TAG = "ImageUtil"
    suspend fun getBitmapFromUri(act: Activity, url: String?): Bitmap? {
        var bm: Bitmap? = null
        val imageLoader = act.imageLoader
        val request = ImageRequest.Builder(act)
            .data(url)
            // Set size to original to get the full image size, or specify a custom Size
            .size(Size.ORIGINAL)
            // Disabling hardware bitmaps is often needed if you intend to modify the bitmap
            .allowHardware(false)
            .build()
        try {
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                LogUtil.d(TAG, "getBitmapFromUri.SuccessResult")
                // Convert the resulting Drawable to a Bitmap
                bm = result.drawable.toBitmap()
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "getBitmapFromUri.Exception: ", e)
        }

        return bm
    }
}