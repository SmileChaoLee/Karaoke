package exoplayer.presenters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.mediarouter.media.MediaRouter;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.dynamite.DynamiteModule;
import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.presenters.BasePlayerPresenter;

import exoplayer.listeners.ExoPlayerCastStateListener;
import exoplayer.listeners.ExoPlayerListener;
import exoplayer.services.ExoPlayService;

public class ExoPlayerPresenter extends BasePlayerPresenter {

    private static final String TAG = "ExoPlayerPresenter";

    private final Fragment mFragment;
    private final Activity mActivity;
    private final ExoPlayerPresentView mPresentView;
    private CastContext mCastContext;
    private ExoPlayerCastStateListener exoPlayerCastStateListener;
    // private StereoVolumeAudioProcessor stereoVolumeAudioProcessor;   // commented out for testing
    private TrackSelectionParameters mTrackSelectionParameters;
    // private ExoPlayer mExoPlayer;
    private CastPlayer mCastPlayer;
    private int mCurrentCastState;
    private final boolean isOnInternet = false;
    private SessionAvailabilityListener mSessionAvailabilityListener;
    private ExoPlayerListener mExoPlayerListener;
    private Player mCurrentPlayer;
    // private ExoMediaSessionCallback mediaSessionCallback;
    // private ExoMediaControllerCallback controllerCallback;
    private int mCurrentItemIndex = -1;
    // instances of the following members have to be saved when configuration changed
    private ArrayList<Integer[]> audioTrackIndicesList = new ArrayList<>();
    private final Handler durationSeekBarHandler = new Handler(Looper.getMainLooper());
    private final Runnable durationSeekBarRunnable = new Runnable() {
        @Override
        public synchronized void run() {
            durationSeekBarHandler.removeCallbacksAndMessages(null);
            if (getExoPlayer() != null) {
                int playbackState = getExoPlayer().getPlaybackState();
                if (getExoPlayer().getPlayWhenReady() && playbackState!=Player.STATE_IDLE && playbackState!=Player.STATE_ENDED) {
                    mPresentView.update_Player_duration_seekbar_progress((int) getExoPlayer().getCurrentPosition());
                }
            }
            durationSeekBarHandler.postDelayed(durationSeekBarRunnable, 500);
        }
    };

    public interface ExoPlayerPresentView extends BasePlayerPresenter.BasePresentView {
        void setCurrentPlayerToPlayerView();
    }

    public ExoPlayerPresenter(Fragment fragment, ExoPlayerPresentView presentView) {
        super(fragment, presentView);
        Log.d(TAG, "Create ExoPlayerPresenter");
        mFragment = fragment;
        mActivity = fragment.getActivity();
        mPresentView = presentView;
    }

    /*  commented out for testing
    public void initExoPlayer() {
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(mActivity, new AdaptiveTrackSelection.Factory());
        Log.d(TAG, "initExoPlayer.trackSelector = " + trackSelector);
        trackSelector.setParameters(mTrackSelectionParameters);

        // EXTENSION_RENDERER_MODE_OFF, EXTENSION_RENDERER_MODE_ON, EXTENSION_RENDERER_MODE_PREFER
        MyRenderersFactory myRenderersFactory = new MyRenderersFactory(mActivity, EXTENSION_RENDERER_MODE_ON);
        stereoVolumeAudioProcessor = myRenderersFactory.getStereoVolumeAudioProcessor();

        ExoPlayer.Builder exoPlayerBuilder = new ExoPlayer.Builder(mActivity, myRenderersFactory);
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
        ExoPlayer mExoPlayer = exoPlayerBuilder
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(mActivity, extractorsFactory))
                .build();
        mExoPlayer.setTrackSelectionParameters(mTrackSelectionParameters);

        mExoPlayerListener = new ExoPlayerListener(this);
        mExoPlayer.addListener(mExoPlayerListener);
        mCurrentPlayer = mExoPlayer; // default is playing video on Android device

        Log.d(TAG, "initExoPlayer.FfmpegLibrary.isAvailable() = " + FfmpegLibrary.isAvailable());
        Log.d(TAG, "initExoPlayer.VpxLibrary.isAvailable() = " + VpxLibrary.isAvailable());
        Log.d(TAG, "initExoPlayer.FlacLibrary.isAvailable() = " + FlacLibrary.isAvailable());
        Log.d(TAG, "initExoPlayer.OpusLibrary.isAvailable() = " + OpusLibrary.isAvailable());
        Log.d(TAG, "initExoPlayer.Gav1Library.isAvailable() = " + Gav1Library.isAvailable());
    }
    */

