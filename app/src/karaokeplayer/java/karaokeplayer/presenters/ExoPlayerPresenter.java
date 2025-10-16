package karaokeplayer.presenters;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.session.PlaybackStateCompat;
import java.util.ArrayList;
import androidx.annotation.NonNull;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.TrackSelectionParameters;
import com.smile.karaoke.constants.CommonConstants;
import com.smile.karaoke.constants.PlayerConstants;
import com.smile.karaoke.presenters.PlayerBasePresenter;
import com.smile.karaoke.utilities.LogUtil;

import karaokeplayer.services.ExoPlayService;

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
            if (getPlayService() != null) {
                int playbackState = mPlayingParam.getCurrentPlaybackState();
                LogUtil.d(TAG, msgStr + ".playbackState = " + playbackState);
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    // PlaybackStateCompat.STATE_PLAYING = 3
                    LogUtil.d(TAG, msgStr + ".update_Player_duration_seekbar_progress");
                    mPresentView.update_Player_duration_seekbar_progress(
                            (int) getPlayService().getCurrentPosition());
                }
            }
            durationSeekBarHandler.postDelayed(durationSeekBarRunnable, 1000);
        }
    };

    public interface ExoPlayerPresentView extends BasePresentView {
        void setVideoPlayerView();
        void removeVideoPlayerView();
    }

    public ExoPlayerPresenter(ExoPlayerPresentView presentView) {
        super(presentView);
        mPresentView = presentView;
        LogUtil.i(TAG, "ExoPlayerPresenter is created");
    }

    public ExoPlayerPresentView getPresentView() {
        return mPresentView;
    }

    public ExoPlayService getPlayService() {
        LogUtil.d(TAG, "getPlayService()");
        return mPresentView.getPlayService() != null?
                (ExoPlayService) (mPresentView.getPlayService()) : null;
    }

    // Begin of override abstract method
    @SuppressWarnings("unchecked")
    @Override
    public void initializeVariables(Bundle savedInstanceState, Intent callingIntent,
                                    boolean isAutoPlay) {
        LogUtil.i(TAG, "initializeVariables");
        initializeVariablesBase(savedInstanceState, callingIntent, isAutoPlay);
        if (savedInstanceState == null) {
            audioTrackIndicesList = new ArrayList<>();
            mTrackSelectionParameters = new TrackSelectionParameters
                    .Builder().build();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                audioTrackIndicesList = (ArrayList<Integer[]>)savedInstanceState.getSerializable(PlayerConstants.AudioTrackIndicesListState, ArrayList.class);
            } else audioTrackIndicesList = (ArrayList<Integer[]>)savedInstanceState.getSerializable(PlayerConstants.AudioTrackIndicesListState);
            if (audioTrackIndicesList == null) audioTrackIndicesList = new ArrayList<>();
            Bundle parameter = savedInstanceState.getBundle(PlayerConstants.TrackSelectionParametersState);
            if (parameter != null) mTrackSelectionParameters = TrackSelectionParameters.fromBundle(parameter);
            else mTrackSelectionParameters = new TrackSelectionParameters
                    .Builder().build();
        }
    }

    public TrackSelectionParameters getTrackSelectionParameters() {
        return mTrackSelectionParameters;
    }

    @Override
    public void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel) {
        int numOfAudioTracks = audioTrackIndicesList.size();
        String msgStr = "setAudioTrackAndChannel";
        LogUtil.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);
        if (numOfAudioTracks > 0) {
            // select audio track
            LogUtil.d(TAG, msgStr + ".audioTrackIndex = " + audioTrackIndex);
            if (audioTrackIndex<=0) {
                LogUtil.d(TAG, msgStr + ".No such audio Track Index = " + audioTrackIndex);
                return;
            }
            if (audioTrackIndex> numOfAudioTracks) {
                LogUtil.d(TAG, msgStr + ".No such audio Track Index = " + audioTrackIndex);
                // set to first track
                audioTrackIndex = 1;
            }
            int indexInArrayList = audioTrackIndex - 1;

            Integer[] trackIndicesCombination = audioTrackIndicesList.get(indexInArrayList);
            LogUtil.d(TAG, msgStr + ".getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                LogUtil.d(TAG, msgStr + ".getPlayService().selectAudioTrack()");
                mTrackSelectionParameters = getPlayService().selectAudioTrack(trackIndicesCombination,
                        mTrackSelectionParameters);
            }

            // set audio track
            LogUtil.d(TAG, msgStr + ".audioTrackIndex = " + audioTrackIndex);
            mPlayingParam.setCurrentAudioTrackIndexPlayed(audioTrackIndex);
            // set audio channel
            LogUtil.d(TAG, msgStr + ".audioChannel = " + audioChannel);
            mPlayingParam.setCurrentChannelPlayed(audioChannel);
            LogUtil.d(TAG, msgStr + ".getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                LogUtil.d(TAG, msgStr + ".getPlayService().setAudioVolume");
                getPlayService().setAudioVolume(mPlayingParam.getCurrentVolume());
            }
        }
    }

    @Override
    public void switchAudioToMusic() {
        LogUtil.i(TAG, "switchAudioToMusic");
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
        LogUtil.i(TAG, "switchAudioToVocal");
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
        LogUtil.d(TAG, msgStr + ".getPlayService() = " + getPlayService());
        int[] result = new int[] {1, CommonConstants.STEREO};
        if (getPlayService() == null) {
            return;
        }
        audioTrackIndicesList.clear();
        mNumberOfVideoTracks = getPlayService().getPlayingMediaInfo(audioTrackIndicesList);
        int numOfAudioTracks = audioTrackIndicesList.size();
        LogUtil.d(TAG, msgStr + ".mNumberOfVideoTracks = " + mNumberOfVideoTracks);
        LogUtil.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);

        if (numOfAudioTracks == 0) {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(PlayerConstants.NoAudioTrack);
            mPlayingParam.setCurrentChannelPlayed(PlayerConstants.NoAudioChannel);
        } else {
            int audioChannelPlayed, audioTrackIdPlayed;
            if (mPlayingParam.isAutoPlay() || mPlayingParam.isPlaySingleSong()
                    || mPlayingParam.isInSongList()) {
                audioTrackIdPlayed = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                audioChannelPlayed = mPlayingParam.getCurrentChannelPlayed();
                LogUtil.d(TAG, msgStr + ".Auto play or playing single song.");
            } else {
                // for open media. do not know the music track and vocal track
                LogUtil.d(TAG, msgStr + ".Do not know the music track and vocal track.");
                // guess
                audioTrackIdPlayed = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                LogUtil.d(TAG, msgStr + ".playingParam.getCurrentAudioTrackIndexPlayed() = " +
                        audioTrackIdPlayed);
                audioChannelPlayed = mPlayingParam.getCurrentChannelPlayed();
                LogUtil.d(TAG, msgStr + ".playingParam.getCurrentChannelPlayed() = " +
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

            LogUtil.d(TAG, msgStr + ".audioTrackIdPlayed = " + audioTrackIdPlayed);
            LogUtil.d(TAG, msgStr + ".audioChannelPlayed = " + audioChannelPlayed);

            if (audioTrackIdPlayed <= 0) {
                audioTrackIdPlayed = 1;
            }
            result[0] = audioTrackIdPlayed;
            result[1] = audioChannelPlayed;
        }
        setAudioTrackAndChannel(result[0], result[1]);
        // build R.id.audioTrack submenu
        LogUtil.d(TAG, msgStr + ".numOfAudioTracks = " + numOfAudioTracks);
        mPresentView.buildAudioTrackMenuItem(numOfAudioTracks);
        // update the duration on controller UI
        LogUtil.d(TAG, msgStr + ".update_Player_duration_seekbar");
        mPresentView.update_Player_duration_seekbar((float)getPlayService().getMediaDuration());

    }

    @Override
    public int getNumberOfAudioTracks() {
        return audioTrackIndicesList.size();
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        LogUtil.i(TAG,"saveInstanceState.getPlayService()");
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
