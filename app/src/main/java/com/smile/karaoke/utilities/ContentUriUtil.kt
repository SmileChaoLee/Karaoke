package com.smile.karaoke.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.ArrayList

object ContentUriUtil {
    private const val TAG = "ContentUriUtil"

    @JvmStatic
    fun intentForSelectFile(isSingleFile: Boolean): Intent {
        LogUtil.d(TAG, "intentForSelectFile")
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, !isSingleFile)
            type = "*/*"
            // Optional: filter for specific types, e.g., "video/*"
            // putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "video/mp4"))
        }

        return intent
    }

    @JvmStatic
    fun getUrisList(context: Context, data: Intent): ArrayList<Uri> {
        LogUtil.d(TAG, "getUrisList")
        val urisList = ArrayList<Uri>()
        val clipData = data.clipData
        if ( clipData != null) {
            // multiple files
            LogUtil.d(TAG, "getUrisList.multiple files")
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i).uri?.let{
                    if (!Uri.EMPTY.equals(it) && getPermissionForContentUri(context, it)) {
                        urisList.add(it)
                    }
                }
            }
        } else {
            // single file
            data.data?.let {
                if (!Uri.EMPTY.equals(it) && getPermissionForContentUri(context, it)) {
                    urisList.add(it)
                    LogUtil.d(TAG, "getUrisList.single file.it = $it")
                }
            }
        }
        return urisList
    }

    private fun getPermissionForContentUri(context: Context, contentUri: Uri): Boolean {
        try {
            context.contentResolver.takePersistableUriPermission(
                contentUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {
            LogUtil.e(TAG, "getPermissionForContentUri.Exception: ", e)
            return false
        }
        return true
    }

    /**
     * Copies data from a content URI to a temporary file in the app's cache directory.
     *
     * This is the safest and most reliable way to obtain a File object from a content URI.
     *
     * @param context The application or activity context.
     * @param contentUri The input content URI (e.g., from an Intent).
     * @return The resulting temporary File object, or null if an error occurred.
     */
    fun getFileFromContentUri(context: Context?, contentUri: Uri): File? {
        return try {
            // 1. Determine a file name for the temporary file
            val fileName = getFileName(context, contentUri) ?: "temp_file_${System.currentTimeMillis()}"
            val tempFile = File(context?.cacheDir, fileName)

            // 2. Open input stream securely using ContentResolver
            context?.contentResolver?.openInputStream(contentUri)?.use { inputStream ->
                // 3. Open output stream to the temporary file location
                FileOutputStream(tempFile).use { outputStream ->
                    // 4. Copy all bytes from the input to the output
                    inputStream.copyTo(outputStream)
                }
            }
            LogUtil.d(TAG, "Successfully created temporary file: ${tempFile.absolutePath}")
            tempFile
        } catch (e: Exception) {
            LogUtil.e(TAG, "Error converting content URI to file: $contentUri", e)
            null
        }
    }

    private fun getFileName(context: Context?, uri: Uri): String? {
        var name: String? = null
        // Query the content provider for the display name
        val cursor = context?.contentResolver?.query(uri, null,
            null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
}