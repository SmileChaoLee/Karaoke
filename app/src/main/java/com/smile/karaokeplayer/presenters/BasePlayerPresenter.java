package com.smile.karaokeplayer.presenters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.fragments.PlayerBaseViewFragment;
import com.smile.karaokeplayer.models.MySingleTon;
import com.smile.karaokeplayer.models.PlayingParameters;
import com.smile.karaokeplayer.models.SongInfo;
import com.smile.karaokeplayer.R;
import com.smile.karaokeplayer.services.BasePlayService;
import com.smile.karaokeplayer.utilities.DatabaseAccessUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;
import java.util.ArrayList;
import java.util.Locale;

public abstract class BasePlayerPresenter {

    private static final String TAG = "BasePlayerPresenter";
    private final BasePresentView mPresentView;
    protected final Activity mActivity;
    protected final float mTextFontSize;
    protected final float mFontScale;
    protected final float mToastTextSize;
    protected Uri mMediaUri;
    protected int mNumberOfVideoTracks;
    protected int mNumberOfAudioTracks;
    protected PlayingParameters mPlayingParam;
    protected SongInfo mSingleSongInfo;    // when playing single song in songs list
    private boolean mCanShowNotSupportedFormat;

    public interface BasePresentView {
        void setImageButtonStatus();
        void playButtonOnPauseButtonOff();
        void playButtonOffPauseButtonOn();
        void setPlayingTimeTextView(String durationString);
        void update_Player_duration_seekbar(float duration);
        void update_Player_duration_seekbar_progress(int progress);
        void updateVolumeSeekBarProgress();
        void showNativeAndHideBannerAd();
        void hideNativeAd();
        void showBufferingMessage();
        void dismissBufferingMessage();
        void buildAudioTrackMenuItem(int audioTrackNumber);
        void setTimerToHideSupportAndAudioController();
        void showMusicAndVocalIsNotSet();
        void showInterstitialAd();
        void hidePlayerView();
        void showPlayerView();
        void setCurrentPlayerToPlayerView();
        BasePlayService getPlayService();
    }
    public abstract void initializeVariables(Bundle savedInstanceState, Intent callingIntent);
    // public abstract void setPlayerTime(int progress); commented out for testing
    // public abstract void setAudioVolume(float volume);
    public abstract void setAudioVolumeInsideVolumeSeekBar(int i);
    public abstract int getCurrentProgressForVolumeSeekBar();
    public abstract void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel);
    public abstract void switchAudioToMusic();
    public abstract void switchAudioToVocal();
    public abstract void startDurationSeekBarHandler();
    // public abstract long getMediaDuration(); commented out for testing
    public abstract void removeCallbacksAndMessages();
    // public abstract void getPlayingMediaInfoAndSetAudioActionSubMenu();
    public abstract void setAudioActionSubMenu();
    // public abstract boolean isSeekable();    commented out for testing
    // public abstract void initMediaCallback();    commented out for testing
    // public abstract void specificPlayerReplayMedia(long currentAudioPosition); commented out for testing

    public BasePlayerPresenter(PlayerBaseViewFragment fragment, BasePresentView presentView) {
        Log.d(TAG, "PlayerBasePresenter() constructor is called.");
        mActivity = fragment.getActivity();
        mPresentView = presentView;
        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(mActivity,
                ScreenUtil.FontSize_Pixel_Type, null);
        mTextFontSize = ScreenUtil.suitableFontSize(mActivity, defaultTextFontSize,
                ScreenUtil.FontSize_Pixel_Type, 0.0f);
        mFontScale = ScreenUtil.suitableFontScale(mActivity, ScreenUtil.FontSize_Pixel_Type, 0.0f);
        mToastTextSize = 0.7f * mTextFontSize;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public float getTextFontSize() {
        return mTextFontSize;
    }
    public float getFontScale() {
        return mFontScale;
    }
    public float getToastTextSize() {
        return mToastTextSize;
    }
    public Uri getMediaUri() {
        return mMediaUri;
    }
    public void setMediaUri(Uri mediaUri) {
        this.mMediaUri = mediaUri;
    }
    public int getNumberOfAudioTracks() {
        return mNumberOfAudioTracks;
    }
    public int getNumberOfVideoTracks() {
        return mNumberOfVideoTracks;
    }
    public PlayingParameters getPlayingParam() {
        return mPlayingParam;
    }

    public void setPlayingParameters(SongInfo songInfo) {
        mPlayingParam.setInSongList(songInfo.getIncluded().equals("1"));
        mPlayingParam.setMusicAudioTrackIndex(songInfo.getMusicTrackNo());
        mPlayingParam.setMusicAudioChannel(songInfo.getMusicChannel());
        mPlayingParam.setVocalAudioTrackIndex(songInfo.getVocalTrackNo());
        mPlayingParam.setVocalAudioChannel(songInfo.getVocalChannel());
        Log.d(TAG, "setPlayingParameters.(songInfo == singleSongInfo) = " + (songInfo == mSingleSongInfo));
        if (songInfo != mSingleSongInfo) {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(songInfo.getVocalTrackNo());
            mPlayingParam.setCurrentChannelPlayed(songInfo.getVocalChannel());
            mSingleSongInfo = songInfo;
        }
    }

    public void autoPlaySongList() {
        Log.d(TAG, "autoPlaySongList");
        mCanShowNotSupportedFormat = true;
        if (MySingleTon.INSTANCE.getOrderedSongs().size() > 0) {
            // playingParam.setAutoPlay(true);
            mPlayingParam.setCurrentSongIndex(-1); // next song that will be played, which the index is 0
            // start playing video from list
            startAutoPlay(false);
        } else {
            Log.d(TAG, "autoPlaySongList.orderedSongs.size() = 0");
            ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noFilesSelectedString)
                    , mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
        }
    }

    private void initializePlayingParam() {
        Log.d(TAG, "initializePlayingParam");
        mPlayingParam = new PlayingParameters();
    }

    @SuppressWarnings("unchecked")
    protected void initializeVariablesBase(Bundle savedInstanceState, Intent callingIntent) {
        Log.d(TAG, "initializeVariablesBase");
        if (savedInstanceState == null) {
            mNumberOfVideoTracks = 0;
            mNumberOfAudioTracks = 0;
            mMediaUri = null;
            initializePlayingParam();
            mCanShowNotSupportedFormat = false;
            mPlayingParam.setPlaySingleSong(false);  // default
            mSingleSongInfo = null;    // default
            if (callingIntent != null) {
                Bundle arguments = callingIntent.getExtras();
                if (arguments != null) {
                    mPlayingParam.setPlaySingleSong(arguments
                            .getBoolean(PlayerConstants.IsPlaySingleSongState, true));
                    mPlayingParam.setCurrentVolume(arguments
                            .getFloat(PlayerConstants.SingleSongVolume, mPlayingParam.getCurrentVolume()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        mSingleSongInfo = arguments.getParcelable(PlayerConstants.SingleSongInfoState, SongInfo.class);
                    else mSingleSongInfo = arguments.getParcelable(PlayerConstants.SingleSongInfoState);
                    Log.d(TAG, "initializeVariablesBase.singleSongInfo = " + mSingleSongInfo);
                }
            }
            MySingleTon.INSTANCE.getOrderedSongs().clear();
        } else {
            // needed to be set
            mNumberOfVideoTracks = savedInstanceState.getInt(PlayerConstants.NumberOfVideoTracksState,0);
            mNumberOfAudioTracks = savedInstanceState.getInt(PlayerConstants.NumberOfAudioTracksState);
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
                mMediaUri = savedInstanceState.getParcelable(PlayerConstants.MediaUriState,Uri.class);
            } else mMediaUri = savedInstanceState.getParcelable(PlayerConstants.MediaUriState);
            Log.d(TAG, "initializeVariablesBase.mediaUri = " + mMediaUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mPlayingParam = savedInstanceState.getParcelable(PlayerConstants.PlayingParamState, PlayingParameters.class);
            } else mPlayingParam = savedInstanceState.getParcelable(PlayerConstants.PlayingParamState);
            Log.d(TAG, "initializeVariablesBase.playingParam = " + mPlayingParam);
            if (mPlayingParam == null) {
                initializePlayingParam();
            }
            mCanShowNotSupportedFormat = savedInstanceState.getBoolean(PlayerConstants.CanShowNotSupportedFormatState);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mSingleSongInfo = savedInstanceState.getParcelable(PlayerConstants.SingleSongInfoState, SongInfo.class);
            } else mSingleSongInfo = savedInstanceState.getParcelable(PlayerConstants.SingleSongInfoState);
            Log.d(TAG, "initializeVariablesBase.singleSongInfo = " + mSingleSongInfo);
        }
    }

    public void onDurationSeekBarProgressChanged(int progress, boolean fromUser) {
        BasePlayService playService = mPresentView.getPlayService();
        if (playService == null || !playService.isSeekable()) {
            Log.d(TAG, "onDurationSeekBarProgressChanged.playService is null or not seekable");
            return;
        }
        Log.d(TAG, "onDurationSeekBarProgressChanged.progress = " + progress);
        float positionTime = progress / 1000.0f;   // seconds
        int minutes = (int)(positionTime / 60.0f);    // minutes
        int seconds = (int)positionTime - (minutes * 60);
        String durationString = String.format(Locale.ENGLISH, "%3d:%02d", minutes, seconds);
        mPresentView.setPlayingTimeTextView(durationString);
        if (fromUser) {
            // setPlayerTime(progress);
            Log.d(TAG, "onDurationSeekBarProgressChanged.playService.setPlayerTime()");
            playService.setPlayerTime(progress);
        }
        mPlayingParam.setCurrentAudioPosition(progress);
    }

    public void playLeftChannel() {
        BasePlayService playService = mPresentView.getPlayService();
        Log.d(TAG, "playLeftChannel.playService = " + playService);
        if (playService != null) {
            Log.d(TAG, "playLeftChannel.CommonConstants.LeftChannel = " + CommonConstants.LeftChannel);
            mPlayingParam.setCurrentChannelPlayed(CommonConstants.LeftChannel);
            playService.setAudioVolume(mPlayingParam.getCurrentVolume());
        }
    }

    public void playRightChannel() {
        BasePlayService playService = mPresentView.getPlayService();
        Log.d(TAG, "playRightChannel.playService = " + playService);
        if (playService != null) {
            Log.d(TAG, "playRightChannel.CommonConstants.RightChannel = " + CommonConstants.RightChannel);
            mPlayingParam.setCurrentChannelPlayed(CommonConstants.RightChannel);
            playService.setAudioVolume(mPlayingParam.getCurrentVolume());
        }
    }

    public void playStereoChannel() {
        BasePlayService playService = mPresentView.getPlayService();
        Log.d(TAG, "playStereoChannel.playService = " + playService);
        if (playService != null) {
            Log.d(TAG, "playStereoChannel.CommonConstants.StereoChannel = " + CommonConstants.StereoChannel);
            mPlayingParam.setCurrentChannelPlayed(CommonConstants.StereoChannel);
            playService.setAudioVolume(mPlayingParam.getCurrentVolume());
        }
    }

    public void startAutoPlay(boolean isSelfFinished) {
        Log.d(TAG, "startAutoPlay");
        if (mActivity.isFinishing()) {
            // activity is being destroyed
            return;
        }
        BasePlayService playService = mPresentView.getPlayService();
        Log.d(TAG, "startAutoPlay.playService = " + playService);
        if (playService != null) {
            Log.d(TAG, "startAutoPlay.playService.startAutoPlay()");
            boolean stillPlayNext = playService.startAutoPlay(this, isSelfFinished);
            Log.d(TAG, "startAutoPlay.stillPlayNext = " + stillPlayNext);
            if (!stillPlayNext) {    // still play the next song
                mPresentView.showNativeAndHideBannerAd();
            }
        }

        mPresentView.setImageButtonStatus();
    }

    public void setAutoPlayStatusAndAction() {
        Log.d(TAG, "setAutoPlayStatusAndAction");
        boolean isAutoPlay = mPlayingParam.isAutoPlay();
        if (!isAutoPlay) {
            // previous is not auto play
            ArrayList<SongInfo> songList = DatabaseAccessUtil.readSavedSongList(mActivity, true);
            MySingleTon.INSTANCE.getOrderedSongs().clear();
            MySingleTon.INSTANCE.getOrderedSongs().addAll(songList);
            // playingParam.setAutoPlay(playSongList()); // must be above autoPlay savedSongList()
            // must be above autoPlay savedSongList()
            mPlayingParam.setAutoPlay(MySingleTon.INSTANCE.getOrderedSongs().size() > 0);
            autoPlaySongList();
            mPresentView.showPlayerView();
        } else {
            // previous is auto play
            int playbackState = mPlayingParam.getCurrentPlaybackState();
            if (playbackState!=PlaybackStateCompat.STATE_NONE
                    && playbackState!=PlaybackStateCompat.STATE_STOPPED) {
                // not the following: (has not started, stopped, or finished)
                stopPlay();
            }
            mPlayingParam.setAutoPlay(false);    // must be the last in this block
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
                    , mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
            return;
        }
        int currentIndex = mPlayingParam.getCurrentSongIndex();
        int repeatStatus = mPlayingParam.getRepeatStatus();
        // because in startAutoPlay(), the next song will be current index + 1
        int lastPreviousIndex = currentIndex - 2;
        switch (repeatStatus) {
            case PlayerConstants.NoRepeatPlaying:
            case PlayerConstants.RepeatOneSong:
                if (currentIndex <= 0) {
                    // no more previous
                    ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noPreviousSongString)
                            , mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
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
        mPlayingParam.setCurrentSongIndex(currentIndex);
        startAutoPlay(false);
    }

    public void playNextSong() {
        Log.d(TAG, "playNextSong");
        int orderedSongsSize = MySingleTon.INSTANCE.getOrderedSongs().size();
        int currentIndex = mPlayingParam.getCurrentSongIndex();
        int repeatStatus = mPlayingParam.getRepeatStatus();
        if (orderedSongsSize <= 1 ) {
            // only one file in the play list
            ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noNextSongString)
                    , mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
            return; // no more next
        }
        switch (repeatStatus) {
            case PlayerConstants.NoRepeatPlaying:
            case PlayerConstants.RepeatOneSong:
                if (currentIndex >= (orderedSongsSize-1)) {
                    ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noNextSongString)
                            , mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                    return; // no more next
                }
                break;
            case PlayerConstants.RepeatAllSongs:
                break;
        }
        startAutoPlay(false);    // go to next round
    }

    public void playSongPlayedBeforeActivityCreated() {
        Log.d(TAG, "playSongPlayedBeforeActivityCreated.isPlaySingleSong = " + mPlayingParam.isPlaySingleSong());
        if (mPresentView != null) mPresentView.updateVolumeSeekBarProgress();
        if (mMediaUri == null || Uri.EMPTY.equals(mMediaUri)) {
            if (mPlayingParam.isPlaySingleSong()) {
                // called by SongListActivity
                Log.d(TAG, "playSongPlayedBeforeActivityCreated.mSingleSongInfo = " + mSingleSongInfo);
                if (mSingleSongInfo != null) {
                    mPlayingParam.setAutoPlay(false);
                    // added on 2020-12-08
                    // set orderedSongs that only contains song info from SongListActivity
                    MySingleTon.INSTANCE.getOrderedSongs().clear();
                    MySingleTon.INSTANCE.getOrderedSongs().add(mSingleSongInfo);
                    mSingleSongInfo = new SongInfo();    // reset for cycle playing
                    autoPlaySongList();
                }
            } else {
                mPlayingParam.setCurrentAudioPosition(0);
            }
        } else {
            int playbackState = mPlayingParam.getCurrentPlaybackState();
            Log.d(TAG, "playSongPlayedBeforeActivityCreated.playbackState = " + playbackState);
            if (playbackState != PlaybackStateCompat.STATE_NONE) {
                BasePlayService playService = mPresentView.getPlayService();
                if (playService != null) {
                    Log.d(TAG, "playSongPlayedBeforeActivityCreated.playService.playMediaFromUri()");
                    playService.playMediaFromUri(mMediaUri, mPlayingParam);
                }
            }
        }
        float currentPosition = mPlayingParam.getCurrentAudioPosition();
        Log.d(TAG, "playSongPlayedBeforeActivityCreated.currentPosition = " + currentPosition);
        onDurationSeekBarProgressChanged((int)currentPosition, true);
        if (mPresentView != null) {
            mPresentView.update_Player_duration_seekbar_progress((int)currentPosition);
        }
    }

    public void setRepeatSongStatus() {
        Log.d(TAG, "setRepeatSongStatus");
        int repeatStatus = mPlayingParam.getRepeatStatus();
        switch (repeatStatus) {
            case PlayerConstants.NoRepeatPlaying:
                // switch to repeat one song
                mPlayingParam.setRepeatStatus(PlayerConstants.RepeatOneSong);
                break;
            case PlayerConstants.RepeatOneSong:
                // switch to repeat song list
                mPlayingParam.setRepeatStatus(PlayerConstants.RepeatAllSongs);
                break;
            case PlayerConstants.RepeatAllSongs:
                // switch to no repeat
                mPlayingParam.setRepeatStatus(PlayerConstants.NoRepeatPlaying);
                break;
        }
        mPresentView.setImageButtonStatus();
    }

    public void startPlay() {
        Log.d(TAG, "startPlay");
        int playbackState = mPlayingParam.getCurrentPlaybackState();
        if (playbackState==PlaybackStateCompat.STATE_NONE
            || playbackState==PlaybackStateCompat.STATE_STOPPED) {
            // start playing the first song in the list
            Log.d(TAG, "startPlay.calling autoPlaySongList()");
            autoPlaySongList();
            return;
        }
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            Log.d(TAG, "startPlay.playService.startPlay() ");
            playService.startPlay(this);
        }
    }

    public void pausePlay() {
        Log.d(TAG, "pausePlay");
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            Log.d(TAG, "pausePlay.playService.pausePlay() ");
            playService.pausePlay(this);
        }
    }

    public void stopPlay() {
        Log.d(TAG, "stopPlay");
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            Log.d(TAG, "stopPlay.playService.stopPlay() ");
            playService.stopPlay(this);
        }
    }

    public void replayMedia() {
        Log.d(TAG, "replayMedia");
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            Log.d(TAG, "replayMedia.playService.replayMedia() ");
            playService.replayMedia(this);
        }
    }

    public void updateStatusAndUi(PlaybackStateCompat state) {
        Log.d(TAG, "updateStatusAndUi");
        int currentState = state.getState();
        mPlayingParam.setCurrentPlaybackState(currentState);
        Log.d(TAG, "updateStatusAndUi.playingParam.isMediaPrepared() = " +
                mPlayingParam.isMediaPrepared());
        if (currentState == PlaybackStateCompat.STATE_BUFFERING) {
            Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_BUFFERING");
            mPresentView.hideNativeAd();
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
                Log.d(TAG, "updateStatusAndUi.mPlayingParam.setCurrentAudioPosition(0)");
                mPlayingParam.setCurrentAudioPosition(0);
                mPresentView.playButtonOnPauseButtonOff();
                removeCallbacksAndMessages();
                mPlayingParam.setMediaPrepared(false);
                mPresentView.showNativeAndHideBannerAd();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                // when playing
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_PLAYING");
                if (!mPlayingParam.isMediaPrepared()) {
                    // the first time of Player.STATE_READY means prepared
                    // getPlayingMediaInfoAndSetAudioActionSubMenu();
                    setAudioActionSubMenu();
                }
                mPlayingParam.setMediaPrepared(true);  // has been prepared
                startDurationSeekBarHandler();   // start updating duration seekbar
                // set up a timer for supportToolbar's visibility
                mPresentView.setTimerToHideSupportAndAudioController();
                mPresentView.playButtonOffPauseButtonOn();
                adsForOnlyMusic();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                // when playing is paused
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_PAUSED");
                mPresentView.playButtonOnPauseButtonOff();
                mPresentView.showNativeAndHideBannerAd();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                // 1. exoPlayer finished playing
                // 2. after vlcPlayer finished playing
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_STOPPED");
                mPlayingParam.setMediaPrepared(false);
                BasePlayService playService = mPresentView.getPlayService();
                if (playService != null) {
                    Log.d(TAG, "updateStatusAndUi.update_Player_duration_seekbar_progress" +
                            "((int) playService.getMediaDuration())");
                    mPresentView.update_Player_duration_seekbar_progress((int) playService.getMediaDuration());
                }
                Log.d(TAG, "updateStatusAndUi.mPlayingParam.setCurrentAudioPosition(0)");
                mPlayingParam.setCurrentAudioPosition(0);
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
                            mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                }
                setMediaUri(null);
                // remove the song that is unable to be played
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_ERROR.orderedSongs.size() = "
                        + MySingleTon.INSTANCE.getOrderedSongs().size());
                int currentIndexOfList = mPlayingParam.getCurrentSongIndex();
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_ERROR.currentIndexOfList = "
                        + currentIndexOfList);
                if (currentIndexOfList >= 0) {
                    MySingleTon.INSTANCE.getOrderedSongs().remove(currentIndexOfList);
                    Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_ERROR.orderedSongs.remove("+
                            currentIndexOfList+")");
                    mPlayingParam.setCurrentSongIndex(--currentIndexOfList);
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

    protected void adsForOnlyMusic() {
        Log.d(TAG, "adsForOnlyMusic.getNumberOfVideoTracks() = "
                + getNumberOfVideoTracks());
        if (getNumberOfVideoTracks() == 0) {
            // no video is being played, show native ads
            mPresentView.showNativeAndHideBannerAd();
        } else {
            mPresentView.hideNativeAd();
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PlayerConstants.NumberOfVideoTracksState, mNumberOfVideoTracks);
        outState.putInt(PlayerConstants.NumberOfAudioTracksState, mNumberOfAudioTracks);
        Log.d(TAG, "saveInstanceState.orderedSongs = " + MySingleTon.INSTANCE.getOrderedSongs());
        ArrayList<SongInfo> orderedSongs = new ArrayList<>(MySingleTon.INSTANCE.getOrderedSongs());
        outState.putSerializable(PlayerConstants.OrderedSongsState, orderedSongs);
        outState.putParcelable(PlayerConstants.MediaUriState, mMediaUri);
        outState.putParcelable(PlayerConstants.PlayingParamState, mPlayingParam);
        outState.putBoolean(PlayerConstants.CanShowNotSupportedFormatState, mCanShowNotSupportedFormat);
        Log.d(TAG, "saveInstanceState.singleSongInfo = " + mSingleSongInfo);
        outState.putParcelable(PlayerConstants.SingleSongInfoState, mSingleSongInfo);
    }
}
