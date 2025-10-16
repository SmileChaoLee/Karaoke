package com.smile.karaoke.presenters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import com.smile.karaoke.constants.CommonConstants;
import com.smile.karaoke.constants.PlayerConstants;
import com.smile.karaoke.models.MySingleTon;
import com.smile.karaoke.models.PlayingParameters;
import com.smile.karaoke.models.SongInfo;
import com.smile.karaoke.services.BasePlayService;
import com.smile.karaoke.utilities.LogUtil;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

@OptIn(markerClass = UnstableApi.class)
public abstract class PlayerBasePresenter {

    private static final String TAG = "PlayerBasePresenter";
    private final BasePresentView mPresentView;
    protected Uri mMediaUri;
    protected int mNumberOfVideoTracks;
    protected PlayingParameters mPlayingParam;
    protected SongInfo mSingleSongInfo;    // when playing single song in songs list
    private boolean mCanShowNotSupportedFormat;

    public interface BasePresentView {
        void setImageButtonStatus();
        void playButtonOnPauseButtonOff();
        void playButtonOffPauseButtonOn();
        void setPlayingTimeTextView(String playingTimeString);
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
        void hidePlayerView();
        void showPlayerView();
        void setCurrentPlayerToPlayerView();
        BasePlayService getPlayService();
        void showToastNoFilesSelected();
        void showToastNoPrevious();
        void showToastNoNext();
        void showToastNotSupported();
        boolean isActivityFinishing();
        ArrayList<SongInfo> getFavoriteSongs();
        Fragment getFragment();
    }

    public abstract void initializeVariables(Bundle savedInstanceState,
                                             Intent callingIntent,
                                             boolean isAutoPlay);
    public abstract void setAudioTrackAndChannel(int audioTrackIndex, int audioChannel);
    public abstract void switchAudioToMusic();
    public abstract void switchAudioToVocal();
    public abstract void startDurationBarHandler();
    public abstract void removeMsgFromDurationBarHandler();
    public abstract void setAudioActionSubMenu();
    public abstract int getNumberOfAudioTracks();

    public PlayerBasePresenter(BasePresentView presentView) {
        LogUtil.d(TAG, "PlayerBasePresenter() constructor is called.");
        mPresentView = presentView;
    }

    public Activity getActivity() {
        return mPresentView.getFragment().getActivity();
    }
    public Uri getMediaUri() {
        return mMediaUri;
    }
    public void setMediaUri(Uri mediaUri) {
        this.mMediaUri = mediaUri;
    }
    public int getNumberOfVideoTracks() {
        return mNumberOfVideoTracks;
    }
    public PlayingParameters getPlayingParam() {
        return mPlayingParam;
    }

    public void setPlayingParameters(SongInfo songInfo) {
        mPlayingParam.setInSongList(Objects.equals(songInfo.getIncluded(), "1"));
        mPlayingParam.setMusicAudioTrackIndex(songInfo.getMusicTrackNo());
        mPlayingParam.setMusicAudioChannel(songInfo.getMusicChannel());
        mPlayingParam.setVocalAudioTrackIndex(songInfo.getVocalTrackNo());
        mPlayingParam.setVocalAudioChannel(songInfo.getVocalChannel());
        LogUtil.d(TAG, "setPlayingParameters.(songInfo == singleSongInfo) = " + (songInfo == mSingleSongInfo));
        if (songInfo != mSingleSongInfo) {
            mPlayingParam.setCurrentAudioTrackIndexPlayed(songInfo.getVocalTrackNo());
            mPlayingParam.setCurrentChannelPlayed(songInfo.getVocalChannel());
            mSingleSongInfo = songInfo;
        }
    }

    public void autoPlaySongList() {
        LogUtil.d(TAG, "autoPlaySongList.orderedSongs.size = "
                + MySingleTon.INSTANCE.getOrderedSongs().size());
        mCanShowNotSupportedFormat = true;
        if (!MySingleTon.INSTANCE.getOrderedSongs().isEmpty()) {
            // next song that will be played, which the index is 0
            // start playing video from list
            mPlayingParam.setCurrentSongIndex(-1);
            BasePlayService playService = mPresentView.getPlayService();
            LogUtil.d(TAG, "autoPlaySongList.playService = " + playService);
            boolean isPlaying = playService != null && playService.isPlaying();
            LogUtil.d(TAG, "autoPlaySongList.isPlaying = " + isPlaying);
            if (isPlaying && playService.isSeekable()) {
                LogUtil.d(TAG, "autoPlaySongList.stopPlay(PlayerConstants.FINISHED_BY_PROGRAM)");
                stopPlay(PlayerConstants.FINISHED_BY_PROGRAM);
            } else {
                LogUtil.d(TAG, "autoPlaySongList.startAutoPlay(false)");
                startAutoPlay(false);
            }
        } else {
            mPresentView.showToastNoFilesSelected();
        }
    }

