package com.smile.karaokeplayer.googlecast

import android.util.Log
import androidx.core.net.toUri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.common.images.WebImage
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.net.NetworkInterface

class WebServerAndCast {
    private var webServer: HttpServerForLocal? = null

    fun startWebServer(fileName: String) {
        Log.d(TAG, "startWebServer.webServer = $webServer")
        Log.d(TAG, "startWebServer.fileName $fileName")
        val orgFileName = webServer?.mediaFileName
        Log.d(TAG, "startWebServer.orgFileName $orgFileName")
        if (webServer == null || fileName != orgFileName) {
            webServer = HttpServerForLocal(SERVER_PORT, fileName)
            Log.d(TAG, "startWebServer.$fileName.on port $SERVER_PORT")
            webServer?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        } else {
            // Server already running, maybe update media file or simply cast
            // if already serving correct file
            // For simplicity, let's assume you'd restart it or have a more complex logic
            Log.w(TAG, "Web server already running." +
                    " Consider restarting or updating served file.")
        }
    }

    fun startWebServerAndCast(castSession: CastSession, fileName: String) {
        try {
            startWebServer(fileName)
            // Get device IP address (you'll need a utility function for this)
            // val deviceIpAddress = getDeviceIpAddress(context) // Implement this function
            val deviceIpAddress = getDeviceIpAddress()
            if (deviceIpAddress != null) {
                val mediaUrl = "http://$deviceIpAddress:$SERVER_PORT"
                // Now call your loadRemoteMedia function with this mediaUrl
                loadRemoteMedia(castSession, mediaUrl,
                    fileName, "Local Media", null)
            } else {
                Log.e(TAG, "Could not get device IP address")
                // Handle error: cannot form URL
            }

        } catch (e: IOException) {
            Log.e(TAG, "Error starting web server", e)
            webServer = null
        }
    }

    fun stopWebServer() {
        webServer?.stop()
        webServer = null
        Log.d(TAG, "Web server stopped")
    }

    fun loadRemoteMedia(castSession: CastSession,
                        localMediaUrl: String, title: String,
                        studio: String, imageUrl: String?) {
        castSession.remoteMediaClient?.let { remoteMediaClient ->
            val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
            movieMetadata.putString(MediaMetadata.KEY_TITLE, title)
            movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio)
            imageUrl?.let {
                movieMetadata.addImage(WebImage(it.toUri()))
            }

            // Replace with the actual URL to your local media
            val mediaInfo = MediaInfo.Builder(localMediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED) // Or STREAM_TYPE_LIVE
                .setContentType("video/mp4") // Adjust content type as needed
                .setMetadata(movieMetadata)
                .build()

            val mediaLoadRequestData = MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(true)
                .build()

            remoteMediaClient.load(mediaLoadRequestData)
        } ?: run {
            Log.w(TAG, "Session is not available")
            // Handle case where session is not active
        }
    }

    fun getFilename(): String? {
        return webServer?.mediaFileName
    }

    fun getMediaUrl(): String {
        val fileName = getFilename()
        if (fileName.isNullOrEmpty()) {
            Log.d(TAG, "getMediaUrl.fileName is null or empty")
            return ""
        }
        val ipAddress = getDeviceIpAddress()
        val tmpUrl = "http://$ipAddress:$SERVER_PORT/$fileName"
        Log.d(TAG, "getMediaUrl.tmpUrl = $tmpUrl")
        return tmpUrl
    }

    // Helper to get local IP address
    fun getDeviceIpAddress(): String? {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                // Filter for Wi-Fi or Ethernet interfaces, not loopback, etc.
                if (!networkInterface.isLoopback && networkInterface.isUp) {
                    val inetAddresses = networkInterface.inetAddresses
                    while (inetAddresses.hasMoreElements()) {
                        val inetAddress = inetAddresses.nextElement()
                        if (!inetAddress.isLoopbackAddress
                            && inetAddress is java.net.Inet4Address) {
                            return inetAddress.hostAddress
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "getLocalIpAddress.Error getting IP address", ex)
        }
        return null
    }

    // Utility to get IP Address (simplified example, needs error handling and network checks)
    /*
    fun getDeviceIpAddress(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE)
                as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return if (ipAddress == 0) null else
            String.format(
                Locale.ENGLISH,
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
    }
    */

    companion object {
        private const val TAG = "WebServerAndCast"
        private const val SERVER_PORT = 8080 // Choose an available port
    }
}