    /*  commented out for testing
    private void releaseExoPlayer() {
        Log.d(TAG, "releaseExoPlayer");
        if (mExoPlayer != null) {
            mExoPlayer.removeListener(mExoPlayerListener);
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }
    */

    public void initCastPlayer() {
        mCastContext = null;
        mCurrentCastState = CastState.NO_DEVICES_AVAILABLE;
        if (com.smile.karaokeplayer.BuildConfig.DEBUG) {
            Log.d(TAG, "initCastPlayer.com.smile.karaokeplayer.BuildConfig.DEBUG");
            try {
                mCastContext = CastContext.getSharedInstance(mActivity);
                exoPlayerCastStateListener = new ExoPlayerCastStateListener(mFragment, this);
                mCurrentCastState = mCastContext.getCastState();
                Log.d(TAG, "initCastPlayer.mCurrentCastState = " + mCurrentCastState);
                boolean deviceAvailable = mCurrentCastState != CastState.NO_DEVICES_AVAILABLE;
                Log.d(TAG, "initCastPlayer.deviceAvailable = " + deviceAvailable);
                mCastPlayer = new CastPlayer(mCastContext);
                // castPlayer.addListener(mExoPlayerEventListener); // add different listener later
                mSessionAvailabilityListener = new SessionAvailabilityListener() {
                    @Override
                    public synchronized void onCastSessionAvailable() {
                        Log.d(TAG, "initCastPlayer.onCastSessionAvailable");
                        Log.d(TAG, "initCastPlayer.onCastSessionAvailable.mediaUri = " +
                                mMediaUri);
                        Log.d(TAG, "initCastPlayer.onCastSessionAvailable.isOnInternet = " +
                                isOnInternet);
                        if (mMediaUri == null || !isOnInternet) {
                            MediaRouter mRouter = MediaRouter.getInstance(mActivity);  // singleton
                            mRouter.unselect(MediaRouter.UNSELECT_REASON_STOPPED);  // stop casting
                            return;
                        }
                        Log.d(TAG, "initCastPlayer.onCastSessionAvailable." +
                                "Set current player to castPlayer");
                        setCurrentPlayer(mCastPlayer);
                    }

                    @Override
                    public void onCastSessionUnavailable() {
                        Log.d(TAG, "initCastPlayer.onCastSessionUnavailable.setCurrentPlayer()");
                        setCurrentPlayer(getExoPlayer());
                    }
                };
            } catch (RuntimeException e) {
                mCastContext = null;
                Throwable cause = e.getCause();
                while (cause != null) {
                    if (cause instanceof DynamiteModule.LoadingException) {
                        Log.d(TAG, "ExoPlayerPresenter.Failed to get CastContext." +
                                " Try updating Google Play Services and restart the app.");
                    }
                    cause = cause.getCause();
                }
                // Unknown error. We propagate it.
                Log.d(TAG, "initCastPlayer.Failed to get CastContext. Unknown error.");
            }
        }
    }

    private void releaseCastPlayer() {
        Log.d(TAG, "releaseCastPlayer");
        if (mCastPlayer != null) {
            mCastPlayer.setSessionAvailabilityListener(null);
            mCastPlayer.release();
            mCastPlayer = null;
        }
    }

    public void releaseExoPlayerAndCastPlayer() {
        // releaseExoPlayer();  commented for testing
        releaseCastPlayer();
    }

    public ExoPlayer getExoPlayer() {
        return getPlayService().getExoPlayer();
    }

    public CastPlayer getCastPlayer() {
        return mCastPlayer;
    }

    public ExoPlayService getPlayService() {
        Log.d(TAG, "getPlayService");
        ExoPlayService playService = mPresentView.getPlayService() != null?
                (ExoPlayService) (mPresentView.getPlayService()) : null;
        Log.d(TAG, "getPlayService.playService = " + playService);
        return playService;
    }

    public int getCurrentCastState() {
        return mCurrentCastState;
    }
    public void setCurrentCastState(int currentCastState) {
        mCurrentCastState = currentCastState;
    }

