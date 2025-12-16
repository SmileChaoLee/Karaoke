package com.smile.karaoke.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.smile.smilelibraries.utilities.ScreenUtil

class SpinnerAdapter(
    context: Context,
    private val resource: Int,
    private val textViewResourceId: Int,
    private val objects: List<String?>,
    private val textFontSize: Float
) : ArrayAdapter<String?>(context, resource, textViewResourceId, objects) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // This method is for the "closed" spinner view (the selected item)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(textViewResourceId)
        ScreenUtil.resizeTextSize(textView, textFontSize)
        return view
    }

    // --- THIS IS THE IMPORTANT PART ---
    // This method is for each item in the dropdown list
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflate a new view, ignoring convertView to ensure the background is always applied
        // val view = inflater.inflate(resource, parent, false)
        val view = convertView ?: inflater.inflate(resource, parent, false)
        // Find the TextView inside the layout
        val textView = view.findViewById<TextView>(textViewResourceId)
        // Set the text for the current item
        textView.text = getItem(position)
        // Apply the text size
        ScreenUtil.resizeTextSize(textView, textFontSize)

        return view
    }
}