    private void initializePlayingParam() {
        LogUtil.i(TAG, "initializePlayingParam");
        mPlayingParam = new PlayingParameters();
    }

    @SuppressWarnings("unchecked")
    protected void initializeVariablesBase(Bundle savedInstanceState, Intent callingIntent,
                                           boolean isAutoPlay) {
        LogUtil.i(TAG, "initializeVariablesBase.savedInstanceState = " + savedInstanceState);
        LogUtil.d(TAG, "initializeVariablesBase.isAutoPlay = " + isAutoPlay);
        if (savedInstanceState == null) {
            mNumberOfVideoTracks = 0;
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
                        mSingleSongInfo = arguments.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE,
                                SongInfo.class);
                    else mSingleSongInfo = arguments.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE);
                    LogUtil.d(TAG, "initializeVariablesBase.singleSongInfo = " + mSingleSongInfo);
                }
            }
        } else {
            // needed to be set
            mNumberOfVideoTracks = savedInstanceState.getInt(PlayerConstants.NumberOfVideoTracksState,0);
            ArrayList<SongInfo> orderedSongs;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                orderedSongs = (ArrayList<SongInfo>)savedInstanceState.getSerializable(PlayerConstants.OrderedSongsState, ArrayList.class);
            } else orderedSongs = (ArrayList<SongInfo>)savedInstanceState.getSerializable(PlayerConstants.OrderedSongsState);
            LogUtil.d(TAG, "initializeVariablesBase.orderedSongs = " + orderedSongs);
            if (orderedSongs != null) {
                MySingleTon.INSTANCE.getOrderedSongs().clear();
                MySingleTon.INSTANCE.getOrderedSongs().addAll(orderedSongs);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mMediaUri = savedInstanceState.getParcelable(PlayerConstants.MediaUriState,
                        Uri.class);
            } else mMediaUri = savedInstanceState.getParcelable(PlayerConstants.MediaUriState);
            LogUtil.d(TAG, "initializeVariablesBase.mediaUri = " + mMediaUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mPlayingParam = savedInstanceState.getParcelable(PlayerConstants.PlayingParamState,
                        PlayingParameters.class);
            } else mPlayingParam = savedInstanceState.getParcelable(PlayerConstants.PlayingParamState);
            LogUtil.d(TAG, "initializeVariablesBase.playingParam = " + mPlayingParam);
            if (mPlayingParam == null) {
                initializePlayingParam();
            }
            mCanShowNotSupportedFormat = savedInstanceState.getBoolean(PlayerConstants.CanShowNotSupportedFormatState);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mSingleSongInfo = savedInstanceState.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE,
                        SongInfo.class);
            } else mSingleSongInfo = savedInstanceState.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE);
            LogUtil.d(TAG, "initializeVariablesBase.singleSongInfo = " + mSingleSongInfo);
        }
        mPlayingParam.setAutoPlay(isAutoPlay);
    }

    public void onDurationSeekBarProgressChanged(int progress, boolean fromUser) {
        String msgString = "onDurationSeekBarProgressChanged";
        LogUtil.d(TAG, msgString);
        BasePlayService playService = mPresentView.getPlayService();
        if (playService == null) {
            LogUtil.d(TAG, msgString + ".playService is null");
            return;
        }
        if (!playService.isSeekable()) {
            LogUtil.d(TAG, msgString + ".not seekable");
            return;
        }
        LogUtil.d(TAG, msgString + ".progress = " + progress);
        float positionTime = progress / 1000.0f;   // seconds
        int minutes = (int)(positionTime / 60.0f);    // minutes
        int seconds = (int)positionTime - (minutes * 60);
        String playingTimeString = String.format(Locale.ENGLISH,
                "%3d:%02d", minutes, seconds);
        mPresentView.setPlayingTimeTextView(playingTimeString);
        if (fromUser) {
            LogUtil.d(TAG, msgString + ".playService.setPlayerTime()");
            playService.setPlayerTime(progress);
        }
        mPlayingParam.setCurrentAudioPosition(progress);
    }

    public void playLeftChannel() {
        BasePlayService playService = mPresentView.getPlayService();
        LogUtil.d(TAG, "playLeftChannel.playService = " + playService);
        if (playService != null) {
            LogUtil.d(TAG, "playLeftChannel.CommonConstants.LeftChannel = " + CommonConstants.LEFT_CHANNEL);
            mPlayingParam.setCurrentChannelPlayed(CommonConstants.LEFT_CHANNEL);
            playService.setAudioVolume(mPlayingParam.getCurrentVolume());
        }
    }

    public void playRightChannel() {
        BasePlayService playService = mPresentView.getPlayService();
        LogUtil.d(TAG, "playRightChannel.playService = " + playService);
        if (playService != null) {
            LogUtil.d(TAG, "playRightChannel.CommonConstants.RightChannel = " + CommonConstants.RIGHT_CHANNEL);
            mPlayingParam.setCurrentChannelPlayed(CommonConstants.RIGHT_CHANNEL);
            playService.setAudioVolume(mPlayingParam.getCurrentVolume());
        }
    }

    public void playStereoChannel() {
        BasePlayService playService = mPresentView.getPlayService();
        LogUtil.d(TAG, "playStereoChannel.playService = " + playService);
        if (playService != null) {
            LogUtil.d(TAG, "playStereoChannel.CommonConstants.StereoChannel = " + CommonConstants.STEREO);
            mPlayingParam.setCurrentChannelPlayed(CommonConstants.STEREO);
            playService.setAudioVolume(mPlayingParam.getCurrentVolume());
        }
    }

    public void startAutoPlay(boolean isSelfFinished) {
        LogUtil.d(TAG, "startAutoPlay");
        if (mPresentView.isActivityFinishing()) {
            // activity is being destroyed
            LogUtil.d(TAG, "startAutoPlay.activity is finishing");
            return;
        }
        BasePlayService playService = mPresentView.getPlayService();
        LogUtil.d(TAG, "startAutoPlay.playService = " + playService);
        if (playService != null) {
            LogUtil.d(TAG, "startAutoPlay.playService.startAutoPlay()");
            boolean stillPlayNext = playService.startAutoPlay(this, isSelfFinished);
            LogUtil.d(TAG, "startAutoPlay.stillPlayNext = " + stillPlayNext);
            if (!stillPlayNext) {    // no more playing the next song
                mPresentView.showNativeAndHideBannerAd();
            }
        }
        mPresentView.setImageButtonStatus();
    }

    public boolean setAutoPlayStatusAndAction() {
        ArrayList<SongInfo> songList = mPresentView.getFavoriteSongs();
        LogUtil.d(TAG, "setAutoPlayStatusAndAction.songList.size() = " + songList.size());
        boolean isAutoPlay = false;
        if (!songList.isEmpty()) {
            MySingleTon.INSTANCE.getOrderedSongs().clear();
            MySingleTon.INSTANCE.getOrderedSongs().addAll(songList);
            mPlayingParam.setAutoPlay(true);
            autoPlaySongList();
            mPresentView.showPlayerView();
            mPresentView.setImageButtonStatus();
        }
        return isAutoPlay;
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
        mPresentView.setImageButtonStatus();
    }

    public void playPreviousSong() {
        LogUtil.d(TAG, "playPreviousSong");
        int orderedSongsSize = MySingleTon.INSTANCE.getOrderedSongs().size();
        if (orderedSongsSize <= 1 ) {
            LogUtil.d(TAG, "playPreviousSong.orderedSongsSize <= 1, only one song in the list");
            // only one file in the play list
            mPresentView.showToastNoPrevious();
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
                    LogUtil.d(TAG, "playPreviousSong.currentIndex <= 0, current is the first one.");
                    // no more previous
                    mPresentView.showToastNoPrevious();
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
        LogUtil.d(TAG, "playNextSong");
        int orderedSongsSize = MySingleTon.INSTANCE.getOrderedSongs().size();
        int currentIndex = mPlayingParam.getCurrentSongIndex();
        int repeatStatus = mPlayingParam.getRepeatStatus();
        if (orderedSongsSize <= 1 ) {
            // only one file in the play list
            LogUtil.d(TAG, "playNextSong.orderedSongsSize <= 1, only one song in the list");
            // no more next
            mPresentView.showToastNoNext();
            return;
        }
        switch (repeatStatus) {
            case PlayerConstants.NoRepeatPlaying:
            case PlayerConstants.RepeatOneSong:
                if (currentIndex >= (orderedSongsSize-1)) {
                    LogUtil.d(TAG, "playPreviousSong.currentIndex >= (orderedSongsSize-1)," +
                            " current is the last one.");
                    // no more next
                    mPresentView.showToastNoNext();
                    return;
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
        LogUtil.i(TAG, "playSongPlayedBeforeActivityCreated.isPlaySingleSong = "
                + mPlayingParam.isPlaySingleSong());
        LogUtil.d(TAG, "playSongPlayedBeforeActivityCreated.preparedStatus = "
                + mPlayingParam.getPreparedStatus());
        mPresentView.updateVolumeSeekBarProgress();
        LogUtil.d(TAG, "playSongPlayedBeforeActivityCreated.mMediaUri = " + mMediaUri);
        if (mMediaUri == null || Uri.EMPTY.equals(mMediaUri)) {
            if (mPlayingParam.isPlaySingleSong()) {
                // called by SongListActivity
                LogUtil.d(TAG, "playSongPlayedBeforeActivityCreated.mSingleSongInfo = " + mSingleSongInfo);
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
            LogUtil.d(TAG, "playSongPlayedBeforeActivityCreated.playbackState = "
                    + playbackState);
            if (playbackState != PlayerConstants.PREPARE_MEDIA) {
                BasePlayService playService = mPresentView.getPlayService();
                if (playService != null) {
                    LogUtil.d(TAG, "playSongPlayedBeforeActivityCreated.playService.playMediaFromUri()");
                    playService.playMediaFromUri(mMediaUri, mPlayingParam);
                }
            }
        }
        float currentPosition = mPlayingParam.getCurrentAudioPosition();
        LogUtil.d(TAG, "playSongPlayedBeforeActivityCreated.currentPosition = " + currentPosition);
        onDurationSeekBarProgressChanged((int)currentPosition, true);
        mPresentView.update_Player_duration_seekbar_progress((int)currentPosition);
    }

    public void setRepeatSongStatus() {
        LogUtil.d(TAG, "setRepeatSongStatus");
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
        LogUtil.i(TAG, "startPlay");
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            LogUtil.d(TAG, "startPlay.playService.startPlay() ");
            playService.startPlay(this);
        }
    }

    public void pausePlay() {
        LogUtil.i(TAG, "pausePlay");
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            LogUtil.d(TAG, "pausePlay.playService.pausePlay() ");
            playService.pausePlay();
        }
    }

    public void stopPlay(int finishState) {
        LogUtil.i(TAG, "stopPlay.finishState = " + finishState);
        String state;
        if (finishState == PlayerConstants.FINISHED_NORMALLY) {
            state = "FINISHED_NORMALLY";
        } else if (finishState == PlayerConstants.STOPPED_BY_USER) {
            state = "STOPPED_BY_USER";
        } else {
            // finishState == PlayerConstants.FINISHED_BY_PROGRAM
            state = "FINISHED_BY_PROGRAM";
        }
        LogUtil.d(TAG, "stopPlay.finishState String = " + state);
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            LogUtil.d(TAG, "stopPlay.playService.stopPlay()");
            mPlayingParam.setFinishState(finishState);
            playService.stopPlay();
        }
    }

    public void replayMedia() {
        LogUtil.i(TAG, "replayMedia");
        BasePlayService playService = mPresentView.getPlayService();
        if (playService != null) {
            LogUtil.d(TAG, "replayMedia.playService.replayMedia() ");
            playService.replayMedia(this);
        }
    }

    public void updateStatusAndUi(PlaybackStateCompat state) {
        String msgStr = "updateStatusAndUi";
        LogUtil.d(TAG, msgStr + ".playingParam.preparedStatus = " +
                mPlayingParam.getPreparedStatus());
        int currentState = state.getState();
        // update the playback state
        mPlayingParam.setCurrentPlaybackState(currentState);
        if (mPlayingParam.isPlaySingleSong()
                && mPlayingParam.getSingleSongPlayingStatus() == 1) {
            LogUtil.d(TAG, msgStr + ".setSingleSongPlayingStatus(2)");
            mPlayingParam.setSingleSongPlayingStatus(2);    // prepared and playing
        }
        if (currentState == PlaybackStateCompat.STATE_BUFFERING) {
            // Only for ExoPlayer
            LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_BUFFERING");
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
                LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_NONE");
                if (mPlayingParam.getPreparedStatus() == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu();
                }
                mPlayingParam.setPreparedStatus(0);
                onDurationSeekBarProgressChanged(0, true);
                mPresentView.update_Player_duration_seekbar_progress(0);
                mPlayingParam.setCurrentAudioPosition(0);
                mPresentView.playButtonOnPauseButtonOff();
                removeMsgFromDurationBarHandler();
                // mPresentView.showNativeAndHideBannerAd();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                // when playing
                LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_PLAYING");
                LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_PLAYING");
                if (mPlayingParam.getPreparedStatus() == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu();
                }
                mPlayingParam.setPreparedStatus(2);  // has been prepared and playing
                mPlayingParam.setCurrentPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                startDurationBarHandler();   // start updating duration seekbar
                // set up a timer for supportToolbar's visibility
                mPresentView.setTimerToHideSupportAudioControl();
                mPresentView.playButtonOffPauseButtonOn();
                mPresentView.hideNativeAd();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                // when playing is paused
                LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_PAUSED");
                if (mPlayingParam.getPreparedStatus() == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu();
                }
                // new add, need to be tested more, especially ExoPlayer
                // VlcPlayer has already been tested but keep an eye on it
                mPlayingParam.setPreparedStatus(2);
                //
                mPresentView.playButtonOnPauseButtonOff();
                // mPresentView.showNativeAndHideBannerAd();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                // 1. exoPlayer finished playing
                // 2. after vlcPlayer finished playing
                LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_STOPPED");
                if (mPlayingParam.getPreparedStatus() == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu();
                }
                mPlayingParam.setPreparedStatus(0);
                BasePlayService playService = mPresentView.getPlayService();
                LogUtil.d(TAG, msgStr + ".playService = " + playService);
                if (playService != null) {
                    mPresentView.update_Player_duration_seekbar_progress(
                            (int) playService.getMediaDuration());
                }
                mPlayingParam.setCurrentAudioPosition(0);
                mPresentView.playButtonOnPauseButtonOff();
                removeMsgFromDurationBarHandler();
                LogUtil.d(TAG, msgStr + ".mPlayingParam.getFinishState() = " +
                        mPlayingParam.getFinishState());
                // not finished by pressing playPreviousSong or PlayNextSong buttons
                boolean isSelfFinished =
                        mPlayingParam.getFinishState() != PlayerConstants.FINISHED_BY_PROGRAM;
                startAutoPlay(isSelfFinished);
                break;
            case PlaybackStateCompat.STATE_ERROR:
                if (mCanShowNotSupportedFormat) {
                    // only show once
                    mCanShowNotSupportedFormat = false;
                    mPresentView.showToastNotSupported();
                }
                mPlayingParam.setPreparedStatus(0);
                setMediaUri(null);
                // remove the song that is unable to be played
                LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_ERROR.orderedSongs.size() = "
                        + MySingleTon.INSTANCE.getOrderedSongs().size());
                int currentIndexOfList = mPlayingParam.getCurrentSongIndex();
                LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_ERROR.currentIndexOfList = "
                        + currentIndexOfList);
                if (currentIndexOfList >= 0) {
                    MySingleTon.INSTANCE.getOrderedSongs().remove(currentIndexOfList);
                    mPlayingParam.setCurrentSongIndex(--currentIndexOfList);
                }
                LogUtil.d(TAG, msgStr + ".PlaybackStateCompat.STATE_ERROR.orderedSongs.size() = "
                        + MySingleTon.INSTANCE.getOrderedSongs().size());
                startAutoPlay(false);
                break;
            default:
                LogUtil.d(TAG, msgStr + ".other PlaybackStateCompat");
        }
        // reset the finish state
        mPlayingParam.setFinishState(PlayerConstants.FINISHED_NORMALLY);
        mPresentView.showNativeAndHideBannerAd();
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PlayerConstants.NumberOfVideoTracksState, mNumberOfVideoTracks);
        LogUtil.i(TAG, "saveInstanceState.orderedSongs = " + MySingleTon.INSTANCE.getOrderedSongs());
        ArrayList<SongInfo> orderedSongs = new ArrayList<>(MySingleTon.INSTANCE.getOrderedSongs());
        outState.putSerializable(PlayerConstants.OrderedSongsState, orderedSongs);
        outState.putParcelable(PlayerConstants.MediaUriState, mMediaUri);
        outState.putParcelable(PlayerConstants.PlayingParamState, mPlayingParam);
        outState.putBoolean(PlayerConstants.CanShowNotSupportedFormatState, mCanShowNotSupportedFormat);
        LogUtil.d(TAG, "saveInstanceState.singleSongInfo = " + mSingleSongInfo);
        outState.putParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE, mSingleSongInfo);
    }
}
