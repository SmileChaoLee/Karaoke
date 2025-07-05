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

import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.presenters.PlayerBasePresenter;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;

import com.smile.karaokeplayer.vlcplayer.services.VlcPlayService;

@OptIn(markerClass = UnstableApi.class)
public class VlcPlayerPresenter extends PlayerBasePresenter {

    private static final String TAG = "VlcPlayerPresenter";

    public interface VlcPresentView extends BasePresentView {
        void setVideoWindowSize();
    }

    private final Handler audioSubMenuHandler = new Handler(Looper.getMainLooper());
    public Handler getAudioSubMenuHandler() {
        return audioSubMenuHandler;
    }
    private class AudioSubMenuRunnable implements Runnable {
        final String msgStr = "AudioSubMenuRunnable";
        final int maxCount = 10;
        int count = 0;
        @Override
        public synchronized void run() {
            Log.d(TAG, msgStr + ".run");
            audioSubMenuHandler.removeCallbacksAndMessages(null);
            Log.d(TAG, msgStr + ".run.count = " + count);
            if (count < maxCount) {
                setAudioActionSubMenu();
                audioSubMenuHandler.postDelayed(this, 2000);
            }
            count++;
        }
    }

    private final VlcPresentView mPresentView;
    // instances of the following members have to be saved when configuration changed
    private ArrayList<Integer> audioTrackIndicesList = new ArrayList<>();

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
    public void setAudioVolumeInsideVolumeSeekBar(int i) {
        Log.d(TAG, "setAudioVolumeInsideVolumeSeekBar");
        float currentVolume = 1.0f;
        if (i < PlayerConstants.MAX_PROGRESS) {
            currentVolume = (float)i / (float)PlayerConstants.MAX_PROGRESS;
        }
        Log.d(TAG, "setAudioVolumeInsideVolumeSeekBar.getPlayService() = " + getPlayService());
        if (getPlayService() != null) {
            getPlayService().setAudioVolume(currentVolume);
        }
    }

