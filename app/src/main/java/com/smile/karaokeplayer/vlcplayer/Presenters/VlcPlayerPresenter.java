package com.smile.karaokeplayer.vlcplayer.Presenters;

import java.util.ArrayList;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import android.support.v4.media.session.PlaybackStateCompat;

import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.presenters.PlayerBasePresenter;
import com.smile.karaokeplayer.vlcplayer.services.VlcPlayService;

@OptIn(markerClass = UnstableApi.class)
public class VlcPlayerPresenter extends PlayerBasePresenter {

    private static final String TAG = "VlcPlayerPresenter";

    public interface VlcPresentView extends BasePresentView {
        void setVideoWindowSize();
    }

    private final VlcPresentView mPresentView;
    // instances of the following members have to be saved when configuration changed
    private ArrayList<Integer> audioTrackIndicesList = new ArrayList<>();

    private final Handler durationBarHandler = new Handler(Looper.getMainLooper());
    private final Runnable durationBarRunnable = new Runnable() {
        final String msgStr = "durationSeekBarRunnable";
        @Override
        public synchronized void run() {
            durationBarHandler.removeCallbacksAndMessages(null);
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
            durationBarHandler.postDelayed(durationBarRunnable, 1000);
        }
    };

    public VlcPlayerPresenter(VlcPresentView presentView) {
        super(presentView);
        mPresentView = presentView;
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public VlcPresentView getPresentView() {
        return mPresentView;
    }

    public VlcPlayService getPlayService() {
        Log.d(TAG, "getPlayService()");
        return mPresentView.getPlayService() != null?
                (VlcPlayService) (mPresentView.getPlayService()) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeVariables(Bundle savedInstanceState, Intent callingIntent,
                                    boolean isAutoPlay) {
        Log.d(TAG, "initializeVariables");
        initializeVariablesBase(savedInstanceState, callingIntent, isAutoPlay);
        if (savedInstanceState == null) {
            audioTrackIndicesList = new ArrayList<>();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                audioTrackIndicesList = (ArrayList<Integer>)savedInstanceState.getSerializable(PlayerConstants.AudioTrackIndicesListState, ArrayList.class);
            } else audioTrackIndicesList = (ArrayList<Integer>)savedInstanceState.getSerializable(PlayerConstants.AudioTrackIndicesListState);
            if (audioTrackIndicesList == null) audioTrackIndicesList = new ArrayList<>();
        }
    }

    @Override
    public void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel) {
        int numOfAudioTracks = audioTrackIndicesList.size();
        Log.d(TAG, "setAudioTrackAndChannel.audioTrackIndex = " + audioTrackIndex +
                ", audioChannel = " + audioChannel + ", numOfAudioTracks = " +
                numOfAudioTracks);
        if (audioTrackIndex <= 0) {
            return;
        }
        if (numOfAudioTracks > 0) {
            // select audio track
            if (audioTrackIndex > numOfAudioTracks) {
                // set to first track
                audioTrackIndex = 1;
            }
            int audioTrackId = audioTrackIndicesList.get(audioTrackIndex - 1);
            Log.d(TAG, "setAudioTrackAndChannel.getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                getPlayService().setAudioTrack(audioTrackId);
                mPlayingParam.setCurrentAudioTrackIndexPlayed(audioTrackIndex);
                // select audio channel
                mPlayingParam.setCurrentChannelPlayed(audioChannel);
                getPlayService().setAudioVolume(mPlayingParam.getCurrentVolume());
            }
        }
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "saveInstanceState.getPlayService() = " + getPlayService());
        if (getPlayService() != null) {
            if (getPlayService().getVlcPlayer() != null) {
                mPlayingParam.setCurrentAudioPosition(getPlayService().getVlcPlayer().getTime());
            } else {
                mPlayingParam.setCurrentAudioPosition(0);
            }
        }
        outState.putIntegerArrayList("AudioTrackIndexList", audioTrackIndicesList);

        super.saveInstanceState(outState);
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
        // do nothing because it does not have this functionality yet
        Log.d(TAG, "switchAudioToVocal() is called.");
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
    public void startDurationBarHandler() {
        String msgStr = "startDurationBarHandler";
        // start monitor player_duration_seekbar
        // delay 200ms
        durationBarHandler.postDelayed(durationBarRunnable, 1000);
    }

