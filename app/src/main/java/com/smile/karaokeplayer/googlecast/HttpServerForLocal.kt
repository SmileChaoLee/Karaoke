package com.smile.karaokeplayer.googlecast

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class HttpServerForLocal(port: Int, val mediaFileName: String)
    : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        return try {
            val mediaFile = File(mediaFileName)
            val fis = FileInputStream(mediaFile)
            // Determine mime type (you might need a more robust way to do this)
            // val mimeType = getMimeTypeForFile(mediaFileName)
            // val mimeType = MediaTools.getMimeTypeFromMedia(mediaFileName)
            val mimeType = getMimeType(mediaFile.extension)

            newChunkedResponse(Response.Status.OK, mimeType, fis)
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT, "Error serving file: ${e.message}")
        }
    }

    // You'll need a way to determine the MIME type
    fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            // Add more as needed
            else -> "application/octet-stream"
        }
    }
}