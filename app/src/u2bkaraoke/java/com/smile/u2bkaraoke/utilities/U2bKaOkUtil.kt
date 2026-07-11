package com.smile.u2bkaraoke.utilities

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.smile.karaoke.utilities.LogUtil

object U2bKaOkUtil {

    private const val TAG = "U2bKaOkUtil"
    const val FRAGMENT_TAG = "U2bFragmentTag"

    fun returnToPrevious(activity: FragmentActivity?) {
        activity?.supportFragmentManager?.popBackStack()
    }

    fun beginTransaction(fm: FragmentManager,
                         fragContainerId: Int,
                         nFragment: Fragment,
                         fragmentTag: String? = null) {
        LogUtil.d(TAG, "beginTransaction.fm.isStateSaved = ${fm.isStateSaved}")
        Handler(Looper.getMainLooper()).post {
            val fTag = fragmentTag ?: FRAGMENT_TAG
            fm.beginTransaction().apply {
                replace(fragContainerId, nFragment, fTag)
                addToBackStack(null)
                commit()
            }
            fm.executePendingTransactions()
            // val curF = fm.findFragmentByTag(fTag)
            // LogUtil.d(TAG, "beginTransaction.curF = $curF")
        }
    }
}