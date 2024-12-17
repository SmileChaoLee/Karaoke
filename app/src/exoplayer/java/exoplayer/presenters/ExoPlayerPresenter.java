package exoplayer.presenters;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.gms.cast.framework.CastContext;
import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.presenters.BasePlayerPresenter;

import exoplayer.ExoPlayerActivity;
import exoplayer.fragments.ExoPlayerFragment;
import exoplayer.services.ExoPlayService;

public class ExoPlayerPresenter extends BasePlayerPresenter {

    private static final String TAG = "ExoPlayerPresenter";

    private final ExoPlayerFragment mFragment;
    private final ExoPlayerPresentView mPresentView;
    private TrackSelectionParameters mTrackSelectionParameters;
    private int mCurrentItemIndex = -1;
    // instances of the following members have to be saved when configuration changed
    private ArrayList<Integer[]> audioTrackIndicesList = new ArrayList<>();
    private final Handler durationSeekBarHandler = new Handler(Looper.getMainLooper());
    private final Runnable durationSeekBarRunnable = new Runnable() {
        @Override
        public synchronized void run() {
            Log.d(TAG, "durationSeekBarRunnable.run()");
            durationSeekBarHandler.removeCallbacksAndMessages(null);
            Log.d(TAG, "durationSeekBarRunnable.run().getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                int playbackState = getPlayService().getPlaybackState();
                Log.d(TAG, "durationSeekBarRunnable.run().getPlayWhenReady = "
                        + getPlayService().getPlayWhenReady());
                Log.d(TAG, "durationSeekBarRunnable.run().playbackState = " + playbackState);
                if (getPlayService().getPlayWhenReady()
                        && playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                    Log.d(TAG, "durationSeekBarRunnable.run().update_Player_duration_seekbar_progress");
                    mPresentView.update_Player_duration_seekbar_progress((int) getPlayService().getCurrentPosition());
                }
            }
            durationSeekBarHandler.postDelayed(durationSeekBarRunnable, 500);
        }
    };

    public interface ExoPlayerPresentView extends BasePlayerPresenter.BasePresentView {
        void setCurrentPlayerToPlayerView();
    }

    public ExoPlayerPresenter(ExoPlayerFragment fragment, ExoPlayerPresentView presentView) {
        super(fragment, presentView);
        mFragment = fragment;
        // mActivity = mFragment.getActivity();
        mPresentView = presentView;
        Log.d(TAG, "ExoPlayerPresenter is created");
    }

    public ExoPlayerFragment getFragment() {
        return mFragment;
    }
    public int getCurrentItemIndex() {
        return mCurrentItemIndex;
    }
    public void setCurrentItemIndex(int currentItemIndex) {
        mCurrentItemIndex = currentItemIndex;
    }
    public void setCurrentPlayerToPlayerView() {
        mPresentView.setCurrentPlayerToPlayerView();
    }

    public CastContext getCastContext() {
        ExoPlayerActivity activity = (ExoPlayerActivity)mActivity;
        Log.d(TAG, "getCastContext().activity = " + activity);
        if (activity == null) {
            return null;
        }
        return activity.getCastContext();
    }

    public ExoPlayService getPlayService() {
        Log.d(TAG, "getPlayService()");
        return mPresentView.getPlayService() != null?
                (ExoPlayService) (mPresentView.getPlayService()) : null;
    }

