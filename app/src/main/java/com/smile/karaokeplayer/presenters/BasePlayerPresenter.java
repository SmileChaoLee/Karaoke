package com.smile.karaokeplayer.presenters;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.models.MySingleTon;
import com.smile.karaokeplayer.models.PlayingParameters;
import com.smile.karaokeplayer.models.SongInfo;
import com.smile.karaokeplayer.R;
import com.smile.karaokeplayer.services.PlayService;
import com.smile.karaokeplayer.utilities.DatabaseAccessUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;
import java.util.ArrayList;
import java.util.Locale;

public abstract class BasePlayerPresenter {

    private static final String TAG = "BasePlayerPresenter";
    private final Activity mActivity;
    private final BasePresentView mPresentView;
    protected final float textFontSize;
    protected final float fontScale;
    protected final float toastTextSize;
    protected Uri mediaUri;
    protected int numberOfVideoTracks;
    protected int numberOfAudioTracks;
    protected PlayingParameters playingParam;
    protected SongInfo singleSongInfo;    // when playing single song in songs list
    protected PlayService mPlayService;
    private boolean isServiceBound = false;
    private boolean isServiceDestroyed = true;
    private boolean mCanShowNotSupportedFormat;
    private int stopNumByUser = 0;

    public interface BasePresentView {
        void setImageButtonStatus();
        void playButtonOnPauseButtonOff();
        void playButtonOffPauseButtonOn();
        void setPlayingTimeTextView(String durationString);
        void update_Player_duration_seekbar(float duration);
        void update_Player_duration_seekbar_progress(int progress);
        void updateVolumeSeekBarProgress();
        void showNativeAndBannerAd();
        void hideNativeAndBannerAd();
        void showBufferingMessage();
        void dismissBufferingMessage();
        void buildAudioTrackMenuItem(int audioTrackNumber);
        void setTimerToHideSupportAndAudioController();
        void showMusicAndVocalIsNotSet();
        void showInterstitialAd();
        void hidePlayerView();
        void showPlayerView();
    }
    public abstract void initializeVariables(Bundle savedInstanceState, Intent callingIntent);
    public abstract void setPlayerTime(int progress);
    public abstract void setAudioVolume(float volume);
    public abstract void setAudioVolumeInsideVolumeSeekBar(int i);
    public abstract int getCurrentProgressForVolumeSeekBar();
    public abstract void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel);
    public abstract void switchAudioToMusic();
    public abstract void switchAudioToVocal();
    public abstract void startDurationSeekBarHandler();
    public abstract long getMediaDuration();
    public abstract void removeCallbacksAndMessages();
    public abstract void getPlayingMediaInfoAndSetAudioActionSubMenu();
    public abstract boolean isSeekable();
    public abstract void initMediaCallback();
    public abstract void specificPlayerReplayMedia(long currentAudioPosition);

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected().mActivity = " + mActivity);
            if (service != null) {
                PlayService.LocalBinder binder = (PlayService.LocalBinder) service;
                mPlayService = binder.getService();
                mPlayService.initMediaControllerCompat(BasePlayerPresenter.this);
            }
            isServiceBound = true;
            isServiceDestroyed = false;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected()");
            isServiceBound = false;
            isServiceDestroyed = true;
            startAndBindPlayService();
        }
    };

    public BasePlayerPresenter(Fragment fragment, BasePresentView presentView) {
        Log.d(TAG, "PlayerBasePresenter() constructor is called.");
        mActivity = fragment.getActivity();
        mPresentView = presentView;
        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(mActivity,
                ScreenUtil.FontSize_Pixel_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(mActivity, defaultTextFontSize,
                ScreenUtil.FontSize_Pixel_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(mActivity, ScreenUtil.FontSize_Pixel_Type, 0.0f);
        toastTextSize = 0.7f * textFontSize;
    }

    protected void startAndBindPlayService() {
        Intent intent = new Intent(mActivity, PlayService.class);
        if (isServiceDestroyed) {
            Log.d(TAG, "startAndBindPlayService.startService()");
            mActivity.startService(intent);
            isServiceDestroyed = false;
        } else {
            Log.d(TAG, "startAndBindPlayService.PlayService already started");
        }
        if (!isServiceBound) {
            boolean result =  mActivity.bindService(intent, connection, Context.BIND_IMPORTANT);
            Log.d(TAG, "startAndBindPlayService.isBound = " + result);
        } else {
            Log.d(TAG, "startAndBindPlayService.PlayService already bound");
        }
    }

    protected void unbindAndStopPlayService() {
        if (isServiceBound) {
            Log.d(TAG, "unbindAndStopPlayService.unbindService()");
            mActivity.unbindService(connection);
            // mPlayService = null;
            isServiceBound = false;
        } else {
            Log.d(TAG, "unbindAndStopPlayService.PlayService is not bound");
        }
        if (!isServiceDestroyed) {
            Log.d(TAG, "unbindAndStopPlayService.stopService()");
            Intent intent = new Intent(mActivity, PlayService.class);
            mActivity.stopService(intent);
            isServiceDestroyed = true;
        } else {
            Log.d(TAG, "unbindAndStopPlayService.PlayService is destroyed or not started");
        }
    }

    // For being used by Fragment or Activity
    public void onResume() {
        Log.d(TAG, "onResume");
        startAndBindPlayService();
    }
    public void onPause() {
        // Empty for now. Can be used for testing
    }
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        unbindAndStopPlayService();
    }
    //

    public Activity getActivity() {
        return mActivity;
    }
    public PlayService getPlayService() {
        return mPlayService;
    }

    public float getTextFontSize() {
        return textFontSize;
    }
    public float getFontScale() {
        return fontScale;
    }
    public float getToastTextSize() {
        return toastTextSize;
    }
    public Uri getMediaUri() {
        return mediaUri;
    }
    public void setMediaUri(Uri mediaUri) {
        this.mediaUri = mediaUri;
    }
    public int getNumberOfAudioTracks() {
        return numberOfAudioTracks;
    }
    public int getNumberOfVideoTracks() {
        return numberOfVideoTracks;
    }
    public PlayingParameters getPlayingParam() {
        return playingParam;
    }

    public void setPlayingParameters(SongInfo songInfo) {
        playingParam.setInSongList(songInfo.getIncluded().equals("1"));
        playingParam.setMusicAudioTrackIndex(songInfo.getMusicTrackNo());
        playingParam.setMusicAudioChannel(songInfo.getMusicChannel());
        playingParam.setVocalAudioTrackIndex(songInfo.getVocalTrackNo());
        playingParam.setVocalAudioChannel(songInfo.getVocalChannel());
        Log.d(TAG, "setPlayingParameters.(songInfo == singleSongInfo) = " + (songInfo == singleSongInfo));
        if (songInfo != singleSongInfo) {
            playingParam.setCurrentAudioTrackIndexPlayed(songInfo.getVocalTrackNo());
            playingParam.setCurrentChannelPlayed(songInfo.getVocalChannel());
            singleSongInfo = songInfo;
        }
    }

    public void autoPlaySongList() {
        Log.d(TAG, "autoPlaySongList");
        mCanShowNotSupportedFormat = true;
        if (MySingleTon.INSTANCE.getOrderedSongs().size() > 0) {
            // playingParam.setAutoPlay(true);
            playingParam.setCurrentSongIndex(-1); // next song that will be played, which the index is 0
            // start playing video from list
            startAutoPlay(false);
        } else {
            Log.d(TAG, "autoPlaySongList.orderedSongs.size() = 0");
            ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noFilesSelectedString)
                    , toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
        }
    }

    private void initializePlayingParam() {
        Log.d(TAG, "initializePlayingParam");
        playingParam = new PlayingParameters();
    }

    @SuppressWarnings("unchecked")
    protected void initializeVariablesBase(Bundle savedInstanceState, Intent callingIntent) {
        Log.d(TAG, "initializeVariablesBase");
        if (savedInstanceState == null) {
            numberOfVideoTracks = 0;
            numberOfAudioTracks = 0;
            mediaUri = null;
            initializePlayingParam();
            mCanShowNotSupportedFormat = false;
            playingParam.setPlaySingleSong(false);  // default
            singleSongInfo = null;    // default
            if (callingIntent != null) {
                Bundle arguments = callingIntent.getExtras();
                if (arguments != null) {
                    playingParam.setPlaySingleSong(arguments
                            .getBoolean(PlayerConstants.IsPlaySingleSongState, true));
                    playingParam.setCurrentVolume(arguments
                            .getFloat(PlayerConstants.SingleSongVolume, playingParam.getCurrentVolume()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        singleSongInfo = arguments.getParcelable(PlayerConstants.SingleSongInfoState, SongInfo.class);
                    else singleSongInfo = arguments.getParcelable(PlayerConstants.SingleSongInfoState);
                    Log.d(TAG, "initializeVariablesBase.singleSongInfo = " + singleSongInfo);
                }
            }
            MySingleTon.INSTANCE.getOrderedSongs().clear();
        } else {
            // needed to be set
            numberOfVideoTracks = savedInstanceState.getInt(PlayerConstants.NumberOfVideoTracksState,0);
            numberOfAudioTracks = savedInstanceState.getInt(PlayerConstants.NumberOfAudioTracksState);
            ArrayList<SongInfo> orderedSongs;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                orderedSongs = (ArrayList<SongInfo>)savedInstanceState.getSerializable(PlayerConstants.OrderedSongsState, ArrayList.class);
            } else orderedSongs = (ArrayList<SongInfo>)savedInstanceState.getSerializable(PlayerConstants.OrderedSongsState);
            Log.d(TAG, "initializeVariablesBase.orderedSongs = " + orderedSongs);
            if (orderedSongs != null) {
                MySingleTon.INSTANCE.getOrderedSongs().clear();
                MySingleTon.INSTANCE.getOrderedSongs().addAll(orderedSongs);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mediaUri = savedInstanceState.getParcelable(PlayerConstants.MediaUriState,Uri.class);
            } else mediaUri = savedInstanceState.getParcelable(PlayerConstants.MediaUriState);
            Log.d(TAG, "initializeVariablesBase.mediaUri = " + mediaUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                playingParam = savedInstanceState.getParcelable(PlayerConstants.PlayingParamState, PlayingParameters.class);
            } else playingParam = savedInstanceState.getParcelable(PlayerConstants.PlayingParamState);
            Log.d(TAG, "initializeVariablesBase.playingParam = " + playingParam);
            if (playingParam == null) {
                initializePlayingParam();
            }
            mCanShowNotSupportedFormat = savedInstanceState.getBoolean(PlayerConstants.CanShowNotSupportedFormatState);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                singleSongInfo = savedInstanceState.getParcelable(PlayerConstants.SingleSongInfoState, SongInfo.class);
            } else singleSongInfo = savedInstanceState.getParcelable(PlayerConstants.SingleSongInfoState);
            Log.d(TAG, "initializeVariablesBase.singleSongInfo = " + singleSongInfo);
        }
    }

    public void onDurationSeekBarProgressChanged(int progress, boolean fromUser) {
        Log.d(TAG, "onDurationSeekBarProgressChanged");
        if (!isSeekable()) {
            return;
        }
        float positionTime = progress / 1000.0f;   // seconds
        int minutes = (int)(positionTime / 60.0f);    // minutes
        int seconds = (int)positionTime - (minutes * 60);
        String durationString = String.format(Locale.ENGLISH, "%3d:%02d", minutes, seconds);
        mPresentView.setPlayingTimeTextView(durationString);
        if (fromUser) {
            setPlayerTime(progress);
        }
        playingParam.setCurrentAudioPosition(progress);
    }

    public void playLeftChannel() {
        Log.d(TAG, "playLeftChannel.CommonConstants.LeftChannel = " + CommonConstants.LeftChannel);
        playingParam.setCurrentChannelPlayed(CommonConstants.LeftChannel);
        setAudioVolume(playingParam.getCurrentVolume());
    }

    public void playRightChannel() {
        Log.d(TAG, "playRightChannel.CommonConstants.RightChannel = " + CommonConstants.RightChannel);
        playingParam.setCurrentChannelPlayed(CommonConstants.RightChannel);
        setAudioVolume(playingParam.getCurrentVolume());
    }

    public void playStereoChannel() {
        Log.d(TAG, "playStereoChannel.CommonConstants.StereoChannel = " + CommonConstants.StereoChannel);
        playingParam.setCurrentChannelPlayed(CommonConstants.StereoChannel);
        setAudioVolume(playingParam.getCurrentVolume());
    }

    public void startAutoPlay(boolean isSelfFinished) {
        Log.d(TAG, "startAutoPlay");
        if (mActivity.isFinishing()) {
            // activity is being destroyed
            return;
        }
        if (mPlayService != null) {
            Log.d(TAG, "startAutoPlay.mPlayService.startAutoPlay()");
            boolean stillPlayNext = mPlayService.startAutoPlay(this, isSelfFinished);
            Log.d(TAG, "startAutoPlay.stillPlayNext = " + stillPlayNext);
            if (!stillPlayNext) {    // still play the next song
                mPresentView.showNativeAndBannerAd();
                if ((MySingleTon.INSTANCE.getOrderedSongs().size() > 0)
                        && (!playingParam.isPlaySingleSong())) {
                    // finish playing and not playing single song
                    mPresentView.showInterstitialAd();
                }
            }
        }

        mPresentView.setImageButtonStatus();
    }

    public void setAutoPlayStatusAndAction() {
        Log.d(TAG, "setAutoPlayStatusAndAction");
        boolean isAutoPlay = playingParam.isAutoPlay();
        if (!isAutoPlay) {
            // previous is not auto play
            ArrayList<SongInfo> songList = DatabaseAccessUtil.readSavedSongList(mActivity, true);
            MySingleTon.INSTANCE.getOrderedSongs().clear();
            MySingleTon.INSTANCE.getOrderedSongs().addAll(songList);
            // playingParam.setAutoPlay(playSongList()); // must be above autoPlay savedSongList()
            // must be above autoPlay savedSongList()
            playingParam.setAutoPlay(MySingleTon.INSTANCE.getOrderedSongs().size() > 0);
            autoPlaySongList();
            mPresentView.showPlayerView();
        } else {
            // previous is auto play
            int playbackState = playingParam.getCurrentPlaybackState();
            if (playbackState!=PlaybackStateCompat.STATE_NONE
                    && playbackState!=PlaybackStateCompat.STATE_STOPPED) {
                // not the following: (has not started, stopped, or finished)
                stopPlay();
            }
            playingParam.setAutoPlay(false);    // must be the last in this block
            mPresentView.hidePlayerView();
        }
        mPresentView.setImageButtonStatus();
    }

    public void playPreviousSong() {
        Log.d(TAG, "playPreviousSong");
        int orderedSongsSize = MySingleTon.INSTANCE.getOrderedSongs().size();
        if (orderedSongsSize <= 1 ) {
            // only one file in the play list
            ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noPreviousSongString)
                    , toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
            return;
        }
        int currentIndex = playingParam.getCurrentSongIndex();
        int repeatStatus = playingParam.getRepeatStatus();
        // because in startAutoPlay(), the next song will be current index + 1
        int lastPreviousIndex = currentIndex - 2;
        switch (repeatStatus) {
            case PlayerConstants.NoRepeatPlaying:
            case PlayerConstants.RepeatOneSong:
                if (currentIndex <= 0) {
                    // no more previous
                    ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noPreviousSongString)
                            , toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                    return;
                }
                // because in startAutoPlay(), the next song will be current index + 1
                currentIndex = lastPreviousIndex;
                break;
            case PlayerConstants.RepeatAllSongs:
                if (currentIndex <= 0) {
                    // is going to play the last one
                    currentIndex = orderedSongsSize - 2; // the last one
                } else {
                    // because in startAutoPlay(), the next song will be current index + 1
                    currentIndex = lastPreviousIndex;
                }
                break;
        }
        playingParam.setCurrentSongIndex(currentIndex);
        startAutoPlay(false);
    }

    public void playNextSong() {
        Log.d(TAG, "playNextSong");
        int orderedSongsSize = MySingleTon.INSTANCE.getOrderedSongs().size();
        int currentIndex = playingParam.getCurrentSongIndex();
        int repeatStatus = playingParam.getRepeatStatus();
        if (orderedSongsSize <= 1 ) {
            // only one file in the play list
            ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noNextSongString)
                    , toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
            return; // no more next
        }
        switch (repeatStatus) {
            case PlayerConstants.NoRepeatPlaying:
            case PlayerConstants.RepeatOneSong:
                if (currentIndex >= (orderedSongsSize-1)) {
                    ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noNextSongString)
                            , toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                    return; // no more next
                }
                break;
            case PlayerConstants.RepeatAllSongs:
                break;
        }
        startAutoPlay(false);    // go to next round
    }

    public void playSongPlayedBeforeActivityCreated() {
        Log.d(TAG, "playSongPlayedBeforeActivityCreated.isPlaySingleSong = " + playingParam.isPlaySingleSong());
        if (mPresentView != null) mPresentView.updateVolumeSeekBarProgress();
        if (mediaUri==null || Uri.EMPTY.equals(mediaUri)) {
            if (playingParam.isPlaySingleSong()) {
                // called by SongListActivity
                Log.d(TAG, "playSongPlayedBeforeActivityCreated.singleSongInfo = " + singleSongInfo);
                if (singleSongInfo != null) {
                    playingParam.setAutoPlay(false);
                    // added on 2020-12-08
                    // set orderedSongs that only contains song info from SongListActivity
                    MySingleTon.INSTANCE.getOrderedSongs().clear();
                    MySingleTon.INSTANCE.getOrderedSongs().add(singleSongInfo);
                    singleSongInfo = new SongInfo();    // reset for cycle playing
                    autoPlaySongList();
                }
            } else {
                playingParam.setCurrentAudioPosition(0);
            }
        } else {
            int playbackState = playingParam.getCurrentPlaybackState();
            Log.d(TAG, "playSongPlayedBeforeActivityCreated.playbackState = " + playbackState);
            if (playbackState != PlaybackStateCompat.STATE_NONE) {
                if (mPlayService != null) {
                    Log.d(TAG, "playSongPlayedBeforeActivityCreated.mPlayService.playMediaFromUri()");
                    mPlayService.playMediaFromUri(mediaUri, playingParam);
                }
            }
        }
        float currentPosition = playingParam.getCurrentAudioPosition();
        Log.d(TAG, "playSongPlayedBeforeActivityCreated.currentPosition = " + currentPosition);
        onDurationSeekBarProgressChanged((int)currentPosition, true);
        if (mPresentView != null) {
            mPresentView.update_Player_duration_seekbar_progress((int)currentPosition);
        }
    }

    public void setRepeatSongStatus() {
        Log.d(TAG, "setRepeatSongStatus");
        int repeatStatus = playingParam.getRepeatStatus();
        switch (repeatStatus) {
            case PlayerConstants.NoRepeatPlaying:
                // switch to repeat one song
                playingParam.setRepeatStatus(PlayerConstants.RepeatOneSong);
                break;
            case PlayerConstants.RepeatOneSong:
                // switch to repeat song list
                playingParam.setRepeatStatus(PlayerConstants.RepeatAllSongs);
                break;
            case PlayerConstants.RepeatAllSongs:
                // switch to no repeat
                playingParam.setRepeatStatus(PlayerConstants.NoRepeatPlaying);
                break;
        }
        mPresentView.setImageButtonStatus();
    }

    public void startPlay() {
        Log.d(TAG, "startPlay");
        int playbackState = playingParam.getCurrentPlaybackState();
        if (playbackState==PlaybackStateCompat.STATE_NONE
            || playbackState==PlaybackStateCompat.STATE_STOPPED) {
            // start playing the first song in the list
            Log.d(TAG, "startPlay.calling autoPlaySongList()");
            autoPlaySongList();
            return;
        }
        if (mPlayService != null) {
            Log.d(TAG, "startPlay.mPlayService.startPlay() ");
            mPlayService.startPlay(this);
        }
    }

    public void pausePlay() {
        Log.d(TAG, "pausePlay");
        if (mPlayService != null) {
            Log.d(TAG, "pausePlay.mPlayService.pausePlay() ");
            mPlayService.pausePlay(this);
        }
    }

    public void stopPlay() {
        Log.d(TAG, "stopPlay");
        if (mPlayService != null) {
            Log.d(TAG, "stopPlay.mPlayService.stopPlay() ");
            mPlayService.stopPlay(this);
        }
    }

    public void replayMedia() {
        Log.d(TAG, "replayMedia");
        if (mPlayService != null) {
            Log.d(TAG, "replayMedia.mPlayService.replayMedia() ");
            mPlayService.replayMedia(this);
        }
    }

    public void updateStatusAndUi(PlaybackStateCompat state) {
        Log.d(TAG, "updateStatusAndUi()");
        int currentState = state.getState();
        playingParam.setCurrentPlaybackState(currentState);
        if (currentState == PlaybackStateCompat.STATE_BUFFERING) {
            Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_BUFFERING");
            mPresentView.hideNativeAndBannerAd();
            mPresentView.showBufferingMessage();
            return;
        }
        mPresentView.dismissBufferingMessage();
        switch (currentState) {
            case PlaybackStateCompat.STATE_NONE:
                // 1. initial state
                // 2. exoPlayer is stopped by user
                // 3. vlcPlayer finished playing (Event.EndReached)
                // 4. vlcPlayer is stopped by user
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_NONE");
                onDurationSeekBarProgressChanged(0, true); // set time to 0 position
                mPresentView.update_Player_duration_seekbar_progress(0);
                mPresentView.playButtonOnPauseButtonOff();
                removeCallbacksAndMessages();
                playingParam.setMediaPrepared(false);
                mPresentView.showNativeAndBannerAd();
                stopNumByUser++;
                Log.d(TAG, "updateStatusAndUi.stopNumByUser = " + stopNumByUser +
                        ", isPlaySingleSong = " + playingParam.isPlaySingleSong());
                if (stopNumByUser >= 3) {    // 3rd stop then show interstitial ad
                    if (!playingParam.isPlaySingleSong()) {
                        mPresentView.showInterstitialAd();
                    }
                    stopNumByUser = 0;
                }
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                // when playing
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_PLAYING");
                if (!playingParam.isMediaPrepared()) {
                    // the first time of Player.STATE_READY means prepared
                    getPlayingMediaInfoAndSetAudioActionSubMenu();
                }
                playingParam.setMediaPrepared(true);  // has been prepared
                startDurationSeekBarHandler();   // start updating duration seekbar
                // set up a timer for supportToolbar's visibility
                mPresentView.setTimerToHideSupportAndAudioController();
                mPresentView.playButtonOffPauseButtonOn();
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_PLAYING.hideNativeAndBannerAd()");
                onlyMusicShowNativeAndBannerAd();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                // when playing is paused
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_PAUSED");
                mPresentView.playButtonOnPauseButtonOff();
                mPresentView.showNativeAndBannerAd();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                // 1. exoPlayer finished playing
                // 2. after vlcPlayer finished playing
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_STOPPED");
                playingParam.setMediaPrepared(false);
                mPresentView.update_Player_duration_seekbar_progress((int) getMediaDuration());
                mPresentView.playButtonOnPauseButtonOff();
                removeCallbacksAndMessages();
                // nextSongOrShowNativeAndBannerAd(true);
                startAutoPlay(true);
                break;
            case PlaybackStateCompat.STATE_ERROR:
                String formatNotSupportedString = mActivity.getString(R.string.formatNotSupportedString);
                if (mCanShowNotSupportedFormat) {
                    // only show once
                    mCanShowNotSupportedFormat = false;
                    ScreenUtil.showToast(mActivity, formatNotSupportedString,
                            toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                }
                setMediaUri(null);
                // remove the song that is unable to be played
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_ERROR.orderedSongs.size() = "
                        + MySingleTon.INSTANCE.getOrderedSongs().size());
                int currentIndexOfList = playingParam.getCurrentSongIndex();
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_ERROR.currentIndexOfList = "
                        + currentIndexOfList);
                if (currentIndexOfList >= 0) {
                    MySingleTon.INSTANCE.getOrderedSongs().remove(currentIndexOfList);
                    Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_ERROR.orderedSongs.remove("+
                            currentIndexOfList+")");
                    playingParam.setCurrentSongIndex(--currentIndexOfList);
                }
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_ERROR.orderedSongs.size() = "
                        + MySingleTon.INSTANCE.getOrderedSongs().size());
                // nextSongOrShowNativeAndBannerAd(false);
                startAutoPlay(false);
                break;
            default:
                Log.d(TAG, "updateStatusAndUi.other PlaybackStateCompat");
        }
    }

    protected void onlyMusicShowNativeAndBannerAd() {
        Log.d(TAG, "musicShowNativeAndBannerAd.getNumberOfVideoTracks() = "
                + getNumberOfVideoTracks());
        if (getNumberOfVideoTracks() == 0) {
            // no video is being played, show native ads
            mPresentView.showNativeAndBannerAd();
        } else {
            mPresentView.hideNativeAndBannerAd();
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PlayerConstants.NumberOfVideoTracksState, numberOfVideoTracks);
        outState.putInt(PlayerConstants.NumberOfAudioTracksState, numberOfAudioTracks);
        Log.d(TAG, "saveInstanceState.orderedSongs = " + MySingleTon.INSTANCE.getOrderedSongs());
        ArrayList<SongInfo> orderedSongs = new ArrayList<>(MySingleTon.INSTANCE.getOrderedSongs());
        outState.putSerializable(PlayerConstants.OrderedSongsState, orderedSongs);
        outState.putParcelable(PlayerConstants.MediaUriState, mediaUri);
        outState.putParcelable(PlayerConstants.PlayingParamState, playingParam);
        outState.putBoolean(PlayerConstants.CanShowNotSupportedFormatState, mCanShowNotSupportedFormat);
        Log.d(TAG, "saveInstanceState.singleSongInfo = " + singleSongInfo);
        outState.putParcelable(PlayerConstants.SingleSongInfoState, singleSongInfo);
    }
}
