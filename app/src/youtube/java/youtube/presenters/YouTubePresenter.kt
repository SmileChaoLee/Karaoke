package youtube.presenters

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.utilities.LogUtil

@OptIn(UnstableApi::class)
class YouTubePresenter(val youTubePresentView: YouTubePresentView)
    : PlayerBasePresenter(youTubePresentView) {

    companion object {
        private const val TAG = "YouTubePresenter"
    }

    interface YouTubePresentView: BasePresentView {
        // fun initYouTubePlayer()
        fun setVideoWindowSize()
    }

    // implementing methods of YouTubePresenter.YouTubePresentView
    override fun initializeVariables(
        savedInstanceState: Bundle?,
        callingIntent: Intent?,
        isAutoPlay: Boolean
    ) {
        LogUtil.d(TAG, "initializeVariables")
        initializeVariablesBase(savedInstanceState, callingIntent, isAutoPlay)
    }

    override fun setAudioTrackAndChannel(audioTrackIndex: Int, audioChannel: Int) {
        LogUtil.d(TAG, "setAudioTrackAndChannel")
    }

    override fun switchAudioToMusic() {
        LogUtil.d(TAG, "switchAudioToMusic")
    }

    override fun switchAudioToVocal() {
        LogUtil.d(TAG, "switchAudioToVocal")
    }

    override fun startDurationBarHandler() {
        LogUtil.d(TAG, "startDurationBarHandler")
    }

    override fun removeMsgFromDurationBarHandler() {
        LogUtil.d(TAG, "removeMsgFromDurationBarHandler")
    }

    override fun setAudioActionSubMenu() {
        LogUtil.d(TAG, "setAudioActionSubMenu")
    }

    override fun getNumberOfAudioTracks(): Int {
        LogUtil.d(TAG, "getNumberOfAudioTracks")
        return 1    // temporary
    }
    // end of implementing methods of YouTubePresenter.YouTubePresentView
}