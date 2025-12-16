package com.smile.karaoke

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.adapters.SelectedFavoriteAdapter
import com.smile.karaoke.adapters.SelectedFavoriteAdapter.OnRecyclerItemClickListener
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.models.MySingleton.selectedFavorites
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.models.SongListSQLite
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

class BaseFavoriteListActivity : AppCompatActivity(), OnRecyclerItemClickListener {

    companion object {
        private const val TAG = "BFavListActivity"
        private const val CRUD_ACTION_STATE = "CrudAction"
        private const val POS_EDIT_STATE = "PositionEdit"
    }
    private var songListSQLite: SongListSQLite? = null
    private var textFontSize = 0f
    lateinit var editFavoritesLauncher: ActivityResultLauncher<Intent>
    private var currentAction: String? = CommonConstants.EDIT_ACTION
    private var weightSum = 0f
    private var favoriteListLinearLayout: LinearLayout? = null
    private var exitFavoriteListButton: Button? = null
    private var favoritesTitleLayout: LinearLayout? = null
    private var favoritesExitButtonLayout: LinearLayout? = null
    private var myListRecyclerView: RecyclerView? = null
    private var myRecyclerViewAdapter: SelectedFavoriteAdapter? = null
    private var positionEdit = -1

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)
        songListSQLite = SongListSQLite(this@BaseFavoriteListActivity)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_list)

        val myFavoritesTextView = findViewById<TextView>(R.id.myFavoritesTextView)
        ScreenUtil.resizeTextSize(myFavoritesTextView, textFontSize)
        exitFavoriteListButton = findViewById(R.id.exitFavoriteListButton)
        ScreenUtil.resizeTextSize(exitFavoriteListButton, textFontSize)
        exitFavoriteListButton?.setOnClickListener { v: View? -> returnToPrevious() }

        favoriteListLinearLayout = findViewById(R.id.favoriteListLinearLayout)
        weightSum = favoriteListLinearLayout?.weightSum ?: 100f
        favoritesTitleLayout = findViewById(R.id.favoritesTitleLayout)
        myListRecyclerView = findViewById(R.id.selectedFavoriteRecyclerView)
        myListRecyclerView?.setHasFixedSize(true)
        favoritesExitButtonLayout = findViewById(R.id.favoritesExitButtonLayout)
        setLayoutViewWeight()

        savedInstanceState?.let { state ->
            // activity being recreated
            currentAction = state.getString(CRUD_ACTION_STATE)
            positionEdit = state.getInt(POS_EDIT_STATE, -1)
            val tempList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                state.getSerializable(MyPlayerConstants.MyFavoriteListState,
                        ArrayList::class.java) as ArrayList<SongInfo>?
            } else {
                state.getSerializable(MyPlayerConstants.MyFavoriteListState) as ArrayList<SongInfo>?
            }
            selectedFavorites.clear()
            if (tempList != null) selectedFavorites.addAll(tempList)
            LogUtil.d(TAG, "onCreate.selectedFavorites.size() = ${selectedFavorites.size}")

        }

        editFavoritesLauncher = registerForActivityResult(
            StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "editFavoritesLauncher.receive.result = $result")
            if (result.resultCode == RESULT_OK) {
                LogUtil.d(TAG, "editFavoritesLauncher.updateFavoriteList")
                updateFavoriteList(result.data)
            }
            exitFavoriteListButton?.post { exitFavoriteListButton?.requestFocus() }
        }

        LogUtil.d(
            TAG, "onCreate.FavoriteSingleTon.INSTANCE.getSelectedList().size() = " +
                    selectedFavorites.size
        )

        initSelectedFavoriteRecyclerView()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtil.d(TAG, "getOnBackPressedDispatcher.handleOnBackPressed")
                returnToPrevious()
            }
        })

        // Find the LinearLayout by its ID
        val favoriteListLinearLayout = findViewById<LinearLayout>(R.id.favoriteListLinearLayout)
        // Get the ViewTreeObserver for the LinearLayout
        favoriteListLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Layout has been finished.
                    // Remove the listener to avoid it being called repeatedly.
                    // The removeOnGlobalLayoutListener() method is used for API 16 and above.
                    favoriteListLinearLayout.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this)
                    // Now it's safe to get the view's dimensions or perform other actions
                    // that depend on the layout being complete.
                    // do something after layout finished
                }
            }
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.d(TAG, "onConfigurationChanged")
        setLayoutViewWeight()
        super.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtil.d(TAG, "onSaveInstanceState")
        outState.putString(CRUD_ACTION_STATE, currentAction)
        outState.putInt(POS_EDIT_STATE, positionEdit)
        // must create a new instance for FavoriteSingleTon.INSTANCE.getSelectedList()
        // in this case
        val tempList = ArrayList<SongInfo?>(selectedFavorites)
        outState.putSerializable(MyPlayerConstants.MyFavoriteListState, tempList)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        LogUtil.d(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        LogUtil.d(TAG, "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        selectedFavorites.clear()
        if (songListSQLite != null) {
            songListSQLite!!.closeDatabase()
            songListSQLite = null
        }
        Runtime.getRuntime().gc()
        super.onDestroy()
    }

    private fun returnToPrevious() {
        LogUtil.d(TAG, "returnToPrevious")
        setResult(RESULT_OK) // no bundle data
        finish()
    }

    private fun createIntentFromSongDataActivity(): Intent {
        LogUtil.d(TAG, "createIntentFromSongDataActivity")
        return Intent(this, BaseSongDataActivity::class.java)
    }

    private fun deleteOneSongFromFavoriteList(singleSongInfo: SongInfo?) {
        LogUtil.d(TAG, "deleteOneSongFromFavoriteList")
        currentAction = CommonConstants.DELETE_ACTION
        val deleteIntent = createIntentFromSongDataActivity()
        deleteIntent.putExtra(CommonConstants.CRUD_ACTION, CommonConstants.DELETE_ACTION)
        deleteIntent.putExtra(MyPlayerConstants.SINGLE_SONG_INFO_STATE, singleSongInfo)
        editFavoritesLauncher.launch(deleteIntent)
    }

    private fun editOneSongFromFavoriteList(singleSongInfo: SongInfo?) {
        LogUtil.d(TAG, "editOneSongFromFavoriteList")
        currentAction = CommonConstants.EDIT_ACTION
        val editIntent = createIntentFromSongDataActivity()
        editIntent.putExtra(CommonConstants.CRUD_ACTION, CommonConstants.EDIT_ACTION)
        editIntent.putExtra(MyPlayerConstants.SINGLE_SONG_INFO_STATE, singleSongInfo)
        editFavoritesLauncher.launch(editIntent)
    }

    private fun initSelectedFavoriteRecyclerView() {
        LogUtil.d(
            TAG, "initSelectedFavoriteRecyclerView.getSelectedList() = " +
                    selectedFavorites.size
        )

        val yellow2Color = ContextCompat.getColor(this, R.color.yellow2)
        val yellow3Color = ContextCompat.getColor(this, R.color.yellow3)

        myRecyclerViewAdapter = SelectedFavoriteAdapter(
            this, songListSQLite!!,
            selectedFavorites,
            textFontSize, yellow2Color, yellow3Color
        )

        myListRecyclerView!!.setAdapter(myRecyclerViewAdapter)
        myListRecyclerView!!.setLayoutManager(object : LinearLayoutManager(this) {
            override fun isAutoMeasureEnabled(): Boolean {
                return false
            }
        })
    }

    // implement SelectedFavoriteAdapter.OnRecyclerItemClickListener
    override fun onRecyclerItemClick(v: View?, position: Int) {
        LogUtil.d(TAG, "onRecyclerItemClick.position = $position")
    }

    override fun editSongButtonFunc(position: Int) {
        LogUtil.d(TAG, "editSongButtonFunc.position = $position")
        if (position < 0 || position >= selectedFavorites.size) {
            return
        }
        LogUtil.d(TAG, "editSongButtonFunc.positionEdit = $positionEdit")
        LogUtil.d(TAG, "editSongButtonFunc.editOneSongFromFavoriteList()")
        positionEdit = position
        editOneSongFromFavoriteList(selectedFavorites[position])
    }

    override fun deleteSongButtonFunc(position: Int) {
        LogUtil.d(TAG, "deleteSongButtonFunc.position = $position")
        if (position < 0 || position >= selectedFavorites.size) {
            return
        }
        positionEdit = position
        LogUtil.d(TAG, "deleteSongButtonFunc.positionEdit = $positionEdit")
        deleteOneSongFromFavoriteList(selectedFavorites[position])
    }

    override fun playSongButtonFunc(position: Int) {
        // play this item (media file)
        LogUtil.d(TAG, "playSongButtonFunc.position = $position")
        if (position < 0 || position >= selectedFavorites.size) {
            return
        }
        LogUtil.d(TAG, "playSongButtonFunc.positionEdit = $positionEdit")
        positionEdit = -1 // no edit or delete
        currentAction = CommonConstants.PLAY_ACTION
        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        val bIntent = Intent(MyPlayerConstants.PlaySingleSongAction)
        val extras = Bundle()
        extras.putBoolean(MyPlayerConstants.IS_PLAY_SINGLE_SONG_STATE, true) // play single song
        extras.putParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE, selectedFavorites[position])
        bIntent.putExtras(extras)
        LogUtil.d(TAG, "playSongButtonFunc.sendBroadcast().to play")
        broadcastManager.sendBroadcast(bIntent)
    }

    // Finish implementing SelectedFavoriteAdapter.OnRecyclerItemClickListener
    private fun updateFavoriteList(data: Intent?) {
        LogUtil.d(TAG, "updateFavoriteList")
        if (data != null && positionEdit != -1) {
            LogUtil.d(TAG, "updateFavoriteList.positionEdit = $positionEdit")
            val songInfo = data.getParcelableExtra<SongInfo?>(MyPlayerConstants.SINGLE_SONG_INFO_STATE)
            if (songInfo != null) {
                when (currentAction) {
                    CommonConstants.EDIT_ACTION -> {
                        // edit
                        selectedFavorites[positionEdit] = songInfo
                        myRecyclerViewAdapter?.notifyItemChanged(positionEdit)
                    }
                    CommonConstants.DELETE_ACTION -> {
                        // delete
                        selectedFavorites.removeAt(positionEdit)
                        myRecyclerViewAdapter!!.notifyItemRemoved(positionEdit)
                    }
                    else -> {    // currentAction = CommonConstants.PlayActionString
                        LogUtil.d(TAG, "updateFavoriteList.do nothing")
                    }
                }
            }
        }
    }

    private fun setLayoutViewWeight() {
        LogUtil.d(TAG, "setLayoutViewWeight.textFontSize = $textFontSize")
        var weight = 10f
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            weight = 20f
        }
        var layoutP = favoritesTitleLayout?.layoutParams as LinearLayout.LayoutParams
        LogUtil.d(TAG, "setLayoutViewWeight.weight = $weight")
        layoutP.weight = weight
        layoutP = favoritesExitButtonLayout?.layoutParams as LinearLayout.LayoutParams
        layoutP.weight = weight
        layoutP = myListRecyclerView?.layoutParams as LinearLayout.LayoutParams
        layoutP.weight = weightSum - weight * 2
    }
}
