package com.smile.androidsong;

import android.annotation.SuppressLint;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smile.androidsong.model.Constants;
import com.smile.androidsong.model.SingerTypeList;
import com.smile.androidsong.retrofit.RestApiAsync;
import com.smile.androidsong.view_adapter.SingerTypeListAdapter;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.utilities.ScreenUtil;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;

public class SingerTypeListActivity extends AppCompatActivity {

    private static final String TAG = "SingerTypeListActivity";
    private float textFontSize;
    private TextView singerTypeListEmptyTextView;
    private RecyclerView mRecyclerView;
    @Inject
    SingerTypeListAdapter myViewAdapter;
    private SingerTypeList singerTypeList;
    private String noResultString;
    private String failedMessage;
    private String loadingString;
    private AlertDialogFragment loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        noResultString = getString(R.string.noResultString);
        failedMessage = getString(R.string.failedMessage);
        loadingString = getString(R.string.loadingString);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, Constants.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, Constants.FontSize_Scale_Type, 0.0f);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_singer_type_list);

        final TextView singerTypesListMenuTextView = findViewById(R.id.singerTypesListMenuTextView);
        ScreenUtil.resizeTextSize(singerTypesListMenuTextView, textFontSize, Constants.FontSize_Scale_Type);

        singerTypeListEmptyTextView = findViewById(R.id.singerTypeListEmptyTextView);
        ScreenUtil.resizeTextSize(singerTypeListEmptyTextView, textFontSize, Constants.FontSize_Scale_Type);
        singerTypeListEmptyTextView.setVisibility(View.GONE);

        mRecyclerView = findViewById(R.id.singerTypeListRecyclerView);

        final Button singerTypesListReturnButton = findViewById(R.id.singerTypesListReturnButton);
        ScreenUtil.resizeTextSize(singerTypesListReturnButton, textFontSize, Constants.FontSize_Scale_Type);
        singerTypesListReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPrevious();
            }
        });

        loadingDialog = AlertDialogFragment.newInstance(loadingString,
                Constants.FontSize_Scale_Type,
                textFontSize, Color.RED, 0, 0, true);

        loadingDialog.show(getSupportFragmentManager(), "LoadingDialogTag");

        new MyRestApi().getAllSingerTypes();
    }

    @Override
    public void onBackPressed() {
        returnToPrevious();
    }

    private void returnToPrevious() {
        Log.d(TAG, "returnToPrevious");
        finish();
    }

    private class MyRestApi extends RestApiAsync<SingerTypeList> {
        @SuppressLint("SetTextI18n")
        @Override
        public void onResponse(Call<SingerTypeList> call, Response<SingerTypeList> response) {
            Log.d(TAG, "MyRestApi.onResponse");
            loadingDialog.dismissAllowingStateLoss();
            Log.d(TAG, "MyRestApi.onResponse.response.isSuccessful() = " + response.isSuccessful());
            if (response.isSuccessful()) {
                singerTypeList = response.body();
                if (singerTypeList.getSingerTypes().isEmpty()) {
                    singerTypeListEmptyTextView.setText(noResultString);
                    singerTypeListEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    singerTypeListEmptyTextView.setVisibility(View.GONE);
                }
            } else {
                singerTypeList = new SingerTypeList();
                singerTypeListEmptyTextView.setText("MyRestApi.response.isSuccessful() = false.");
                singerTypeListEmptyTextView.setVisibility(View.VISIBLE);
            }
            Log.d(TAG, "MyRestApi.onResponse.inject()");
            SongApplication.Companion.getAppCompBuilder()
                    .activityModule(SingerTypeListActivity.this)
                    .singerTypeArrayListModule(singerTypeList.getSingerTypes())
                    .floatModule(textFontSize).build()
                    .inject(SingerTypeListActivity.this);
            mRecyclerView.setAdapter(myViewAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        }

        @Override
        public void onFailure(Call<SingerTypeList> call, Throwable t) {
            Log.d(TAG, "MyRestApi.onFailure." + t.toString());
            loadingDialog.dismissAllowingStateLoss();
            singerTypeList = new SingerTypeList();
            singerTypeListEmptyTextView.setText(failedMessage);
            singerTypeListEmptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
