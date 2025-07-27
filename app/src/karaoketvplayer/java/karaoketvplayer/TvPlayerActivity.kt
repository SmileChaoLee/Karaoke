package karaoketvplayer

import android.os.Bundle
import android.util.Log
import com.smile.karaoke.PlayerActivity

class TvPlayerActivity: PlayerActivity() {
    private val mTAG : String = "TvPlayerActivity"
    init {
        Log.d(mTAG, "")
        setTag(mTAG)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(mTAG, "onCreate.Started")
        val intentAction = intent.action
        val intentCategories = intent.categories
        Log.i(mTAG, "onCreate.intentCategories = $intentCategories")
        Log.i(mTAG, "onCreate.intentAction = $intentAction")
        if (intentCategories != null && intentCategories.isNotEmpty()) {
            for (category in intentCategories) {
                Log.i(mTAG, "onCreate.category = $category")
            }
        } else {
            Log.i(mTAG, "No categories in intent")
        }
    }
}
