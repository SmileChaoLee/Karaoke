package com.smile.karaokeplayer.exoplayer.listeners

import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.exoplayer.services.ExoPlayService

@OptIn(UnstableApi::class)
class CastPlayerListener(private val playService: ExoPlayService)
    : ExoPlayerListener(playService) {

    private val mTAG = "CastPlayerListener"
    init {
        setTAG(mTAG)
        Log.d(mTAG, "CastPlayerListener is created")
    }

    override fun onPlayerPaused() {
        // when onPlayWhenReadyChanged()
        val msgStr = "onPlaybackStateChanged"
        val finishState = playService.presenter?.playingParam?.finishState
            ?: PlayerConstants.FINISHED_NORMALLY
        Log.d(mTAG, "${msgStr}.finishState = $finishState")
        when(finishState) {
            PlayerConstants.STOPPED_BY_USER -> {
                // stopped by PlayerConstants.STOPPED_BY_USER
                // use castPlay.pause() in playService.stop() for carPlayer
                // then no playing next song
                Log.d(mTAG, "${msgStr}.send PlaybackStateCompat.STATE_NONE")
                playService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE)
            }
            else -> {
                // stopped by PlayerConstants.FINISHED_BY_PROGRAM
                // use castPlay.pause() in playService.stop() for carPlayer
                // then playing next song
                Log.d(mTAG, "${msgStr}.send PlaybackStateCompat.STATE_STOPPED")
                playService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
            }
        }
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