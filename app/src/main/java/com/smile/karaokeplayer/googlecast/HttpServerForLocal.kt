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
            val mimeType = getMimeTypeForFile(mediaFileName)

            newChunkedResponse(Response.Status.OK, mimeType, fis)
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT, "Error serving file: ${e.message}")
        }
    }
}