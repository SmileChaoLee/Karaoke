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
import androidx.core.os.BundleCompat;

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

public abstract class PlayerBasePresenter {

    private static final String TAG = "PlayerBasePresenter";
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
        void setTimerToHideSupportAudioControl();
        void showMusicAndVocalIsNotSet();
        void showInterstitialAd(boolean isSelfFinished);
        void hidePlayerView();
        void showPlayerView();
        void setCurrentPlayerToPlayerView();
        BasePlayService getPlayService();
    }
    public abstract void initializeVariables(Bundle savedInstanceState,
                                             Intent callingIntent,
                                             boolean isAutoPlay);
    public abstract void setAudioVolumeInsideVolumeSeekBar(int i);
    public abstract int getCurrentProgressForVolumeSeekBar();
    public abstract void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel);
    public abstract void switchAudioToMusic();
    public abstract void switchAudioToVocal();
    public abstract void startDurationSeekBarHandler();
    public abstract void removeCallbacksAndMessages();
    public abstract void setAudioActionSubMenu();

    public PlayerBasePresenter(PlayerBaseViewFragment fragment, BasePresentView presentView) {
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
        Log.d(TAG, "autoPlaySongList.orderedSongs.size = "
                + MySingleTon.INSTANCE.getOrderedSongs().size());
        mCanShowNotSupportedFormat = true;
        if (!MySingleTon.INSTANCE.getOrderedSongs().isEmpty()) {
            mPlayingParam.setCurrentSongIndex(-1); // next song that will be played, which the index is 0
            // start playing video from list
            BasePlayService playService = mPresentView.getPlayService();
            Log.d(TAG, "autoPlaySongList.playService = " + playService);
            boolean isPlaying = playService != null && playService.isPlaying();
            Log.d(TAG, "autoPlaySongList.isPlaying = " + isPlaying);
            if (isPlaying && playService.isSeekable()) {
                /*
                mPlayingParam.setFinishState(2);
                playService.onPlay();
                playService.setPlayerTime(playService.getMediaDuration());
                 */
                stopPlay(PlayerConstants.FINISHED_BY_PROGRAM);
            } else {
                // paused, buffering, stopped, finished
                startAutoPlay(false);
            }
        } else {
            ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noFilesSelectedString)
                    , mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
        }
    }

    private void initializePlayingParam() {
        Log.d(TAG, "initializePlayingParam");
        mPlayingParam = new PlayingParameters();
    }

    @SuppressWarnings("unchecked")
    protected void initializeVariablesBase(Bundle savedInstanceState, Intent callingIntent,
                                           boolean isAutoPlay) {
        Log.d(TAG, "initializeVariablesBase.savedInstanceState = " + savedInstanceState);
        Log.d(TAG, "initializeVariablesBase.isAutoPlay = " + isAutoPlay);
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
                            .getBoolean(PlayerConstants.IS_PLAY_SINGLE_SONG_STATE, true));
                    mPlayingParam.setCurrentVolume(arguments
                            .getFloat(PlayerConstants.SingleSongVolume, mPlayingParam.getCurrentVolume()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        mSingleSongInfo = BundleCompat.getParcelable(arguments, PlayerConstants.SINGLE_SONG_INFO_STATE,
                                SongInfo.class);
                    else mSingleSongInfo = arguments.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE);
                    Log.d(TAG, "initializeVariablesBase.singleSongInfo = " + mSingleSongInfo);
                }
            }
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
                mMediaUri = BundleCompat.getParcelable(savedInstanceState,
                        PlayerConstants.MediaUriState, Uri.class);
            } else mMediaUri = savedInstanceState.getParcelable(PlayerConstants.MediaUriState);
            Log.d(TAG, "initializeVariablesBase.mediaUri = " + mMediaUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mPlayingParam = BundleCompat.getParcelable(savedInstanceState,
                        PlayerConstants.PlayingParamState, PlayingParameters.class);
            } else mPlayingParam = savedInstanceState.getParcelable(PlayerConstants.PlayingParamState);
            Log.d(TAG, "initializeVariablesBase.playingParam = " + mPlayingParam);
            if (mPlayingParam == null) {
                initializePlayingParam();
            }
            mCanShowNotSupportedFormat = savedInstanceState.getBoolean(PlayerConstants.CanShowNotSupportedFormatState);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mSingleSongInfo = BundleCompat.getParcelable(savedInstanceState,
                        PlayerConstants.SINGLE_SONG_INFO_STATE, SongInfo.class);
            } else mSingleSongInfo = savedInstanceState.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE);
            Log.d(TAG, "initializeVariablesBase.singleSongInfo = " + mSingleSongInfo);
        }
        mPlayingParam.setAutoPlay(isAutoPlay);
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

    public boolean startAutoPlay(boolean isSelfFinished) {
        Log.d(TAG, "startAutoPlay");
        boolean stillPlayNext = false;
        mPlayingParam.setFinishState(PlayerConstants.FINISHED_NORMALLY);    // default value for start playing a video
        if (mActivity.isFinishing()) {
            // activity is being destroyed
            return stillPlayNext;
        }
        BasePlayService playService = mPresentView.getPlayService();
        Log.d(TAG, "startAutoPlay.playService = " + playService);
        if (playService != null) {
            Log.d(TAG, "startAutoPlay.playService.startAutoPlay()");
            stillPlayNext = playService.startAutoPlay(this, isSelfFinished);
            Log.d(TAG, "startAutoPlay.stillPlayNext = " + stillPlayNext);
            if (!stillPlayNext) {    // no more playing the next song
                if (mPlayingParam.getNumPlayed() > 0) {
                    // did not show interstitial Ad before finishing playing
                    try {
                        mPresentView.showNativeAndHideBannerAd();
                    } catch (Exception ex) {
                        Log.d(TAG, "startAutoPlay.Exception form showNativeAndHideBannerAd(): "
                                + ex);
                    }
                }
            }
        }
        mPresentView.setImageButtonStatus();

        return stillPlayNext;
    }

    // no longer used
    private void setAutoPlayStatusAndAction() {
        Log.d(TAG, "setAutoPlayStatusAndAction");
        ArrayList<SongInfo> songList = DatabaseAccessUtil.readSavedSongList(mActivity, true);
        MySingleTon.INSTANCE.getOrderedSongs().clear();
        MySingleTon.INSTANCE.getOrderedSongs().addAll(songList);
        mPlayingParam.setAutoPlay(!MySingleTon.INSTANCE.getOrderedSongs().isEmpty());
        autoPlaySongList();
        mPresentView.showPlayerView();
        mPresentView.setImageButtonStatus();
    }

    public void stopAutoPlay() {
        int playbackState = mPlayingParam.getCurrentPlaybackState();
        if (playbackState!=PlayerConstants.PREPARE_MEDIA
                && playbackState!=PlaybackStateCompat.STATE_NONE
                && playbackState!=PlaybackStateCompat.STATE_STOPPED) {
            // not the following: (has not started, stopped, or finished)
            stopPlay(PlayerConstants.STOPPED_BY_USER);
        }
        mPlayingParam.setAutoPlay(false);    // must be the last in this block
        // mPresentView.hidePlayerView();
        mPresentView.setImageButtonStatus();
    }

    public void playPreviousSong() {
        Log.d(TAG, "playPreviousSong");
        int orderedSongsSize = MySingleTon.INSTANCE.getOrderedSongs().size();
        if (orderedSongsSize <= 1 ) {
            Log.d(TAG, "playPreviousSong.orderedSongsSize <= 1, only one song in the list");
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
                    Log.d(TAG, "playPreviousSong.currentIndex <= 0, current is the first one.");
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
        if (mPlayingParam.getCurrentPlaybackState() == PlaybackStateCompat.STATE_PLAYING
                || mPlayingParam.getCurrentPlaybackState() == PlaybackStateCompat.STATE_PAUSED) {
            stopPlay(PlayerConstants.FINISHED_BY_PROGRAM);
        } else {
            startAutoPlay(false);
        }
    }

    public void playNextSong() {
        Log.d(TAG, "playNextSong");
        int orderedSongsSize = MySingleTon.INSTANCE.getOrderedSongs().size();
        int currentIndex = mPlayingParam.getCurrentSongIndex();
        int repeatStatus = mPlayingParam.getRepeatStatus();
        if (orderedSongsSize <= 1 ) {
            // only one file in the play list
            Log.d(TAG, "playNextSong.orderedSongsSize <= 1, only one song in the list");
            ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noNextSongString)
                    , mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
            return; // no more next
        }
        switch (repeatStatus) {
            case PlayerConstants.NoRepeatPlaying:
            case PlayerConstants.RepeatOneSong:
                if (currentIndex >= (orderedSongsSize-1)) {
                    Log.d(TAG, "playPreviousSong.currentIndex >= (orderedSongsSize-1)," +
                            " current is the last one.");
                    ScreenUtil.showToast(mActivity, mActivity.getString(R.string.noNextSongString)
                            , mToastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                    return; // no more next
                }
                break;
            case PlayerConstants.RepeatAllSongs:
                break;
        }
        // mPlayingParam.setCurrentSongIndex(currentIndex); no need because it already is
        if (mPlayingParam.getCurrentPlaybackState() == PlaybackStateCompat.STATE_PLAYING
            || mPlayingParam.getCurrentPlaybackState() == PlaybackStateCompat.STATE_PAUSED) {
            stopPlay(PlayerConstants.FINISHED_BY_PROGRAM);
        } else {
            startAutoPlay(false);
        }
    }

    public void playSongPlayedBeforeActivityCreated() {
        Log.d(TAG, "playSongPlayedBeforeActivityCreated.isPlaySingleSong = "
                + mPlayingParam.isPlaySingleSong());
        Log.d(TAG, "playSongPlayedBeforeActivityCreated.preparedStatus = "
                + mPlayingParam.getPreparedStatus());
        if (mPresentView != null) mPresentView.updateVolumeSeekBarProgress();
        Log.d(TAG, "playSongPlayedBeforeActivityCreated.mMediaUri = " + mMediaUri);
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
            // if (playbackState != PlaybackStateCompat.STATE_NONE) {
            if (playbackState != PlayerConstants.PREPARE_MEDIA) {
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
        /*
        int playbackState = mPlayingParam.getCurrentPlaybackState();
        if (playbackState==PlayerConstants.PREPARE_MEDIA
            || playbackState==PlaybackStateCompat.STATE_NONE
            || playbackState==PlaybackStateCompat.STATE_STOPPED) {
            // start playing the first song in the list
            Log.d(TAG, "startPlay.calling autoPlaySongList()");
            autoPlaySongList();
            return;
        }
        */
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

    public void stopPlay(int finishState) {
        Log.d(TAG, "stopPlay.finishState = " + finishState);
        String state;
        if (finishState == 0) {
            state = "FINISHED_NORMALLY";
        } else if (finishState == 1) {
            state = "STOPPED_BY_USER";
        } else {
            state = "FINISHED_BY_PROGRAM";
        }
        Log.d(TAG, "stopPlay.finishState String = " + state);
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            Log.d(TAG, "stopPlay.playService.stopPlay() ");
            playService.stopPlay(this);
            mPlayingParam.setFinishState(finishState);
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
        Log.d(TAG, "updateStatusAndUi.playingParam.preparedStatus = " +
                mPlayingParam.getPreparedStatus());
        if (mPlayingParam.isPlaySingleSong() && mPlayingParam.getSingleSongPlayingStatus() == 1) {
            Log.d(TAG, "updateStatusAndUi.setSingleSongPlayingStatus(2)");
            mPlayingParam.setSingleSongPlayingStatus(2);    // prepared and playing
        }
        if (currentState == PlaybackStateCompat.STATE_BUFFERING) {
            // Only for ExoPlayer
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
                if (mPlayingParam.getPreparedStatus() == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu();
                }
                onDurationSeekBarProgressChanged(0, true); // set time to 0 position
                mPresentView.update_Player_duration_seekbar_progress(0);
                Log.d(TAG, "updateStatusAndUi.mPlayingParam.setCurrentAudioPosition(0)");
                mPlayingParam.setCurrentAudioPosition(0);
                mPresentView.playButtonOnPauseButtonOff();
                removeCallbacksAndMessages();
                mPlayingParam.setPreparedStatus(0);
                mPresentView.showNativeAndHideBannerAd();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                // when playing
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_PLAYING");
                if (mPlayingParam.getPreparedStatus() == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu();
                }
                mPlayingParam.setPreparedStatus(2);  // has been prepared and playing
                mPlayingParam.setCurrentPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                startDurationSeekBarHandler();   // start updating duration seekbar
                // set up a timer for supportToolbar's visibility
                mPresentView.setTimerToHideSupportAudioControl();
                mPresentView.playButtonOffPauseButtonOn();
                // adsForOnlyMusic();
                mPresentView.hideNativeAd();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                // when playing is paused
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_PAUSED");
                if (mPlayingParam.getPreparedStatus() == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu();
                }
                mPresentView.playButtonOnPauseButtonOff();
                mPresentView.showNativeAndHideBannerAd();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                // 1. exoPlayer finished playing
                // 2. after vlcPlayer finished playing
                Log.d(TAG, "updateStatusAndUi.PlaybackStateCompat.STATE_STOPPED");
                if (mPlayingParam.getPreparedStatus() == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu();
                }
                mPlayingParam.setPreparedStatus(0);
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
                Log.d(TAG, "updateStatusAndUi.mPlayingParam.getFinishState() = " +
                        mPlayingParam.getFinishState());
                // not finished by pressing playPreviousSong or PlayNextSong buttons
                final boolean isSelfFinished = mPlayingParam.getFinishState() != PlayerConstants.FINISHED_BY_PROGRAM;
                if (isSelfFinished) {
                    mPlayingParam.setNumPlayed(mPlayingParam.getNumPlayed() + 1);
                }
                Log.d(TAG, "updateStatusAndUi.mPlayingParam.getNumPlayed() = " + mPlayingParam.getNumPlayed());
                if (mPlayingParam.getNumPlayed() >= PlayerConstants.SHOW_INTERSTITIAL_AFTER_NUM_SONGS) {
                    // show interstitial ad after 10 songs
                    mPlayingParam.setNumPlayed(0);
                    mPresentView.showInterstitialAd(isSelfFinished);
                } else {
                    startAutoPlay(isSelfFinished);
                }
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
        outState.putParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE, mSingleSongInfo);
    }
}
