package com.smile.karaokeplayer.listeners

import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaokeplayer.services.ExoPlayService

@OptIn(UnstableApi::class)
class CastPlayerListener(private val playService: ExoPlayService)
    : ExoPlayerListener(playService) {

    private val mTAG = "CastPlayerListener"
    init {
        setTAG(mTAG)
        LogUtil.d(mTAG, "CastPlayerListener is created")
    }

    override fun onPlayerPaused() {
        // when onPlayWhenReadyChanged()
        val msgStr = "onPlaybackStateChanged"
        val finishState = playService.presenter?.playingParam?.finishState
            ?: MyPlayerConstants.FINISHED_NORMALLY
        LogUtil.d(mTAG, "${msgStr}.finishState = $finishState")
        when(finishState) {
            MyPlayerConstants.STOPPED_BY_USER -> {
                // stopped by PlayerConstants.STOPPED_BY_USER
                // use castPlay.pause() in playService.stop() for carPlayer
                // then no playing next song
                LogUtil.d(mTAG, "${msgStr}.send PlaybackStateCompat.STATE_NONE")
                playService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE)
            }
            else -> {
                // stopped by PlayerConstants.FINISHED_BY_PROGRAM
                // use castPlay.pause() in playService.stop() for carPlayer
                // then playing next song
                LogUtil.d(mTAG, "${msgStr}.send PlaybackStateCompat.STATE_STOPPED")
                playService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
            }
        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        super.onTracksChanged(tracks)
        val msgStr = "onTracksChanged"
        LogUtil.d(mTAG, msgStr)
        if (playService.castPlayer == null) return
        val cPlayer = playService.castPlayer!!
        val currentTracks: Tracks? = cPlayer.currentTracks
        LogUtil.d(mTAG, "${msgStr}.currentTacks = $currentTracks")
    }

    override fun onEvents(player: Player, events: Player.Events) {
        val msgStr = "onEvents"
        if (events.contains(Player.EVENT_TRACKS_CHANGED)
            || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
            val tracks = player.currentTracks
            LogUtil.d(mTAG, "${msgStr}.onEvents.tracks = $tracks")
            tracks.apply {
                LogUtil.d(mTAG, "${msgStr}.onEvents.tracks.groups.size" +
                        " = ${tracks.groups.size}")
            }
            // Now you can safely access currentTracks
        }
    }
}