package youtube.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.BuildConfig
import com.smile.karaoke.R
import com.smile.karaoke.adapters.FavoriteRecyclerViewAdapter
import com.smile.karaoke.fragments.ItemsBaseFragment
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.MySingleTon
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import youtube.retrofit.RestApiSync

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
    private var myRecyclerViewAdapter : FavoriteRecyclerViewAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_search_video,
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

        initFavoriteRecyclerView()

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
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        mediaRetriever.release()
    }

    fun searchYouTubeVideos(searchTerm: String) {
        val logStr = "searchYouTubeVideos"
        LogUtil.i(TAG, "$logStr.searchTerm = $searchTerm")
        searchCompleted = false
        LogUtil.i(TAG, "$logStr.APPLICATION_ID = ${BuildConfig.APPLICATION_ID}")
        lifecycleScope.launch(Dispatchers.IO) {
            val videoList = RestApiSync.getVideoList(BuildConfig.APPLICATION_ID,
                searchTerm)
            LogUtil.d(TAG, "$logStr.videoList.items.size = ${videoList.items.size}")
            for (item in videoList.items) {
                LogUtil.d(TAG, "$logStr.kind = ${item.id.kind}")
                LogUtil.d(TAG, "$logStr.videoId = ${item.id.videoId}")
            }
            searchCompleted = true
        }
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.d(TAG, "onItemClick")
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
            for (i in 0 until MySingleTon.favorites.size) {
                MySingleTon.favorites[i].run {
                    song.included = "1"
                    myRecyclerViewAdapter?.notifyItemChanged(i)
                }
            }
        }
        unselectButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until MySingleTon.favorites.size) {
                MySingleTon.favorites[i].run {
                    song.included = "0"
                    myRecyclerViewAdapter?.notifyItemChanged(i)
                }
            }
        }
        playSelectedButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            // open the files to play
            val songs = ArrayList<SongInfo>().also { songIt ->
                var index = 0
                for (i in 0 until MySingleTon.favorites.size) {
                    if (MySingleTon.favorites[i].song.included == "1") {
                        songIt.add(MySingleTon.favorites[i].song)
                        index++
                        if (index >= MySingleTon.MAX_SONGS) {
                            // excess the max
                            ScreenUtil.showToast(
                                activity, getString(R.string.excess_max) +
                                        " ${MySingleTon.MAX_SONGS}", textFontSize,
                                Toast.LENGTH_SHORT)
                            break
                        }
                    }
                }
            }
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
        buttonLayout = fragmentView?.findViewById(R.id.searchButtonLayout)
        super.setButtonsSize()
        searchButton?.layoutParams = buttonParam
        selectAllButton?.layoutParams = buttonParam
        unselectButton?.layoutParams = buttonParam
        playSelectedButton?.layoutParams = buttonParam
        addToFavoriteButton?.layoutParams = buttonParam
    }
    // end of overriding BaseFragment's methods

    private fun initFavoriteRecyclerView() {
        LogUtil.i(TAG, "initFavoriteRecyclerView")
    }

    private fun convertItemToSongInfo(): SongInfo {
        val sInfo = SongInfo()
        return sInfo
    }
}