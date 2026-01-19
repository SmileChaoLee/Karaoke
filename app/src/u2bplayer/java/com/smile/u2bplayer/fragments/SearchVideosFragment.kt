package com.smile.u2bplayer.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.scale

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.BuildConfig
import com.smile.karaoke.R
import com.smile.karaoke.adapters.MyLayoutManager
import com.smile.karaoke.fragments.ItemsBaseFragment
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.ImageUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bplayer.utilities.U2bPlayerUtil
import com.smile.u2bplayer.adapters.U2bRecyclerAdapter
import com.smile.u2bplayer.models.U2bSingleton
import com.smile.u2bplayer.retrofit.RestApiSync
import com.smile.u2bplayer.u2bplay_constants.U2bPlayConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SearchVideosFragment : ItemsBaseFragment(), RecyclerItemListener {

    companion object {
        private const val TAG : String = "SearchVideosFragment"
    }
    private var searchButton: ImageButton? = null
    private var selectAllButton: ImageButton? = null
    private var unselectButton: ImageButton? = null
    private var playSelectedButton: ImageButton? = null
    private var addToFavoriteButton: ImageButton? = null
    private var searchEditTextView: EditText? = null
    private var searchRecyclerView: RecyclerView? = null
    private var loadingMsgTextView: TextView? = null
    private var myRecyclerViewAdapter : U2bRecyclerAdapter? = null
    private val selectedSongs : ArrayList<SongInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        // U2bSingleton.videos.clear() moved to U2bPlayerActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_youtube_video,
            container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")

        view.let {
            searchEditTextView = it.findViewById(R.id.searchEditTextView)
            ScreenUtil.resizeTextSize(searchEditTextView, textFontSize)
            searchButton = it.findViewById(R.id.searchButton)
            selectAllButton = it.findViewById(R.id.searchSelectAllButton)
            unselectButton = it.findViewById(R.id.searchUnselectButton)
            playSelectedButton = it.findViewById(R.id.searchPlaySelectedButton)
            addToFavoriteButton = it.findViewById(R.id.addToFavoriteButton)
            showVideoButton = it.findViewById(R.id.showVideoImageButton)
            showVideoButton?.visibility = View.VISIBLE
            exitImageButton = it.findViewById(R.id.exitImageButton)
            exitImageButton?.visibility = View.VISIBLE
            loadingMsgTextView = it.findViewById(R.id.loadingMsgTextView)
            ScreenUtil.resizeTextSize(loadingMsgTextView, textFontSize * 2f)
            loadingMsgTextView?.visibility = View.GONE
            searchRecyclerView = it.findViewById(R.id.searchRecyclerView)
            searchRecyclerView?.setHasFixedSize(true)
            searchRecyclerView?.visibility = View.GONE
        }
        initRecyclerAdapter()
        activity?.let { act ->
            var searchTerm = "Most Popular"
            try {
                val fis = act.openFileInput(U2bPlayConstants.KEYWORD_FILENAME)
                val isr = InputStreamReader(fis)
                val br = BufferedReader(isr)
                searchTerm = br.readLine()  // last video id played
                br.close()
                isr.close()
                fis.close()
            } catch (ex: Exception) {
                LogUtil.e(TAG, "onViewCreated.Exception", ex)
            }
            LogUtil.d(TAG, "onViewCreated.searchTerm = $searchTerm")
            searchYouTubeVideos(searchTerm)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
        setProperFocus()
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        mediaRetriever.release()
    }

    private fun searchYouTubeVideos(searchTerm: String) {
        val logStr = "searchYouTubeVideos"
        LogUtil.i(TAG, "$logStr.searchTerm = $searchTerm")
        if (searchTerm.isEmpty()) return
        val act = activity?: return
        searchCompleted = false

        // save the searchTerm to file, U2bConstants.KEYWORD_FILENAME
        U2bPlayerUtil.saveKeyword(act, searchTerm)

        LogUtil.i(TAG, "$logStr.APPLICATION_ID = ${BuildConfig.APPLICATION_ID}")
        searchRecyclerView?.visibility = View.GONE
        loadingMsgTextView?.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            U2bSingleton.videos.clear()
            val videoList = RestApiSync.getVideoList(BuildConfig.APPLICATION_ID,
                searchTerm)
            LogUtil.d(TAG, "$logStr.videoList.items.size = ${videoList.items.size}")
            val fileBm = BitmapFactory.decodeResource(resources, R.drawable.video_image)
            var songInfo: SongInfo
            var bm: Bitmap?
            for (item in videoList.items) {
                songInfo = SongInfo()
                bm = null
                item.id.videoId?.let {
                    songInfo.apply {
                        songName = item.snippet.title
                        filePath = it
                        included = "0"
                        bitmapUrl = item.snippet.thumbnails.high.url
                        bm = ImageUtil.getBitmapFromUri(act, bitmapUrl)
                    }
                }
                if (bm == null) bm = fileBm
                bm = bm.scale(videoThumbnailsWidth, videoThumbnailsHeight)
                val songDes = SongDescription(songInfo, bm)
                U2bSingleton.videos.add(songDes)
            }
            // update the UI
            withContext(Dispatchers.Main) {
                updateRecyclerView()
                searchCompleted = true
            }
        }
    }

    private fun setProperFocus() {
        if (U2bSingleton.videos.isEmpty()) {
            searchRecyclerView?.visibility = View.GONE
            searchButton?.post { searchButton?.requestFocus() }
        } else {
            searchRecyclerView?.visibility = View.VISIBLE
            searchRecyclerView?.post { searchRecyclerView?.requestFocus() }
        }
    }

    private fun updateRecyclerView() {
        myRecyclerViewAdapter?.myNotifyDataSetChanged()
        loadingMsgTextView?.visibility = View.GONE
        setProperFocus()
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.d(TAG, "onItemClick.position = $position")
        val songDesc = U2bSingleton.videos[position]
        songDesc.apply {
            var isUpdated = false
            if (song.included == "1") {
                song.included = "0"
                selectedSongs.remove(song)
                isUpdated = true
            } else {
                if (selectedSongs.size >= MySingleton.MAX_SONGS) {
                    ScreenUtil.showToast(
                        activity, getString(R.string.excess_max) +
                                " ${MySingleton.MAX_SONGS}", textFontSize,
                        Toast.LENGTH_SHORT)
                } else {
                    song.included = "1"
                    selectedSongs.add(song)
                    isUpdated = true
                }
            }
            if (isUpdated) myRecyclerViewAdapter?.myNotifyItemChanged(position)
        }
    }

    private fun startSearchVideos() {
        LogUtil.i(TAG, "startSearchVideos.searchCompleted = $searchCompleted")
        if (!searchCompleted) {
            ScreenUtil.showToast(
                activity, getString(R.string.loadingWaitStr),
                textFontSize,Toast.LENGTH_SHORT)
            return
        }
        // start searching video
        searchEditTextView?.let { editIt ->
            val searchTerm = editIt.text.toString()
            searchYouTubeVideos(searchTerm)
        }
    }

    private fun videosToSongs(): ArrayList<SongInfo> {
        LogUtil.i(TAG, "videosToSongs")
        return ArrayList<SongInfo>().also { songIt ->
            var index = 0
            for (i in 0 until U2bSingleton.videos.size) {
                if (U2bSingleton.videos[i].song.included == "1") {
                    songIt.add(U2bSingleton.videos[i].song)
                    index++
                    if (index >= MySingleton.MAX_SONGS) {
                        // excess the max
                        ScreenUtil.showToast(
                            activity, getString(R.string.excess_max) +
                                    " ${MySingleton.MAX_SONGS}", textFontSize,
                            Toast.LENGTH_SHORT)
                        break
                    }
                }
            }
        }
    }

    // overriding BaseFragment's methods
    override fun setClickListeners() {
        searchButton?.setOnClickListener {
            startSearchVideos()
        }
        selectAllButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until U2bSingleton.videos.size) {
                U2bSingleton.videos[i].run {
                    song.included = "1"
                    myRecyclerViewAdapter?.notifyItemChanged(i)
                }
            }
        }
        unselectButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until U2bSingleton.videos.size) {
                U2bSingleton.videos[i].run {
                    song.included = "0"
                    myRecyclerViewAdapter?.notifyItemChanged(i)
                }
            }
        }
        playSelectedButton?.setOnClickListener {
            val logStr = "playSelectedButton.setOnClickListener"
            LogUtil.i(TAG, "$logStr.searchCompleted = $searchCompleted")
            if (!searchCompleted) return@setOnClickListener // searching
            // val songs = videosToSongs()
            LogUtil.i(TAG, "$logStr.selectedSongs.size = ${selectedSongs.size}")
            if (selectedSongs.isEmpty()) {
                ScreenUtil.showToast(activity, getString(R.string.noFilesSelectedString),
                    textFontSize,
                    Toast.LENGTH_SHORT)
            } else {
                val vSongs = ArrayList(selectedSongs.take(MySingleton.MAX_SONGS))
                playSongs?.playSelectedSongList(vSongs)
            }
        }
        addToFavoriteButton?.setOnClickListener {
            it.post { it.requestFocus() }
            LogUtil.i(TAG, "addToFavoriteButton.searchCompleted = $searchCompleted")
            if (!searchCompleted) return@setOnClickListener // searching
            val act = activity?: return@setOnClickListener
            // val songs = videosToSongs()
            lifecycleScope.launch(Dispatchers.IO) {
                if (selectedSongs.isEmpty()) {
                    ScreenUtil.showToast(activity, getString(R.string.noFilesSelectedString),
                        textFontSize,
                        Toast.LENGTH_SHORT)
                } else {
                    if (DatabaseUtil.addSongsToFavorites(act,
                            U2bPlayConstants.U2B_FAV_DB_NAME,
                            selectedSongs)) {
                        withContext(Dispatchers.Main) {
                            ScreenUtil.showToast(act,
                                getString(R.string.add_to_favorites),
                                textFontSize,Toast.LENGTH_SHORT)
                        }
                    }
                }
            }
        }
        super.setClickListeners()
    }

    override fun setButtonsSize() {
        LogUtil.i(TAG, "setButtonsSize")
        buttonLayout = fragmentView?.findViewById(R.id.searchButtonLayout)
        super.setButtonsSize()
        searchButton?.layoutParams = buttonParam
        selectAllButton?.layoutParams = buttonParam
        unselectButton?.layoutParams = buttonParam
        playSelectedButton?.layoutParams = buttonParam
        addToFavoriteButton?.layoutParams = buttonParam
    }

    override fun gridSpanCount(): Int {
        val act = activity ?: return 1
        return U2bPlayerUtil.gridSpanCount(act)
    }
    // end of overriding the methods of ItemsBaseFragment

    private fun initRecyclerAdapter() {
        LogUtil.i(TAG, "initRecyclerAdapter")
        val size = U2bSingleton.videos.size
        LogUtil.i(TAG, "initRecyclerAdapter.size = $size")
        activity?.let {
            myRecyclerViewAdapter = U2bRecyclerAdapter(this,
                U2bSingleton.videos, textFontSize)
            searchRecyclerView?.itemAnimator = null
            searchRecyclerView?.adapter = myRecyclerViewAdapter
            searchRecyclerView?.layoutManager = MyLayoutManager(context, gridSpanCount())
            updateRecyclerView()
        }
    }
}