    @Override
    public void removeMsgFromDurationBarHandler() {
        String msgStr = "removeMsgFromDurationBarHandler";
        durationBarHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public int[] setAudioActionSubMenu() {
        String msgStr = "setAudioActionSubMenu";
        Log.d(TAG, msgStr);
        final int[] result = getPlayingMediaInfo();
        if (audioTrackIndicesList.isEmpty()) {
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable runnable = new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    handler.removeCallbacksAndMessages(null);
                    Log.d(TAG, msgStr + ".runnable.count = " + count);
                    int[] tempResult = getPlayingMediaInfo();
                    result[0] = tempResult[0];
                    result[1] = tempResult[1];
                    // mPresentView.showNativeAndHideBannerAd();
                    mPresentView.setVideoWindowSize();
                    if (audioTrackIndicesList.isEmpty()) {
                        if (count < 2) {
                            handler.postDelayed(this, 2000); // delay 2 seconds
                            count++;
                        }
                    } else {
                        handler.removeCallbacksAndMessages(null);
                        Log.d(TAG, msgStr + ".audioTrackIndicesList not empty");
                    }
                }
            };
            handler.postDelayed(runnable, 1000); // delay 1 seconds
        }
        // mPresentView.setVideoWindowSize();
        return result;
    }

    public int[] getPlayingMediaInfo() {
        String msgStr = "getPlayingMediaInfo";
        Log.d(TAG, msgStr);
        int[] result = new int[] {1, CommonConstants.STEREO};
        mNumberOfVideoTracks = 0;
        int numOfAudioTracks = 0;
        audioTrackIndicesList.clear();
        if (getPlayService() == null || getPlayService().getVlcPlayer() == null) {
            Log.d(TAG, msgStr + ".getPlayService() or vlcPlayer is null");
        } else {
            mNumberOfVideoTracks = getPlayService().getPlayingMediaInfo(audioTrackIndicesList);
            numOfAudioTracks = audioTrackIndicesList.size();
            Log.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);
            if (numOfAudioTracks == 0) {
                mPlayingParam.setCurrentAudioTrackIndexPlayed(PlayerConstants.NoAudioTrack);
                mPlayingParam.setCurrentChannelPlayed(PlayerConstants.NoAudioChannel);
            } else {
                // currently played audio track
                int audioTrackIdPlayed = getPlayService().getAudioTrack();
                int audioTrackIndex = 1;    // default audio track index
                int audioChannel = CommonConstants.STEREO;
                if (mPlayingParam.isAutoPlay() || mPlayingParam.isPlaySingleSong()
                        || mPlayingParam.isInSongList()) {
                    audioTrackIndex = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                    audioChannel = mPlayingParam.getCurrentChannelPlayed();
                } else {
                    for (int index = 0; index < audioTrackIndicesList.size(); index++) {
                        int audioId = audioTrackIndicesList.get(index);
                        if (audioId == audioTrackIdPlayed) {
                            audioTrackIndex = index + 1;
                            break;
                        }
                    }
                    // for open media. do not know the music track and vocal track
                    // guess
                    audioTrackIdPlayed = 1;
                    int musicAudioTrack;
                    if (numOfAudioTracks >= 2) {
                        // more than 2 audio tracks
                        musicAudioTrack = 2; // default music is the second track
                    } else {
                        // only one track
                        musicAudioTrack = 1;
                    }
                    mPlayingParam.setVocalAudioTrackIndex(audioTrackIdPlayed);
                    mPlayingParam.setVocalAudioChannel(audioChannel);
                    // default music is the second track
                    mPlayingParam.setMusicAudioTrackIndex(musicAudioTrack);
                    mPlayingParam.setMusicAudioChannel(audioChannel);
                    mPlayingParam.setCurrentAudioTrackIndexPlayed(audioTrackIdPlayed);
                    mPlayingParam.setCurrentChannelPlayed(audioChannel);
                }
                result[0] = audioTrackIndex;
                result[1] = audioChannel;
            }
        }
        // update the duration on controller UI
        // build R.id.audioTrack submenu
        Log.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);
        mPresentView.buildAudioTrackMenuItem(numOfAudioTracks);
        mPresentView.setVideoWindowSize();
        mPresentView.update_Player_duration_seekbar(getPlayService().getMediaDuration());

        return result;
    }

    @Override
    public int getNumberOfAudioTracks() {
        Log.d(TAG, "getNumberOfAudioTracks.audioTrackIndicesList.size() = " +
                audioTrackIndicesList.size());
        return audioTrackIndicesList.size();
    }
}
