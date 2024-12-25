package videoplayer.Presenters;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.presenters.BasePlayerPresenter;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;

import java.util.ArrayList;

import videoplayer.fragments.VlcPlayerFragment;
import videoplayer.services.VlcPlayService;

public class VlcPlayerPresenter extends BasePlayerPresenter {

    private static final String TAG = "VlcPlayerPresenter";

    public interface VlcPresentView extends BasePlayerPresenter.BasePresentView {
        void setVideoWindowSize();
    }
    // private final VlcPresentView mVlcView;
    private final VlcPresentView mPresentView;
    // private MediaPlayer vlcPlayer;
    // instances of the following members have to be saved when configuration changed
    private ArrayList<Integer> audioTrackIndicesList = new ArrayList<>();

    public VlcPlayerPresenter(VlcPlayerFragment fragment, VlcPresentView presentView) {
        super(fragment, presentView);
        // mActivity = fragment.getActivity();
        mPresentView = presentView;
        // mVlcView = vlcView;
        // set volume control stream to STREAM_MUSIC
        mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
    public void initializeVariables(Bundle savedInstanceState, Intent callingIntent) {
        Log.d(TAG, "initializeVariables");
        initializeVariablesBase(savedInstanceState, callingIntent);
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
        if (i < PlayerConstants.MaxProgress) {
            currentVolume = (float)i / (float)PlayerConstants.MaxProgress;
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
            currentProgress = PlayerConstants.MaxProgress;
        } else {
            // percentage of 100
            currentProgress = (int) (currentVolume * PlayerConstants.MaxProgress);
        }

        return currentProgress;
    }

    @Override
    public void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel) {
        Log.d(TAG, "setAudioTrackAndChannel.audioTrackIndex = " + audioTrackIndex +
                ", audioChannel = " + audioChannel + ", numberOfAudioTracks = " +
                mNumberOfAudioTracks);
        if (audioTrackIndex <= 0) {
            return;
        }
        if (mNumberOfAudioTracks > 0) {
            // select audio track
            if (audioTrackIndex > mNumberOfAudioTracks) {
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
        // do nothing because no need
    }

    @Override
    public void setAudioActionSubMenu() {
        Log.d(TAG, "setAudioActionSubMenu");
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable runnable = () -> {
            Log.d(TAG, "getMediaInfoSetAudioSubMenu().run()");
            handler.removeCallbacksAndMessages(null);
            getMediaInfoSetAudioSubMenu();
            mPresentView.showNativeAndHideBannerAd();
        };
        handler.postDelayed(runnable, 1000); // delay 1 seconds
    }

    private void getMediaInfoSetAudioSubMenu() {
        Log.d(TAG, "getMediaInfoSetAudioSubMenu");
        if (getPlayService() == null || getPlayService().getVlcPlayer() == null) {
            Log.d(TAG, "getMediaInfoSetAudioSubMenu.getPlayService() = null or  getVlcPlayer()) = null");
            return;
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
                Log.d(TAG, "getMediaInfoSetAudioSubMenu.videoDis[i].id = " + videoTrackId);
                Log.d(TAG, "getMediaInfoSetAudioSubMenu.videoDis[i].name = " + videoTrackName);
                // exclude disabled
                if (videoTrackId >= 0) {
                    // enabled video track
                    mNumberOfVideoTracks++;
                }
            }
        }
        Log.d(TAG, "getMediaInfoSetAudioSubMenu.mNumberOfVideoTracks = " + mNumberOfVideoTracks);

        //
        int audioTrackId;
        String audioTrackName;
        audioTrackIndicesList.clear();
        MediaPlayer.TrackDescription[] audioDis = vlcPlayer.getAudioTracks();
        if (audioDis != null) {
            // because it is null sometimes
            for (MediaPlayer.TrackDescription audioDi : audioDis) {
                audioTrackId = audioDi.id;
                audioTrackName = audioDi.name;
                Log.d(TAG, "getMediaInfoSetAudioSubMenu.audioDis[i].id = " + audioTrackId);
                Log.d(TAG, "getMediaInfoSetAudioSubMenu.audioDis[i].name = " + audioTrackName);
                // exclude disabled
                if (audioTrackId >= 0) {
                    // enabled audio track
                    audioTrackIndicesList.add(audioTrackId);
                }
            }
        }
        mNumberOfAudioTracks = audioTrackIndicesList.size();
        Log.d(TAG, "getMediaInfoSetAudioSubMenu.numberOfAudioTracks = " + mNumberOfAudioTracks);
        if (mNumberOfAudioTracks == 0) {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(PlayerConstants.NoAudioTrack);
            mPlayingParam.setCurrentChannelPlayed(PlayerConstants.NoAudioChannel);
        } else {
            int audioTrackIdPlayed = vlcPlayer.getAudioTrack(); // currently played audio track
            Log.d(TAG, "getMediaInfoSetAudioSubMenu.getAudioTrack() = " + audioTrackIdPlayed);
            Log.d(TAG, "getMediaInfoSetAudioSubMenu.audioTrackIdPlayed = " + audioTrackIdPlayed);
            int audioTrackIndex = 1;    // default audio track index
            int audioChannel = CommonConstants.StereoChannel;
            if (mPlayingParam.isAutoPlay() || mPlayingParam.isPlaySingleSong() || mPlayingParam.isInSongList()) {
                audioTrackIndex = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                audioChannel = mPlayingParam.getCurrentChannelPlayed();
                Log.d(TAG, "getMediaInfoSetAudioSubMenu.Auto play or playing single song.");
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
                if (mNumberOfAudioTracks >= 2) {
                    // more than 2 audio tracks
                    musicAudioTrack = 2; // default music is the second track
                } else {
                    // only one track
                    musicAudioTrack = 1;
                }
                mPlayingParam.setVocalAudioTrackIndex(audioTrackIdPlayed);
                mPlayingParam.setVocalAudioChannel(audioChannel);
                mPlayingParam.setMusicAudioTrackIndex(musicAudioTrack);    // default music is the second track
                mPlayingParam.setMusicAudioChannel(audioChannel);
            }
            setAudioTrackAndChannel(audioTrackIndex, audioChannel);

            // build R.id.audioTrack submenu
            mPresentView.buildAudioTrackMenuItem(audioTrackIndicesList.size());

            // for testing
            // Media media = vlcPlayer.getMedia();  // for version 3.1.12
            IMedia media = vlcPlayer.getMedia();    // for version above 3.3.0
            // int trackCount = media.getTrackCount();
            int trackCount = media.getTracks().length;
            Log.d(TAG, "getMediaInfoSetAudioSubMenu.trackCount = " + trackCount);
            for (int i=0; i<trackCount; i++) {
                // Media.Track track = media.getTrack(i);   // for version 3.1.12
                // if (track.type == Media.Track.Type.Audio) {  // for version 3.1.12
                // IMedia.Track track = media.getTrack(i);
                IMedia.Track track = media.getTracks()[i];
                Log.d(TAG, "getMediaInfoSetAudioSubMenu.track.id = " + track.id);
                if (track.type == IMedia.Track.Type.Audio) {
                    // audio
                    // Media.AudioTrack audioTrack = (Media.AudioTrack)track;
                    IMedia.AudioTrack audioTrack = (IMedia.AudioTrack)track;
                    Log.d(TAG, "getMediaInfoSetAudioSubMenu.audioTrack.channels = " + audioTrack.channels);
                    Log.d(TAG, "getMediaInfoSetAudioSubMenu.audioTrack.rate = " + audioTrack.rate);
                } else if (track.type == IMedia.Track.Type.Video) {
                // } else if (track.type == Media.Track.Type.Video) {
                    // IMedia.VideoTrack videoTrack = (IMedia.VideoTrack)track;
                    Media.VideoTrack videoTrack = (Media.VideoTrack)track;
                    int height = videoTrack.height;
                    Log.d(TAG, "getMediaInfoSetAudioSubMenu.videoTrack.height = " + height);
                    int width = videoTrack.width;
                    Log.d(TAG, "getMediaInfoSetAudioSubMenu.videoTrack.width = " + width);
                }
            }
            //
        }
        // update the duration on controller UI
        mPresentView.setVideoWindowSize();
        mPresentView.update_Player_duration_seekbar(vlcPlayer.getLength());
    }

    @Override
    public void removeCallbacksAndMessages() {
    }
}