    @Override
    public int getCurrentProgressForVolumeSeekBar() {
        int currentProgress;
        float currentVolume = mPlayingParam.getCurrentVolume();
        if ( currentVolume >= 1.0f) {
            currentProgress = PlayerConstants.MAX_PROGRESS;
        } else {
            // percentage of 100
            currentProgress = (int) (currentVolume * PlayerConstants.MAX_PROGRESS);
        }

        return currentProgress;
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
        /*
        int trackIndex;
        int channel;
        if (mNumberOfAudioTracks >= 2) {
            // has more than 2 audio tracks
            trackIndex = mPlayingParam.getCurrentAudioTrackIndexPlayed();
            trackIndex++;
            if (trackIndex > mNumberOfAudioTracks) {
                trackIndex = 1; // the first audio track
            }
            mPlayingParam.setCurrentAudioTrackIndexPlayed(trackIndex);
            mPlayingParam.setCurrentChannelPlayed(CommonConstants.StereoChannel);
        } else {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(1);    // first audio track
            channel = mPlayingParam.getCurrentChannelPlayed();
            if (channel == CommonConstants.LeftChannel) {
                mPlayingParam.setCurrentChannelPlayed(CommonConstants.RightChannel);
            } else {
                mPlayingParam.setCurrentChannelPlayed(CommonConstants.LeftChannel);
            }
        }
        int audioTrack = mPlayingParam.getCurrentAudioTrackIndexPlayed();
        int audioChannel = mPlayingParam.getCurrentChannelPlayed();
        setAudioTrackAndChannel(audioTrack, audioChannel);
        */
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
    public void startDurationSeekBarHandler() {
        // do nothing
    }

    /*
    @Override
    public int[] setAudioActionSubMenu() {
        String msgStr = "setAudioActionSubMenu";
        Log.d(TAG, msgStr);
        int[] result = new int[] {1, CommonConstants.STEREO};
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
                if (count < 10) {
                    handler.postDelayed(this, 2000); // delay 1 seconds
                    count++;
                }
            }
        };
        handler.postDelayed(runnable, 2000); // delay 1 seconds
        // mPresentView.setVideoWindowSize();
        return result;
    }
    */

    @Override
    public int[] setAudioActionSubMenu() {
        String msgStr = "setAudioActionSubMenu";
        Log.d(TAG, msgStr);
        int[] result = new int[] {1, CommonConstants.STEREO};
        if (getPlayService() == null || getPlayService().getVlcPlayer() == null) {
            Log.d(TAG, msgStr + ".getPlayService() = null or  getVlcPlayer()) = null");
            return result;
        }
        MediaPlayer vlcPlayer = getPlayService().getVlcPlayer();

        MediaPlayer.TrackDescription[] videoDis = vlcPlayer.getVideoTracks();
        int videoTrackId;
        String videoTrackName;
        mNumberOfVideoTracks = 0;
        if (videoDis != null) {
            // because it is null sometimes
            for (MediaPlayer.TrackDescription videoDi : videoDis) {
                videoTrackId = videoDi.id;
                videoTrackName = videoDi.name;
                // exclude disabled
                if (videoTrackId >= 0) {
                    // enabled video track
                    mNumberOfVideoTracks++;
                }
            }
        }
        Log.d(TAG, msgStr + ".mNumberOfVideoTracks = " + mNumberOfVideoTracks);

        int audioTrackId;
        String audioTrackName;
        audioTrackIndicesList.clear();
        MediaPlayer.TrackDescription[] audioDis = vlcPlayer.getAudioTracks();
        if (audioDis != null) {
            // because it is null sometimes
            for (MediaPlayer.TrackDescription audioDi : audioDis) {
                audioTrackId = audioDi.id;
                audioTrackName = audioDi.name;
                // exclude disabled
                if (audioTrackId >= 0) {
                    // enabled audio track
                    audioTrackIndicesList.add(audioTrackId);
                }
            }
        }
        int numOfAudioTracks = audioTrackIndicesList.size();
        Log.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);
        if (numOfAudioTracks == 0) {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(PlayerConstants.NoAudioTrack);
            mPlayingParam.setCurrentChannelPlayed(PlayerConstants.NoAudioChannel);
        } else {
            int audioTrackIdPlayed = vlcPlayer.getAudioTrack(); // currently played audio track
            int audioTrackIndex = 1;    // default audio track index
            int audioChannel = CommonConstants.STEREO;
            if (mPlayingParam.isAutoPlay() || mPlayingParam.isPlaySingleSong() || mPlayingParam.isInSongList()) {
                audioTrackIndex = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                audioChannel = mPlayingParam.getCurrentChannelPlayed();
            } else {
                for (int index = 0; index< audioTrackIndicesList.size(); index++) {
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
            }
            result[0] = audioTrackIndex;
            result[1] = audioChannel;
            // setAudioTrackAndChannel(audioTrackIndex, audioChannel);

            IMedia media = vlcPlayer.getMedia();    // for version above 3.3.0
            if (media != null) {
                int trackCount = media.getTracks().length;
                Log.d(TAG, msgStr + ".trackCount = " + trackCount);
                for (int i=0; i<trackCount; i++) {
                    IMedia.Track track = media.getTracks()[i];
                    Log.d(TAG, msgStr + ".track.id = " + track.id);
                    if (track.type == IMedia.Track.Type.Audio) {
                        // audio
                        IMedia.AudioTrack audioTrack = (IMedia.AudioTrack)track;
                        Log.d(TAG, msgStr + ".audioTrack.channels = " + audioTrack.channels);
                    } else if (track.type == IMedia.Track.Type.Video) {
                        // video
                        Media.VideoTrack videoTrack = (Media.VideoTrack)track;
                        int height = videoTrack.height;
                        Log.d(TAG, msgStr + ".videoTrack.height = " + height);
                        int width = videoTrack.width;
                        Log.d(TAG, msgStr + ".videoTrack.width = " + width);
                    }
                }
            }
        }
        // update the duration on controller UI
        // build R.id.audioTrack submenu
        Log.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);
        mPresentView.buildAudioTrackMenuItem(numOfAudioTracks);
        mPresentView.setVideoWindowSize();
        if (getPlayService() != null) {
            mPresentView.update_Player_duration_seekbar(getPlayService().getMediaDuration());
        }
        if (numOfAudioTracks == 0) {
            // trigger get playing media info
            Log.d(TAG, msgStr + ".trigger audioSubMenuHandler");
            audioSubMenuHandler.removeCallbacksAndMessages(null);
            AudioSubMenuRunnable audioRunnable = new AudioSubMenuRunnable();
            audioSubMenuHandler.postDelayed(audioRunnable, 2000);
        } else {
            audioSubMenuHandler.removeCallbacksAndMessages(null);
        }

        return result;
    }

    @Override
    public int getNumberOfAudioTracks() {
        Log.d(TAG, "getNumberOfAudioTracks.audioTrackIndicesList.size() = " +
                audioTrackIndicesList.size());
        return audioTrackIndicesList.size();
    }

    @Override
    public void removeCallbacksAndMessages() {
    }
}
