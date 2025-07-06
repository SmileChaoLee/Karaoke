package com.smile.karaokeplayer.exoplayer.listeners

// import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.smile.karaokeplayer.exoplayer.services.ExoPlayService

@OptIn(UnstableApi::class)
class CastPlayerListener(private val playService: ExoPlayService)
    : ExoPlayerListener(playService) {

    private val mTAG = "CastPlayerListener"
    init {
        setTAG(mTAG)
        Log.d(mTAG, "CastPlayerListener is created")
    }

    override fun onTracksChanged(tracks: Tracks) {
        super.onTracksChanged(tracks)
        val msgStr = "onTracksChanged"
        Log.d(mTAG, msgStr)
        if (playService.castPlayer == null) return
        val cPlayer = playService.castPlayer!!
        val currentTracks: Tracks? = cPlayer.currentTracks
        Log.d(mTAG, "${msgStr}.currentTacks = $currentTracks")
    }

    override fun onEvents(player: Player, events: Player.Events) {
        val msgStr = "onEvents"
        if (events.contains(Player.EVENT_TRACKS_CHANGED)
            || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
            val tracks = player.currentTracks
            Log.d(mTAG, "${msgStr}.onEvents.tracks = $tracks")
            tracks.apply {
                Log.d(mTAG, "${msgStr}.onEvents.tracks.groups.size" +
                        " = ${tracks.groups.size}")
            }
            // Now you can safely access currentTracks
        }
    }
}