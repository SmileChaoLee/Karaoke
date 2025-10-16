package com.smile.karaoke.utilities

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlin.text.lowercase

object MediaTools {

    private const val TAG = "MediaTools"

    fun getMimeTypeFromMedia(filePath: String): String? {
        val msgString = "getMimeTypeFromMedia"
        LogUtil.d(TAG, msgString)
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        } catch (e: Exception) {
            // Handle exceptions, e.g., file not found or not a valid media file
            LogUtil.e(TAG, "${msgString}.Error retrieving MIME type: ${e.message}")
            null
        } finally {
            retriever.release()
        }
    }

    /**
     * Example usage with a file path:
     * val mimeType = getMimeTypeFromMedia("/path/to/your/media/file.mp4")
     * LogUtil.d("MimeType", "MIME type: $mimeType")
     * You can also use it with a Uri:
    */
    fun getMimeTypeFromMediaUri(context: Context, uri: Uri): String? {
        val msgString = "getMimeTypeFromMediaUri"
        LogUtil.d(TAG, msgString)
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        } catch (e: Exception) {
            LogUtil.e(TAG, "${msgString}.Error retrieving MIME type from Uri: ${e.message}", e)
            null
        } finally {
            retriever.release()
        }
    }

    /**
     * Example usage:
     * val mimeType = getMimeTypeFromExtension("/path/to/your/file.jpg")
     * LogUtil.d("MimeType", "MIME type from extension: $mimeType")
    */
    fun getMimeTypeFromExtension(filePath: String): String? {
        val msgString = "getMimeTypeFromExtension"
        LogUtil.d(TAG, msgString)
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        return if (extension != null) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else {
            null
        }
    }
}