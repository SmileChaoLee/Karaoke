package com.smile.androidsong;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smile.androidsong.model.*;
import com.smile.androidsong.retrofit.RestApiAsync;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.utilities.ScreenUtil;
import com.smile.androidsong.view_adapter.LanguageListAdapter;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;

public class LanguageListActivity extends AppCompatActivity {
    private static final String TAG = "LanguageListActivity";
    private float textFontSize;
    private TextView languagesListEmptyTextView;
    private RecyclerView mRecyclerView;
    @Inject
    LanguageListAdapter myViewAdapter;
    private LanguageList languageList = null;
    private String noResultString;
    private String failedMessage;
    private String loadingString;
    private AlertDialogFragment loadingDialog;

    private int orderedFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        noResultString = getString(R.string.noResultString);
        failedMessage = getString(R.string.failedMessage);
        loadingString = getString(R.string.loadingString);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, Constants.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, Constants.FontSize_Scale_Type, 0.0f);

        orderedFrom = Constants.WordsOrdered;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            orderedFrom = extras.getInt(Constants.OrderedFrom, Constants.WordsOrdered);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_language_list);

        TextView menuTextView = findViewById(R.id.languagesListMenuTextView);
        ScreenUtil.resizeTextSize(menuTextView, textFontSize, Constants.FontSize_Scale_Type);
        switch (orderedFrom) {
            case Constants.WordsOrdered:
                // from main activity (MyActivity)
                menuTextView.setText(getString(R.string.languagesListString));
                break;
            case Constants.NewSongOrdered:
                menuTextView.setText(getString(R.string.newSOngLanguagesListString));
                break;
            case Constants.HotSongOrdered:
                menuTextView.setText(getString(R.string.hotSongLanguagesListString));
                break;
        }

        languagesListEmptyTextView = findViewById(R.id.languagesListEmptyTextView);
        ScreenUtil.resizeTextSize(languagesListEmptyTextView, textFontSize, Constants.FontSize_Scale_Type);
        languagesListEmptyTextView.setVisibility(View.GONE);

        mRecyclerView = findViewById(R.id.languageListRecyclerView);

        final Button languagesListReturnButton = findViewById(R.id.languagesListReturnButton);
        ScreenUtil.resizeTextSize(languagesListReturnButton, textFontSize, Constants.FontSize_Scale_Type);
        languagesListReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPrevious();
            }
        });

        loadingDialog = AlertDialogFragment.newInstance(loadingString,
                Constants.FontSize_Scale_Type,
                textFontSize, Color.RED, 0, 0, true);

        loadingDialog.show(getSupportFragmentManager(), "LoadingDialogTag");

        new MyRestApi().getAllLanguages();
    }

    @Override
    public void onBackPressed() {
        returnToPrevious();
    }

    private void returnToPrevious() {
        Log.d(TAG, "returnToPrevious");
        finish();
    }

    private class MyRestApi extends RestApiAsync<LanguageList> {
        @Override
        public void onResponse(Call<LanguageList> call, Response<LanguageList> response) {
            Log.d(TAG, "MyRestApi.onResponse");
            loadingDialog.dismissAllowingStateLoss();
            Log.d(TAG, "MyRestApi.onResponse.response.isSuccessful() = " + response.isSuccessful());
            if (response.isSuccessful()) {
                languageList = response.body();
                if (languageList.getLanguages().isEmpty()) {
                    languagesListEmptyTextView.setText(noResultString);
                    languagesListEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    languagesListEmptyTextView.setVisibility(View.GONE);
                }
            } else {
                languageList = new LanguageList();
                languagesListEmptyTextView.setText(failedMessage);
                languagesListEmptyTextView.setVisibility(View.VISIBLE);
            }
            // myViewAdapter = new LanguageListAdapter(LanguageListActivity.this,
            //         languageList.getLanguages(), orderedFrom, textFontSize);
            Log.d(TAG, "MyRestApi.onResponse.inject()");
            SongApplication.Companion.getAppCompBuilder()
                    .activityModule(LanguageListActivity.this)
                    .languageArrayListModule(languageList.getLanguages())
                    .intModule(orderedFrom)
                    .floatModule(textFontSize).build()
                    .inject(LanguageListActivity.this);
            mRecyclerView.setAdapter(myViewAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        }

        @Override
        public void onFailure(Call<LanguageList> call, Throwable t) {
            Log.d(TAG, "MyRestApi.onFailure." + t.toString());
            loadingDialog.dismissAllowingStateLoss();
            languageList = new LanguageList();
            languagesListEmptyTextView.setText(failedMessage);
            languagesListEmptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
