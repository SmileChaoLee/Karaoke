package com.smile.youtube.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import com.smile.karaoke.BuildConfig
import com.smile.karaoke.R
import com.smile.karaoke.adapters.MyLinearLayoutManager
import com.smile.karaoke.fragments.ItemsBaseFragment
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.youtube.adapters.YouTubeRecyclerAdapter
import com.smile.youtube.models.VideoItem
import com.smile.youtube.models.YouSingleton
import com.smile.youtube.retrofit.RestApiSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var myRecyclerViewAdapter : YouTubeRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        YouSingleton.videos.clear()
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
            searchRecyclerView = it.findViewById(R.id.searchRecyclerView)
            searchRecyclerView?.setHasFixedSize(true)
        }

        initRecyclerAdapter()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
        YouSingleton.videos.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        mediaRetriever.release()
        YouSingleton.videos.clear()
    }

    fun searchYouTubeVideos(searchTerm: String) {
        val logStr = "searchYouTubeVideos"
        LogUtil.i(TAG, "$logStr.searchTerm = $searchTerm")
        if (searchTerm.isEmpty()) return
        searchCompleted = false
        LogUtil.i(TAG, "$logStr.APPLICATION_ID = ${BuildConfig.APPLICATION_ID}")
        lifecycleScope.launch(Dispatchers.IO) {
            YouSingleton.videos.clear()
            val videoList = RestApiSync.getVideoList(BuildConfig.APPLICATION_ID,
                searchTerm)
            LogUtil.d(TAG, "$logStr.videoList.items.size = ${videoList.items.size}")
            for (item in videoList.items) {
                // LogUtil.d(TAG, "$logStr.videoId = ${item.id.videoId}")
                // LogUtil.d(TAG, "$logStr.title = ${item.snippet.title}")
                YouSingleton.videos.add(convertItemToSongDes(item))
            }
            // update the UI
            withContext(Dispatchers.Main) {
                myRecyclerViewAdapter?.myNotifyDataSetChanged()
                searchCompleted = true
            }
        }
    }

    private suspend fun convertItemToSongDes(item: VideoItem): SongDescription {
        LogUtil.d(TAG, "convertItemToSongDes")
        val songInfo = SongInfo()
        val act = activity?: return SongDescription(songInfo, null)
        val imageLoader = act.imageLoader
        item.id.videoId?.let {
            var bm: Bitmap? = null
            songInfo.apply {
                songName = item.snippet.title
                filePath = it
                included = "0"
                val url = item.snippet.thumbnails.default.url
                val request = ImageRequest.Builder(act)
                    .data(url)
                    // Set size to original to get the full image size, or specify a custom Size
                    .size(Size.ORIGINAL)
                    // Disabling hardware bitmaps is often needed if you intend to modify the bitmap
                    .allowHardware(false)
                    .build()
                try {
                    val result = imageLoader.execute(request)
                    if (result is SuccessResult) {
                        // Convert the resulting Drawable to a Bitmap
                        bm = result.drawable.toBitmap()
                    }
                } catch (e: Exception) {
                    LogUtil.e(TAG, "convertItemToSongDes.Exception: ", e)
                }
            }
            return SongDescription(songInfo, bm)
        }

        return SongDescription(songInfo, null)
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.d(TAG, "onItemClick.position = $position")
        YouSingleton.videos[position].apply {
            song.included = if (song.included == "1") "0" else "1"
            myRecyclerViewAdapter?.myNotifyItemChanged(position)
        }

    }

    // overriding BaseFragment's methods
    override fun setClickListeners() {
        searchButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            // start searching video
            searchEditTextView?.let { editIt ->
                val searchTerm = editIt.text.toString()
                searchYouTubeVideos(searchTerm)
            }
        }
        selectAllButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until YouSingleton.videos.size) {
                YouSingleton.videos[i].run {
                    song.included = "1"
                    myRecyclerViewAdapter?.notifyItemChanged(i)
                }
            }
        }
        unselectButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until YouSingleton.videos.size) {
                YouSingleton.videos[i].run {
                    song.included = "0"
                    myRecyclerViewAdapter?.notifyItemChanged(i)
                }
            }
        }
        playSelectedButton?.setOnClickListener {
            val logStr = "playSelectedButton.setOnClickListener"
            LogUtil.i(TAG, logStr)
            if (!searchCompleted) return@setOnClickListener // searching
            // open the files to play
            LogUtil.i(TAG, "$logStr.searchCompleted")
            val songs = ArrayList<SongInfo>().also { songIt ->
                var index = 0
                for (i in 0 until YouSingleton.videos.size) {
                    if (YouSingleton.videos[i].song.included == "1") {
                        songIt.add(YouSingleton.videos[i].song)
                        index++
                        if (index >= YouSingleton.MAX_SONGS) {
                            // excess the max
                            ScreenUtil.showToast(
                                activity, getString(R.string.excess_max) +
                                        " ${YouSingleton.MAX_SONGS}", textFontSize,
                                Toast.LENGTH_SHORT)
                            break
                        }
                    }
                }
            }
            LogUtil.i(TAG, "$logStr.songs.size = ${songs.size}")
            if (songs.isEmpty()) {
                ScreenUtil.showToast(activity, getString(R.string.noFilesSelectedString),
                    textFontSize,
                    Toast.LENGTH_SHORT)
            } else {
                playSongs?.playSelectedSongList(ArrayList(songs))
            }
        }
        addToFavoriteButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
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
    // end of overriding BaseFragment's methods

    private fun initRecyclerAdapter() {
        LogUtil.i(TAG, "initRecyclerAdapter")
        activity?.let {
            myRecyclerViewAdapter = YouTubeRecyclerAdapter(
                this, YouSingleton.videos,
                textFontSize,
                videoThumbnailsWidth, videoThumbnailsHeight
            )
            searchRecyclerView?.adapter = myRecyclerViewAdapter
            searchRecyclerView?.layoutManager = MyLinearLayoutManager(context)
        }
    }
}