    /*  commented out for testing
    private void selectAudioTrack(Integer[] trackIndicesCombination) {
        Log.d(TAG, "selectAudioTrack");
        DefaultTrackSelector trackSelector = (DefaultTrackSelector) getExoPlayer().getTrackSelector();
        if (trackSelector == null) {
            Log.d(TAG, "selectAudioTrack.trackSelector is null");
            return;
        }
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if ( (trackIndicesCombination == null) || (mappedTrackInfo == null) ) {
            return;
        }

        int audioRendererIndex = trackIndicesCombination[0];
        Log.d(TAG, "selectAudioTrack.audioRendererIndex = " + audioRendererIndex);
        int audioTrackGroupIndex = trackIndicesCombination[1];
        Log.d(TAG, "selectAudioTrack.audioTrackGroupIndex = " + audioTrackGroupIndex);
        int audioTrackIndex = trackIndicesCombination[2];
        Log.d(TAG, "selectAudioTrack.audioTrackIndex = " + audioTrackIndex);

        if (mappedTrackInfo.getTrackSupport(audioRendererIndex, audioTrackGroupIndex, audioTrackIndex)
                 != C.FORMAT_HANDLED) {
            return;
        }

        Log.d(TAG, "selectAudioTrack.trackSelectorParameters = " + mTrackSelectionParameters);
        TrackSelectionParameters.Builder parametersBuilder= mTrackSelectionParameters.buildUpon();
        TrackGroup trackGroup = mappedTrackInfo.getTrackGroups(audioRendererIndex).get(audioTrackGroupIndex);
        TrackSelectionOverride override = new TrackSelectionOverride(trackGroup, audioTrackIndex);
        mTrackSelectionParameters = parametersBuilder.setOverrideForType(override).build();
        getExoPlayer().setTrackSelectionParameters(mTrackSelectionParameters);
    }
    */

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

    /*  commented out for testing
    public void releaseMediaCallback() {
        Log.d(TAG, "releaseMediaCallback");
        mediaSessionCallback = null;
        Log.d(TAG, "releaseMediaCallback.getPlayService() = " + getPlayService());
        if (getPlayService() != null && controllerCallback != null) {
            getPlayService().getMediaControllerCompat().unregisterCallback(controllerCallback);
            controllerCallback = null;
        }
    }
    */

    /*  commented out for testing
    @Override
    public void setPlayerTime(int progress) {
        getExoPlayer().seekTo(progress);
    }
    */

    @Override
    public void setAudioVolume(float volume) {
        Log.d(TAG, "setAudioVolume");
        if (getPlayService() != null) {
            Log.d(TAG, "setAudioVolume.getPlayService().setPlayerAudioVolume(volume)");
            getPlayService().setPlayerAudioVolume(volume);
        }
        mPlayingParam.setCurrentVolume(volume);
    }

    @Override
    public void setAudioVolumeInsideVolumeSeekBar(int i) {
        Log.d(TAG, "setAudioVolumeInsideVolumeSeekBar");
        float currentVolume = 1.0f;
        if (i < PlayerConstants.MaxProgress) {
            currentVolume = (float)(1.0f - (Math.log(PlayerConstants.MaxProgress - i)
                    / Math.log(PlayerConstants.MaxProgress)));
        }
        setAudioVolume(currentVolume);
    }

