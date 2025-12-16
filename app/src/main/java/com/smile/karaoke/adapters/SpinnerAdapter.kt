package com.smile.karaoke.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smile.karaoke.R;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter {

    private final Activity mActivity;
    private final int mTextViewResourceId;
    private final float mTextFontSize;

    @SuppressWarnings("unchecked")
    public SpinnerAdapter(@NonNull Context context, int resource, int textViewResourceId,
                          @NonNull List objects, float textSize) {
        super(context, resource, textViewResourceId, objects);
        mActivity = (Activity)context;
        mTextViewResourceId = textViewResourceId;
        mTextFontSize = textSize;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (getCount() == 0) {
            return view;
        }

        TextView itemTextView = view.findViewById(mTextViewResourceId);
        ScreenUtil.resizeTextSize(itemTextView, mTextFontSize);

        return view;
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // View view = super.getView(position, convertView, parent);
        View view = mActivity.getLayoutInflater().inflate(R.layout.spinner_dropdown_item_layout, parent, false);

        if (getCount() == 0) {
            return view;
        }

        if (view != null) {
            TextView itemTextView = view.findViewById(R.id.customSpinnerTextView);
            itemTextView.setText(getItem(position).toString());
            ScreenUtil.resizeTextSize(itemTextView, mTextFontSize);
        }

        return view;
    }
}
