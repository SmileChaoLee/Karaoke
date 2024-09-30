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
        mActivity = mFragment.getActivity();
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
        ExoPlayService playService = mPresentView.getPlayService() != null?
                (ExoPlayService) (mPresentView.getPlayService()) : null;
        return playService;
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
        if (!mPlayingParam.isInSongList()) {
            // not in the database and show message
            mPresentView.showMusicAndVocalIsNotSet();
        }
        int audioTrack = mPlayingParam.getMusicAudioTrackIndex();
        int audioChannel = mPlayingParam.getMusicAudioChannel();
        setAudioTrackAndChannel(audioTrack, audioChannel);
    }

    @Override
    public void switchAudioToVocal() {
        Log.d(TAG, "switchAudioToVocal() is called.");
        if (!mPlayingParam.isInSongList()) {
            // not in the database and show message
            mPresentView.showMusicAndVocalIsNotSet();
        }
        setAudioTrackAndChannel(mPlayingParam.getVocalAudioTrackIndex(), mPlayingParam.getVocalAudioChannel());
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

    /*
    // @Override
    private void getPlayingMediaInfoAndSetAudioActionSubMenu() {
        Log.d(TAG, "getPlayingMediaInfoAndSetAudioActionSubMenu()");
        int numVideoRenderers = 0;
        int numAudioRenderers = 0;
        int numVideoTrackGroups = 0;
        int numAudioTrackGroups = 0;

        mNumberOfVideoTracks = 0;
        audioTrackIndicesList.clear();

        Integer[] trackIndicesCombination;
        int audioTrackIdPlayed = -1;

        Format videoPlayedFormat = getExoPlayer().getVideoFormat();
        if (videoPlayedFormat != null) {
            Log.d(TAG, "videoPlayedFormat.id = " + videoPlayedFormat.id);
        } else {
            Log.d(TAG, "videoPlayedFormat is null.");
        }
        Format audioPlayedFormat = getExoPlayer().getAudioFormat();
        if (audioPlayedFormat != null) {
            Log.d(TAG, "audioPlayedFormat.id = " + audioPlayedFormat.id);
            int channelsNum = audioPlayedFormat.channelCount;
            Log.d(TAG, "audioPlayedFormat.channelCount = " + channelsNum);
            Log.d(TAG, "audioPlayedFormat.sampleRate = " + audioPlayedFormat.sampleRate);
            Log.d(TAG, "audioPlayedFormat.pcmEncoding = " + audioPlayedFormat.pcmEncoding);
        } else {
            Log.d(TAG, "audioPlayedFormat is null.");
        }

        DefaultTrackSelector trackSelector = (DefaultTrackSelector) getExoPlayer().getTrackSelector();
        if (trackSelector == null) {
            Log.d(TAG, "getPlayingMediaInfoAndSetAudioActionSubMenu.trackSelector is null");
            return;
        }
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            int rendererCount = mappedTrackInfo.getRendererCount();
            Log.d(TAG, "mappedTrackInfo.getRendererCount() = " + rendererCount);
            //
            for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
                Log.d(TAG, "rendererIndex = " + rendererIndex);
                int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
                switch (rendererType) {
                    case C.TRACK_TYPE_VIDEO:
                        numVideoRenderers++;
                        break;
                    case C.TRACK_TYPE_AUDIO:
                        numAudioRenderers++;
                        break;
                }
                //
                TrackGroupArray trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex);
                if (trackGroupArray != null) {
                    int arraySize = trackGroupArray.length;
                    Log.d(TAG, "trackGroupArray.length of renderer no ( " + rendererIndex + " ) = " + arraySize);
                    for (int groupIndex = 0; groupIndex < arraySize; groupIndex++) {
                        Log.d(TAG, "trackGroupArray.index = " + groupIndex);
                        switch (rendererType) {
                            case C.TRACK_TYPE_VIDEO:
                                numVideoTrackGroups++;
                                break;
                            case C.TRACK_TYPE_AUDIO:
                                numAudioTrackGroups++;
                                break;
                        }
                        TrackGroup trackGroup = trackGroupArray.get(groupIndex);
                        int groupSize = trackGroup.length;
                        Log.d(TAG, "trackGroup.length of trackGroup [ " + groupIndex + " ] = " + groupSize);
                        for (int trackIndex = 0; trackIndex < groupSize; trackIndex++) {
                            Format tempFormat = trackGroup.getFormat(trackIndex);
                            switch (rendererType) {
                                case C.TRACK_TYPE_VIDEO:
                                    trackIndicesCombination = new Integer[3];
                                    trackIndicesCombination[0] = rendererIndex;
                                    trackIndicesCombination[1] = groupIndex;
                                    trackIndicesCombination[2] = trackIndex;
                                    mNumberOfVideoTracks++;
                                    break;
                                case C.TRACK_TYPE_AUDIO:
                                    trackIndicesCombination = new Integer[3];
                                    trackIndicesCombination[0] = rendererIndex;
                                    trackIndicesCombination[1] = groupIndex;
                                    trackIndicesCombination[2] = trackIndex;
                                    audioTrackIndicesList.add(trackIndicesCombination);
                                    if (tempFormat.equals(audioPlayedFormat)) {
                                        audioTrackIdPlayed = audioTrackIndicesList.size();
                                    }
                                    break;
                            }
                            //
                            Log.d(TAG, "tempFormat = " + tempFormat);
                        }
                    }
                }
            }
        } else {
            Log.d(TAG, "mappedTrackInfo is null.");
        }
        mNumberOfAudioTracks = audioTrackIndicesList.size();

        Log.d(TAG, "numVideoRenderer = " + numVideoRenderers);
        Log.d(TAG, "numAudioRenderer = " + numAudioRenderers);
        Log.d(TAG, "numVideoTrackGroups = " + numVideoTrackGroups);
        Log.d(TAG, "numAudioTrackGroups = " + numAudioTrackGroups);
        Log.d(TAG, "numberOfVideoTracks = " + mNumberOfVideoTracks);
        Log.d(TAG, "numberOfAudioTracks = " + mNumberOfAudioTracks);

        if (mNumberOfAudioTracks == 0) {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(PlayerConstants.NoAudioTrack);
            mPlayingParam.setCurrentChannelPlayed(PlayerConstants.NoAudioChannel);
        } else {
            int audioChannelPlayed;
            Log.d(TAG, "audioTrackIdPlayed = " + audioTrackIdPlayed);
            if (mPlayingParam.isAutoPlay() || mPlayingParam.isPlaySingleSong() || mPlayingParam.isInSongList()) {
                audioTrackIdPlayed = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                audioChannelPlayed = mPlayingParam.getCurrentChannelPlayed();
                Log.d(TAG, "Auto play or playing single song.");
            } else {
                // for open media. do not know the music track and vocal track
                Log.d(TAG, "Do not know the music track and vocal track.");
                // guess
                audioTrackIdPlayed = mPlayingParam.getCurrentAudioTrackIndexPlayed();
                Log.d(TAG, "getPlayingMediaInfoAndSetAudioActionSubMenu.playingParam.getCurrentAudioTrackIndexPlayed() = " + audioTrackIdPlayed);
                audioChannelPlayed = mPlayingParam.getCurrentChannelPlayed();
                Log.d(TAG, "getPlayingMediaInfoAndSetAudioActionSubMenu.playingParam.getCurrentChannelPlayed() = " + audioChannelPlayed);
                Log.d(TAG, "getPlayingMediaInfoAndSetAudioActionSubMenu.numberOfAudioTracks = " + mNumberOfAudioTracks);
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
        mPresentView.update_Player_duration_seekbar(getExoPlayer().getDuration());
    }
    */

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
        // mPresentView.update_Player_duration_seekbar(getExoPlayer().getDuration());
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