    @Override
    public int getCurrentProgressForVolumeSeekBar() {
        Log.d(TAG, "setAudioVolumeInsideVolumeSeekBar");
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
            setAudioVolume(mPlayingParam.getCurrentVolume());
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

    /*  commented out for testing
    @Override
    public long getMediaDuration() {
        return getExoPlayer().getDuration();
    }
    */

    @Override
    public void removeCallbacksAndMessages() {
        durationSeekBarHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void getPlayingMediaInfoAndSetAudioActionSubMenu() {
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

    /*  commented out for testing
    @Override
    public boolean isSeekable() {
        return getExoPlayer().isCurrentMediaItemSeekable();
    }
    */

    /*  commented out for testing
    @Override
    public void initMediaCallback() {
        Log.d(TAG, "initMediaCallback");
        mediaSessionCallback = new ExoMediaSessionCallback(mActivity,this);
        controllerCallback = new ExoMediaControllerCallback(this);
        Log.d(TAG, "initMediaCallback.getPlayService() = " + getPlayService());
        if (getPlayService() != null) {
            Log.d(TAG, "initMediaCallback.mediaSessionCallback = " + mediaSessionCallback);
            getPlayService().getMediaSessionCompat().setCallback(mediaSessionCallback);
            Log.d(TAG, "initMediaCallback.controllerCallback = " + controllerCallback);
            getPlayService().getMediaControllerCompat().registerCallback(controllerCallback);
        }
    }
    */

    /*  commented out for testing
    @Override
    public void specificPlayerReplayMedia(long currentAudioPosition) {
        // song is playing, paused, or finished playing
        // cannot do the following statement (exoPlayer.setPlayWhenReady(false); )
        // because it will send Play.STATE_ENDED event after the playing has finished
        // but the playing was stopped in the middle of playing then won't send
        // Play.STATE_ENDED event
        // exoPlayer.setPlayWhenReady(false);
        Log.d(TAG, "specificPlayerReplayMedia.exoPlayer.seekTo(currentAudioPosition).");
        getExoPlayer().seekTo(currentAudioPosition);
        getExoPlayer().prepare();    // replace exoPlayer.retry();
        getExoPlayer().setPlayWhenReady(true);
    }
    */
    // End of override abstract method

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG,"saveInstanceState() is called.");

        if (getExoPlayer() != null) {
            mPlayingParam.setCurrentAudioPosition(getExoPlayer().getContentPosition());
        } else {
            mPlayingParam.setCurrentAudioPosition(0);
        }
        outState.putSerializable(PlayerConstants.AudioTrackIndicesListState, audioTrackIndicesList);
        outState.putBundle(PlayerConstants.TrackSelectionParametersState, mTrackSelectionParameters.toBundle());
        super.saveInstanceState(outState);
    }

    // methods related to ChromeCast
    public Player getCurrentPlayer() {
        return mCurrentPlayer;
    }

    @SuppressLint("WrongConstant")
    public synchronized void setCurrentPlayer(Player currentPlayer) {
        if (currentPlayer == null) {
            return;
        }
        if (mCurrentPlayer == currentPlayer) {
            return;
        }
        if (mMediaUri == null) {
            return;
        }
        // Player View management.
        mPresentView.setCurrentPlayerToPlayerView();
        // Player state management.
        long playbackPositionMs = C.TIME_UNSET;
        int windowIndex = C.INDEX_UNSET;
        boolean playWhenReady = false;
        Player previousPlayer = mCurrentPlayer;
        if (previousPlayer != null) {
            // Save state from the previous player.
            int playbackState = previousPlayer.getPlaybackState();
            if (playbackState != Player.STATE_ENDED) {
                playbackPositionMs = previousPlayer.getCurrentPosition();
                playWhenReady = previousPlayer.getPlayWhenReady();
                windowIndex = previousPlayer.getCurrentMediaItemIndex();
                if (windowIndex != mCurrentItemIndex) {
                    playbackPositionMs = C.TIME_UNSET;
                    // windowIndex = currentItemIndex;
                    mCurrentItemIndex = windowIndex;
                }
            }
            // previousPlayer.stop(true);
            stopPlay(); // or pausePlay();
        }
        mCurrentPlayer = currentPlayer;
        if (mCurrentPlayer == getCastPlayer()) {
            Log.d(TAG, "exoPlayer startPlay()");
            startPlay();
        } else {
            // Playback transition.
            if (mCastPlayer.getCurrentTimeline().isEmpty()) {
                // has not play yet
                Log.d(TAG, "getCurrentTimeline() is Empty()");
                MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(mMediaUri)
                        .setMediaMetadata(new MediaMetadata.Builder().setTitle("Video Casted").build())
                        .setMimeType(MimeTypes.BASE_TYPE_VIDEO)
                        // .setDrmConfiguration(null)
                        .build();
                Log.d(TAG, "windowIndex = " + windowIndex);
                List<MediaItem> mediaItems = new ArrayList<>();
                mediaItems.add(mediaItem);
                mCastPlayer.setMediaItems(mediaItems, windowIndex, C.TIME_UNSET);
                mCastPlayer.setRepeatMode(mPlayingParam.getRepeatStatus());
                //
                mCastPlayer.setPlayWhenReady(playWhenReady);
            } else {
                // already played before
                Log.d(TAG, "getCurrentTimeline() is not Empty()");
            }
        }
        // Playback transition.
        if (windowIndex != C.INDEX_UNSET) {
            Log.d(TAG, "windowIndex != C.INDEX_UNSET");
            currentPlayer.seekTo(playbackPositionMs);
            currentPlayer.setPlayWhenReady(playWhenReady);
        }
    }

    // ChromeCast methods
    public void setSessionAvailabilityListener() {
        if (mCastPlayer !=null && mSessionAvailabilityListener!=null) {
            mCastPlayer.setSessionAvailabilityListener(mSessionAvailabilityListener);
        }
    }
    public void releaseSessionAvailabilityListener() {
        if (mCastPlayer !=null) {
            mCastPlayer.setSessionAvailabilityListener(null);
        }
    }
    public void addBaseCastStateListener() {
        Log.d(TAG, "addBaseCastStateListener() is called.");
        Log.d(TAG, "castContext = " + mCastContext);
        if (mCastContext !=null && exoPlayerCastStateListener!=null) {
            mCastContext.addCastStateListener(exoPlayerCastStateListener);
            Log.d(TAG, "castContext.addCastStateListener(baseCastStateListener)");
        }
    }
    public void removeBaseCastStateListener() {
        Log.d(TAG, "removeBaseCastStateListener() is called.");
        if (mCastContext !=null && exoPlayerCastStateListener!=null) {
            mCastContext.removeCastStateListener(exoPlayerCastStateListener);
            Log.d(TAG, "castContext.removeCastStateListener(baseCastStateListener)");
        }
    }
}
