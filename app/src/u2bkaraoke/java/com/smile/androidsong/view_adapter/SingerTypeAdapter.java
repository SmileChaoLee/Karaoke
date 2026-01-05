package com.smile.androidsong.view_adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smile.androidsong.R;
import com.smile.androidsong.SingerListActivity;
import com.smile.androidsong.model.Constants;
import com.smile.androidsong.model.SingerType;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;

public class SingerTypeAdapter extends RecyclerView.Adapter<SingerTypeAdapter.MyViewHolder> {
    private static final String TAG = "SingerTypesAdapter";
    private final Activity mActivity;
    private final ArrayList<SingerType> mSingerTypes;
    private final float mTextFontSize;
    public SingerTypeAdapter(Activity activity, ArrayList<SingerType> singerTypes, float textFontSize) {
        mActivity = activity;
        mTextFontSize = textFontSize;
        mSingerTypes = singerTypes;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView positionNoTextView;
        private final TextView singerAreaNaTextView;
        private final TextView singerSexTextView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            positionNoTextView = itemView.findViewById(R.id.singerTypeItem_Layout_positionNoTextView);
            ScreenUtil.resizeTextSize(positionNoTextView, mTextFontSize, Constants.FontSize_Scale_Type);

            singerAreaNaTextView = itemView.findViewById(R.id.singerAreaNaTextView);
            ScreenUtil.resizeTextSize(singerAreaNaTextView, mTextFontSize, Constants.FontSize_Scale_Type);

            singerSexTextView = itemView.findViewById(R.id.singerSexTextView);
            ScreenUtil.resizeTextSize(singerSexTextView, mTextFontSize, Constants.FontSize_Scale_Type);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        // inflate the singerType item view
        View singerTypeItemView = layoutInflater.inflate(R.layout.singer_type_list_item, parent, false);
        return new MyViewHolder(singerTypeItemView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final SingerType singerType = mSingerTypes.get(position);
        holder.positionNoTextView.setText(String.valueOf(position));
        holder.singerAreaNaTextView.setText(singerType.getAreaNa());
        String sexString = switch (singerType.getSex()) {
            case "1" -> "Male";
            case "2" -> "Female";
            default ->
                // "0"
                    "";
        };
        holder.singerSexTextView.setText(sexString);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String singersListActivityTitle = singerType.getAreaNa();
                ScreenUtil.showToast(mActivity, singersListActivityTitle,
                        mTextFontSize, Constants.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                Intent singersIntent = new Intent(mActivity, SingerListActivity.class);
                singersIntent.putExtra("SingersListActivityTitle", singersListActivityTitle);
                singersIntent.putExtra("SingerTypeParcelable", singerType);
                mActivity.startActivity(singersIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        int size = mSingerTypes.size();
        Log.d(TAG, "singerTypes.size() = " + size);
        return size;
    }
}
