package com.smile.videoplayer.native

object VlcNativeBridge {
    const val STEREO = 1
    const val REVERSE_STEREO = 2
    const val LEFT_CHANNEL = 3
    const val RIGHT_CHANNEL = 4
    const val DOLBY = 5
    fun loadVlcBridge() {
        // Load custom native library compiled with NDK/CMake
        System.loadLibrary("vlcbridge")
    }

    // Pass the native libvlc_media_player_t pointer obtained via vlcPlayer.getInstance()
    // 1 = Stereo
    // 2 = Reverse Stereo
    // 3 = Left Channel
    // 4 = Right Channel
    // 5 = Dolby
    // This instructs the native C++ audio sink to re-route structural layouts instantly
    // Mode 1: Plays both tracks cleanly (Stereo)
    // Mode 3: Routes Left stream data to both left and right ears
    // Mode 4: Routes Right stream data to both left and right ears
    external fun setNativeAudioChannel(playerInstancePtr: Long, channel: Int): Int
}
