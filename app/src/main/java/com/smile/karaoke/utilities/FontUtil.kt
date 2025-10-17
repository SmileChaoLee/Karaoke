package com.smile.karaoke.utilities

import android.app.Activity
import com.smile.smilelibraries.utilities.ScreenUtil

object FontUtil {
    fun getTextFontSizeNeeded(activity: Activity): Float {
        val defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(activity,
            ScreenUtil.FontSize_Pixel_Type, null)
        return ScreenUtil.suitableFontSize(activity,
            defaultTextFontSize,
            ScreenUtil.FontSize_Pixel_Type,0.0f)
    }

    fun getFontSize(activity: Activity): Float {
        return ScreenUtil.suitableFontScale(activity,
            ScreenUtil.FontSize_Pixel_Type, 0.0f)
    }
}