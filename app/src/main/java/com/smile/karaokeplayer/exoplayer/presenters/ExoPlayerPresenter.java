package com.smile.karaokeplayer.exoplayer.presenters;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import java.util.ArrayList;
import androidx.annotation.NonNull;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.TrackSelectionParameters;
import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.presenters.PlayerBasePresenter;
import com.smile.karaokeplayer.exoplayer.services.ExoPlayService;

@OptIn(markerClass = UnstableApi.class)
public class ExoPlayerPresenter extends PlayerBasePresenter {

    private static final String TAG = "ExoPlayerPresenter";

    private final ExoPlayerPresentView mPresentView;
    private TrackSelectionParameters mTrackSelectionParameters;
    // instances of the following members have to be saved when configuration changed
    private ArrayList<Integer[]> audioTrackIndicesList = new ArrayList<>();
    private final Handler durationSeekBarHandler = new Handler(Looper.getMainLooper());
    private final Runnable durationSeekBarRunnable = new Runnable() {
        final String msgStr = "durationSeekBarRunnable";
        @Override
        public synchronized void run() {
            durationSeekBarHandler.removeCallbacksAndMessages(null);
            /*
            Log.d(TAG, msgStr + ".getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                int playbackState = getPlayService().getPlaybackState();
                Log.d(TAG, msgStr + ".playbackState = " + playbackState);
                if (getPlayService().getPlayWhenReady()
                        && playbackState != Player.STATE_IDLE
                        && playbackState != Player.STATE_ENDED) {
                    Log.d(TAG, msgStr + ".update_Player_duration_seekbar_progress");
                    mPresentView.update_Player_duration_seekbar_progress(
                            (int) getPlayService().getCurrentPosition());
                }
            }
            */
            if (getPlayService() != null) {
                int playbackState = mPlayingParam.getCurrentPlaybackState();
                Log.d(TAG, msgStr + ".playbackState = " + playbackState);
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    // PlaybackStateCompat.STATE_PLAYING = 3
                    Log.d(TAG, msgStr + ".update_Player_duration_seekbar_progress");
                    mPresentView.update_Player_duration_seekbar_progress(
                            (int) getPlayService().getCurrentPosition());
                }
            }
            durationSeekBarHandler.postDelayed(durationSeekBarRunnable, 1000);
        }
    };

    public interface ExoPlayerPresentView extends BasePresentView {
        void setCurrentPlayerToPlayerView();
    }

    public ExoPlayerPresenter(ExoPlayerPresentView presentView) {
        super(presentView);
        mPresentView = presentView;
        Log.d(TAG, "ExoPlayerPresenter is created");
    }

    public void setCurrentPlayerToPlayerView() {
        mPresentView.setCurrentPlayerToPlayerView();
    }

    public ExoPlayService getPlayService() {
        Log.d(TAG, "getPlayService()");
        return mPresentView.getPlayService() != null?
                (ExoPlayService) (mPresentView.getPlayService()) : null;
    }

    // Begin of override abstract method
    @SuppressWarnings("unchecked")
    @Override
    public void initializeVariables(Bundle savedInstanceState, Intent callingIntent,
                                    boolean isAutoPlay) {
        Log.d(TAG, "initializeVariables");
        initializeVariablesBase(savedInstanceState, callingIntent, isAutoPlay);
        if (savedInstanceState == null) {
            audioTrackIndicesList = new ArrayList<>();
            mTrackSelectionParameters = new TrackSelectionParameters
                    .Builder(getActivity()).build();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                audioTrackIndicesList = (ArrayList<Integer[]>)savedInstanceState.getSerializable(PlayerConstants.AudioTrackIndicesListState, ArrayList.class);
            } else audioTrackIndicesList = (ArrayList<Integer[]>)savedInstanceState.getSerializable(PlayerConstants.AudioTrackIndicesListState);
            if (audioTrackIndicesList == null) audioTrackIndicesList = new ArrayList<>();
            Bundle parameter = savedInstanceState.getBundle(PlayerConstants.TrackSelectionParametersState);
            if (parameter != null) mTrackSelectionParameters = TrackSelectionParameters.fromBundle(parameter);
            else mTrackSelectionParameters = new TrackSelectionParameters
                    .Builder(getActivity()).build();
        }
    }

    public TrackSelectionParameters getTrackSelectionParameters() {
        return mTrackSelectionParameters;
    }

    @Override
    public void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel) {
        int numOfAudioTracks = audioTrackIndicesList.size();
        String msgStr = "setAudioTrackAndChannel";
        Log.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);
        if (numOfAudioTracks > 0) {
            // select audio track
            Log.d(TAG, msgStr + ".audioTrackIndex = " + audioTrackIndex);
            if (audioTrackIndex<=0) {
                Log.d(TAG, msgStr + ".No such audio Track Index = " + audioTrackIndex);
                return;
            }
            if (audioTrackIndex> numOfAudioTracks) {
                Log.d(TAG, msgStr + ".No such audio Track Index = " + audioTrackIndex);
                // set to first track
                audioTrackIndex = 1;
            }
            int indexInArrayList = audioTrackIndex - 1;

            Integer[] trackIndicesCombination = audioTrackIndicesList.get(indexInArrayList);
            Log.d(TAG, msgStr + ".getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                Log.d(TAG, msgStr + ".getPlayService().selectAudioTrack()");
                mTrackSelectionParameters = getPlayService().selectAudioTrack(trackIndicesCombination,
                        mTrackSelectionParameters);
            }

            // set audio track
            Log.d(TAG, msgStr + ".audioTrackIndex = " + audioTrackIndex);
            mPlayingParam.setCurrentAudioTrackIndexPlayed(audioTrackIndex);
            // set audio channel
            Log.d(TAG, msgStr + ".audioChannel = " + audioChannel);
            mPlayingParam.setCurrentChannelPlayed(audioChannel);
            Log.d(TAG, msgStr + ".getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                Log.d(TAG, msgStr + ".getPlayService().setAudioVolume");
                getPlayService().setAudioVolume(mPlayingParam.getCurrentVolume());
            }
        }
    }

    @Override
    public void switchAudioToMusic() {
        Log.d(TAG, "switchAudioToMusic");
        if (!mPlayingParam.isInSongList()) {
            // not in the database and show message
            mPresentView.showMusicAndVocalIsNotSet();
        } else {
            int audioTrack = mPlayingParam.getMusicAudioTrackIndex();
            int audioChannel = mPlayingParam.getMusicAudioChannel();
            setAudioTrackAndChannel(audioTrack, audioChannel);
        }
    }
    @Override
    public void switchAudioToVocal() {
        Log.d(TAG, "switchAudioToVocal");
        if (!mPlayingParam.isInSongList()) {
            // not in the database and show message
            mPresentView.showMusicAndVocalIsNotSet();
        } else {
            int audioTrack = mPlayingParam.getVocalAudioTrackIndex();
            int audioChannel = mPlayingParam.getVocalAudioChannel();
            setAudioTrackAndChannel(audioTrack, audioChannel);
        }
    }

    @Override
    public synchronized void startDurationBarHandler() {
        // start monitor player_duration_seekbar
        // delay 200ms
        durationSeekBarHandler.postDelayed(durationSeekBarRunnable, 1000);
    }

    @Override
    public void removeMsgFromDurationBarHandler() {
        durationSeekBarHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void setAudioActionSubMenu() {
        String msgStr = "setAudioActionSubMenu";
        Log.d(TAG, msgStr + ".getPlayService() = " + getPlayService());
        int[] result = new int[] {1, CommonConstants.STEREO};
        if (getPlayService() == null) {
            return;
        }
        audioTrackIndicesList.clear();
        mNumberOfVideoTracks = getPlayService().getPlayingMediaInfo(audioTrackIndicesList);
        int numOfAudioTracks = audioTrackIndicesList.size();
        Log.d(TAG, msgStr + ".mNumberOfVideoTracks = " + mNumberOfVideoTracks);
        Log.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);

        if (numOfAudioTracks == 0) {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(PlayerConstants.NoAudioTrack);
            mPlayingParam.setCurrentChannelPlayed(PlayerConstants.NoAudioChannel);
        } else {
            int audioChannelPlayed, audioTrackIdPlayed;
            if (mPlayingParam.isAutoPlay() || mPlayingParam.isPlaySingleSong()
                    || mPlayingParam.isInSongList()) {
                audioTrackIdPlayed = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                audioChannelPlayed = mPlayingParam.getCurrentChannelPlayed();
                Log.d(TAG, msgStr + ".Auto play or playing single song.");
            } else {
                // for open media. do not know the music track and vocal track
                Log.d(TAG, msgStr + ".Do not know the music track and vocal track.");
                // guess
                audioTrackIdPlayed = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                Log.d(TAG, msgStr + ".playingParam.getCurrentAudioTrackIndexPlayed() = " +
                        audioTrackIdPlayed);
                audioChannelPlayed = mPlayingParam.getCurrentChannelPlayed();
                Log.d(TAG, msgStr + ".playingParam.getCurrentChannelPlayed() = " +
                        audioChannelPlayed);
                if (numOfAudioTracks >= 2) {
                    // more than 2 audio tracks
                    mPlayingParam.setVocalAudioTrackIndex(audioTrackIdPlayed);
                    mPlayingParam.setVocalAudioChannel(audioChannelPlayed);
                    mPlayingParam.setMusicAudioTrackIndex(audioTrackIdPlayed==1? 2:1);
                    mPlayingParam.setMusicAudioChannel(audioChannelPlayed);
                } else {
                    // only one track
                    audioTrackIdPlayed = 1;
                    mPlayingParam.setCurrentAudioTrackIndexPlayed(audioTrackIdPlayed);
                    mPlayingParam.setVocalAudioTrackIndex(audioTrackIdPlayed);
                    mPlayingParam.setMusicAudioTrackIndex(audioTrackIdPlayed);
                    mPlayingParam.setVocalAudioChannel(CommonConstants.LEFT_CHANNEL);
                    mPlayingParam.setMusicAudioChannel(CommonConstants.RIGHT_CHANNEL);
                }
            }

            Log.d(TAG, msgStr + ".audioTrackIdPlayed = " + audioTrackIdPlayed);
            Log.d(TAG, msgStr + ".audioChannelPlayed = " + audioChannelPlayed);

            if (audioTrackIdPlayed <= 0) {
                audioTrackIdPlayed = 1;
            }
            result[0] = audioTrackIdPlayed;
            result[1] = audioChannelPlayed;
        }
        setAudioTrackAndChannel(result[0], result[1]);
        // build R.id.audioTrack submenu
        Log.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);
        mPresentView.buildAudioTrackMenuItem(numOfAudioTracks);
        // update the duration on controller UI
        Log.d(TAG, msgStr + ".update_Player_duration_seekbar");
        mPresentView.update_Player_duration_seekbar((float)getPlayService().getMediaDuration());

    }

    @Override
    public int getNumberOfAudioTracks() {
        return audioTrackIndicesList.size();
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG,"saveInstanceState.getPlayService()");
        if (getPlayService() != null) {
            mPlayingParam.setCurrentAudioPosition(getPlayService().getCurrentPosition());
        } else {
            mPlayingParam.setCurrentAudioPosition(0);
        }
        outState.putSerializable(PlayerConstants.AudioTrackIndicesListState, audioTrackIndicesList);
        outState.putBundle(PlayerConstants.TrackSelectionParametersState, mTrackSelectionParameters.toBundle());
        super.saveInstanceState(outState);
    }
}
