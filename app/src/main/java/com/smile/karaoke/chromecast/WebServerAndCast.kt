package com.smile.karaoke.chromecast

import androidx.core.net.toUri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.common.images.WebImage
import com.smile.karaoke.utilities.LogUtil
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.IOException
import java.net.NetworkInterface

class WebServerAndCast {
    private var webServer: HttpServerForLocal? = null

    fun startWebServer(fileName: String) {
        val msgString = "startWebServer"
        LogUtil.d(TAG, "$msgString.webServer = $webServer")
        LogUtil.d(TAG, "$msgString.fileName $fileName")
        if (webServer != null) {
            LogUtil.d(TAG, "$msgString.webServer is not null, stopping it")
            webServer?.stop()
            webServer = null
        }
        webServer = HttpServerForLocal(SERVER_PORT, fileName)
        webServer?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
    }

    fun startWebServerAndCast(castSession: CastSession, fileName: String) {
        val msgString = "startWebServerAndCast"
        LogUtil.d(TAG, msgString)
        try {
            startWebServer(fileName)
            val deviceIpAddress = getDeviceIpAddress()
            if (deviceIpAddress != null) {
                val mediaUrl = "http://$deviceIpAddress:$SERVER_PORT"
                // Now call your loadRemoteMedia function with this mediaUrl
                loadRemoteMedia(castSession, mediaUrl,
                    fileName, "Local Media", null)
            } else {
                LogUtil.e(TAG, "$msgString.Could not get device IP address")
                // Handle error: cannot form URL
            }

        } catch (e: IOException) {
            LogUtil.e(TAG, "$msgString.Error starting web server", e)
            webServer = null
        }
    }

    fun stopWebServer() {
        val msgString = "stopWebServer"
        LogUtil.d(TAG, msgString)
        webServer?.stop()
        webServer = null
    }

    fun loadRemoteMedia(castSession: CastSession,
                        localMediaUrl: String, title: String,
                        studio: String, imageUrl: String?) {
        val msgString = "loadRemoteMedia"
        LogUtil.d(TAG, msgString)
        castSession.remoteMediaClient?.let { remoteMediaClient ->
            val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
            movieMetadata.putString(MediaMetadata.KEY_TITLE, title)
            movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio)
            imageUrl?.let {
                movieMetadata.addImage(WebImage(it.toUri()))
            }

            // val contentType = NanoHTTPD.getMimeTypeForFile(title)
            // val contentType = MediaTools.getMimeTypeFromMedia(title)
            val contentType = webServer?.getMimeType(File(title).extension)
            // Replace with the actual URL to your local media
            val mediaInfo = MediaInfo.Builder(localMediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED) // Or STREAM_TYPE_LIVE
                .setContentType(contentType)
                .setMetadata(movieMetadata)
                .build()

            val mediaLoadRequestData = MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(true)
                .build()

            remoteMediaClient.load(mediaLoadRequestData)
        } ?: run {
            LogUtil.w(TAG, "$msgString.Session is not available")
            // Handle case where session is not active
        }
    }

    fun getFilename(): String? {
        return webServer?.mediaFileName
    }

    fun getMediaUrl(): String {
        val msgString = "getMediaUrl"
        LogUtil.d(TAG, msgString)
        val fileName = getFilename()
        if (fileName.isNullOrEmpty()) {
            LogUtil.d(TAG, "$msgString.fileName is null or empty")
            return ""
        }
        val ipAddress = getDeviceIpAddress()
        val tmpUrl = "http://$ipAddress:$SERVER_PORT/$fileName"
        LogUtil.d(TAG, "$msgString.tmpUrl = $tmpUrl")
        return tmpUrl
    }

    // Helper to get local IP address
    fun getDeviceIpAddress(): String? {
        val msgString = "getDeviceIpAddress"
        LogUtil.d(TAG, msgString)
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
            LogUtil.e(TAG, "getLocalIpAddress.Error getting IP address", ex)
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
        // private const val SERVER_PORT = 8080
        private const val SERVER_PORT = 8888
    }
}