    // Begin of override abstract method
    @SuppressWarnings("unchecked")
    @Override
    public void initializeVariables(Bundle savedInstanceState, Intent callingIntent) {
        Log.d(TAG, "initializeVariables()");
        initializeVariablesBase(savedInstanceState, callingIntent);
        if (savedInstanceState == null) {
            audioTrackIndicesList = new ArrayList<>();
            mTrackSelectionParameters = new TrackSelectionParameters.Builder(mActivity).build();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                audioTrackIndicesList = (ArrayList<Integer[]>)savedInstanceState.getSerializable(PlayerConstants.AudioTrackIndicesListState, ArrayList.class);
            } else audioTrackIndicesList = (ArrayList<Integer[]>)savedInstanceState.getSerializable(PlayerConstants.AudioTrackIndicesListState);
            if (audioTrackIndicesList == null) audioTrackIndicesList = new ArrayList<>();
            Bundle parameter = savedInstanceState.getBundle(PlayerConstants.TrackSelectionParametersState);
            if (parameter != null) mTrackSelectionParameters = TrackSelectionParameters.fromBundle(parameter);
            else mTrackSelectionParameters = new TrackSelectionParameters.Builder(mActivity).build();
        }
    }

    public TrackSelectionParameters getTrackSelectionParameters() {
        return mTrackSelectionParameters;
    }

    @Override
    public void setAudioVolumeInsideVolumeSeekBar(int i) {
        Log.d(TAG, "setAudioVolumeInsideVolumeSeekBar");
        float currentVolume = 1.0f;
        if (i < PlayerConstants.MaxProgress) {
            currentVolume = (float)(1.0f - (Math.log(PlayerConstants.MaxProgress - i)
                    / Math.log(PlayerConstants.MaxProgress)));
        }
        Log.d(TAG, "setAudioVolumeInsideVolumeSeekBar.getPlayService()" + getPlayService());
        if (getPlayService() != null) {
            Log.d(TAG, "setAudioVolumeInsideVolumeSeekBar.getPlayService().setAudioVolume()");
            getPlayService().setAudioVolume(currentVolume);
        }
    }

    @Override
    public int getCurrentProgressForVolumeSeekBar() {
        Log.d(TAG, "getCurrentProgressForVolumeSeekBar");
        int currentProgress;
        float currentVolume = mPlayingParam.getCurrentVolume();
        if ( currentVolume >= 1.0f) {
            currentProgress = PlayerConstants.MaxProgress;
        } else {
            currentProgress = PlayerConstants.MaxProgress
                    - (int)Math.pow(PlayerConstants.MaxProgress, (1-currentVolume));
            currentProgress = Math.max(0, currentProgress);
        }
        return currentProgress;
    }

    @Override
    public void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel) {
        Log.d(TAG, "setAudioTrackAndChannel().numberOfAudioTracks = " + mNumberOfAudioTracks);
        if (mNumberOfAudioTracks > 0) {
            // select audio track
            Log.d(TAG, "setAudioTrackAndChannel().audioTrackIndex = " + audioTrackIndex);
            if (audioTrackIndex<=0) {
                Log.d(TAG, "No such audio Track Index = " + audioTrackIndex);
                return;
            }
            if (audioTrackIndex> mNumberOfAudioTracks) {
                Log.d(TAG, "No such audio Track Index = " + audioTrackIndex);
                // set to first track
                audioTrackIndex = 1;
            }
            int indexInArrayList = audioTrackIndex - 1;

            Integer[] trackIndicesCombination = audioTrackIndicesList.get(indexInArrayList);
            Log.d(TAG, "setAudioTrackAndChannel.getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                Log.d(TAG, "setAudioTrackAndChannel.getPlayService().selectAudioTrack()");
                mTrackSelectionParameters = getPlayService().selectAudioTrack(trackIndicesCombination,
                        mTrackSelectionParameters);
            }

            // set audio track
            Log.d(TAG, "setAudioTrackAndChannel.audioTrackIndex = " + audioTrackIndex);
            mPlayingParam.setCurrentAudioTrackIndexPlayed(audioTrackIndex);
            // set audio channel
            Log.d(TAG, "setAudioTrackAndChannel.audioChannel = " + audioChannel);
            mPlayingParam.setCurrentChannelPlayed(audioChannel);
            Log.d(TAG, "setAudioTrackAndChannel.getPlayService() = " + getPlayService());
            if (getPlayService() != null) {
                Log.d(TAG, "setAudioTrackAndChannel.getPlayService().setAudioVolume");
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
    public synchronized void startDurationSeekBarHandler() {
        // start monitor player_duration_seekbar
        durationSeekBarHandler.postDelayed(durationSeekBarRunnable, 200); // delay 200ms
    }

    @Override
    public void removeCallbacksAndMessages() {
        durationSeekBarHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void setAudioActionSubMenu() {
        Log.d(TAG, "setAudioActionSubMenu.getPlayService() = " + getPlayService());
        if (getPlayService() == null) {
            return;
        }
        audioTrackIndicesList.clear();
        mNumberOfVideoTracks = getPlayService().getPlayingMediaInfo(audioTrackIndicesList);
        mNumberOfAudioTracks = audioTrackIndicesList.size();
        Log.d(TAG, "setAudioActionSubMenu.mNumberOfVideoTracks = " + mNumberOfVideoTracks);
        Log.d(TAG, "setAudioActionSubMenu.mNumberOfAudioTracks = " + mNumberOfAudioTracks);

        if (mNumberOfAudioTracks == 0) {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(PlayerConstants.NoAudioTrack);
            mPlayingParam.setCurrentChannelPlayed(PlayerConstants.NoAudioChannel);
        } else {
            int audioChannelPlayed, audioTrackIdPlayed;
            if (mPlayingParam.isAutoPlay() || mPlayingParam.isPlaySingleSong() || mPlayingParam.isInSongList()) {
                audioTrackIdPlayed = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                audioChannelPlayed = mPlayingParam.getCurrentChannelPlayed();
                Log.d(TAG, "Auto play or playing single song.");
            } else {
                // for open media. do not know the music track and vocal track
                Log.d(TAG, "Do not know the music track and vocal track.");
                // guess
                audioTrackIdPlayed = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                Log.d(TAG, "setAudioActionSubMenu.playingParam.getCurrentAudioTrackIndexPlayed() = " +
                        audioTrackIdPlayed);
                audioChannelPlayed = mPlayingParam.getCurrentChannelPlayed();
                Log.d(TAG, "setAudioActionSubMenu.playingParam.getCurrentChannelPlayed() = " +
                        audioChannelPlayed);
                if (mNumberOfAudioTracks >= 2) {
                    // more than 2 audio tracks
                    mPlayingParam.setVocalAudioTrackIndex(audioTrackIdPlayed);
                    mPlayingParam.setVocalAudioChannel(audioChannelPlayed);
                    mPlayingParam.setMusicAudioTrackIndex(audioTrackIdPlayed==1? 2:1);
                    mPlayingParam.setMusicAudioChannel(audioChannelPlayed);
                } else {
                    // only one track
                    audioTrackIdPlayed = 1;
                    mPlayingParam.setVocalAudioTrackIndex(audioTrackIdPlayed);
                    mPlayingParam.setMusicAudioTrackIndex(audioTrackIdPlayed);
                    mPlayingParam.setVocalAudioChannel(CommonConstants.LeftChannel);
                    mPlayingParam.setMusicAudioChannel(CommonConstants.RightChannel);
                }
            }

            Log.d(TAG, "audioTrackIdPlayed = " + audioTrackIdPlayed);
            Log.d(TAG, "audioChannelPlayed = " + audioChannelPlayed);

            if (audioTrackIdPlayed < 0) {
                audioTrackIdPlayed = 1;
            }
            setAudioTrackAndChannel(audioTrackIdPlayed, audioChannelPlayed);
        }

        // build R.id.audioTrack submenu
        mPresentView.buildAudioTrackMenuItem(audioTrackIndicesList.size());

        // update the duration on controller UI
        mPresentView.update_Player_duration_seekbar(getPlayService().getMediaDuration());
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
