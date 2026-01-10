package com.smile.u2bkaraoke.utilities

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

object U2bKaOkUtil {

    fun returnToPrevious(activity: FragmentActivity?) {
        activity?.supportFragmentManager?.popBackStack()
    }

    fun beginTransaction(fm: FragmentManager,
                         fragContainerId: Int,
                         nFragment: Fragment) {
        fm.beginTransaction().apply {
            replace(fragContainerId, nFragment)
            addToBackStack(null)
            commit()
        }
    }
}