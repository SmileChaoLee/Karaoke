package com.smile.videoplayer.jni

object VlcNativeBridge {
    init {
        // Load custom native library compiled with NDK/CMake
        System.loadLibrary("vlcbridge")
    }

    // Call this empty method early to trigger class loading
    fun preload() {
        // No-op; accessing this forces the init block to execute
    }

    // Pass the native libvlc_media_player_t pointer obtained via vlcPlayer.getInstance()
    // 1 = Stereo
    // 2 = Reverse Stereo
    // 3 = Left Channel
    // 4 = Right Channel
    // 5 = Dolby
    external fun setNativeAudioChannel(playerInstancePtr: Long, channel: Int): Int
}
