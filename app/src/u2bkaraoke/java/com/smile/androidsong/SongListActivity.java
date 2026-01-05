package com.smile.androidsong;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smile.androidsong.model.*;
import com.smile.androidsong.retrofit.RestApiAsync;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.utilities.ScreenUtil;
import com.smile.androidsong.view_adapter.SongListAdapter;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;

public class SongListActivity extends AppCompatActivity {

    private static final String TAG = "SongListActivity";
    private float textFontSize;
    private EditText searchEditText;
    private boolean isSearchEditTextChanged;
    private String filterString;
    private TextView songsListEmptyTextView;
    private RecyclerView mRecyclerView;
    @Inject
    SongListAdapter myViewAdapter;
    private SongList songList = null;
    private Singer singer;
    private Language language;
    private Object objectPassed;

    private int orderedFrom = 0;
    private int numOfWords = 0;
    private int pageNo = 1;
    private int pageSize = 7;
    private int totalPages = 0;
    private String noResultString;
    private String failedMessage;
    private String loadingString;
    private AlertDialogFragment loadingDialog = null;

    private MyRestApi restApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        noResultString = getString(R.string.noResultString);
        failedMessage = getString(R.string.failedMessage);
        loadingString = getString(R.string.loadingString);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, Constants.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, Constants.FontSize_Scale_Type, 0.0f);

        orderedFrom = 0;    // default value
        String songListTitle = getString(R.string.songsListString);
        String activityTitle = "";
        numOfWords = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null ) {
            orderedFrom = extras.getInt(Constants.OrderedFrom, 0);
            activityTitle = extras.getString(Constants.SongListActivityTitle, "").trim();
            switch (orderedFrom) {
                case Constants.SingerOrdered -> {
                    singer = extras.getParcelable(Constants.SingerParcelable);
                    objectPassed = singer;
                }
                case Constants.NewSongOrdered -> objectPassed = language;
                case Constants.NewSongLanguageOrdered, Constants.HotSongLanguageOrdered -> {
                    language = extras.getParcelable(Constants.LanguageParcelable);
                    objectPassed = language;
                }
                case Constants.HotSongOrdered -> objectPassed = null;
                case Constants.LanguageOrdered, Constants.LanguageWordsOrdered -> {
                    language = extras.getParcelable(Constants.LanguageParcelable);
                    objectPassed = language;
                    numOfWords = extras.getInt(Constants.NumOfWords);
                }
            }
        }
        Log.d(TAG, "onCreate.orderedFrom = " + orderedFrom);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_song_list);

        TextView songsListMenuTextView = findViewById(R.id.songsListMenuTextView);
        ScreenUtil.resizeTextSize(songsListMenuTextView, textFontSize, Constants.FontSize_Scale_Type);
        songsListMenuTextView.setText(activityTitle + " " + songListTitle);

        filterString = "";
        searchEditText = findViewById(R.id.songSearchEditText);
        ScreenUtil.resizeTextSize(searchEditText, textFontSize, Constants.FontSize_Scale_Type);
        LinearLayout.LayoutParams searchEditLp = (LinearLayout.LayoutParams) searchEditText.getLayoutParams();
        searchEditLp.leftMargin = (int)(textFontSize * 2.0f);
        searchEditLp.rightMargin = (int)(textFontSize * 5.0f);
        // searchEditLp.setMargins(100, 0, (int)textFontSize*2, 0);
        searchEditText.setText(filterString);
        isSearchEditTextChanged = false;
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "searchEditText.addTextChangedListener.beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "searchEditText.addTextChangedListener.onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "searchEditText.addTextChangedListener.afterTextChanged");
                String content = editable.toString().trim();
                filterString = content.isEmpty() ? "" : "SongNa+" + content;
                Log.d(TAG, "searchEditText.addTextChangedListener.afterTextChanged.filterString = "
                + filterString);
                pageNo = 1;
                isSearchEditTextChanged = true;
                // searchEditText.clearFocus();
                retrieveSongList();
            }
        });

        mRecyclerView = findViewById(R.id.songListRecyclerView);

        songsListEmptyTextView = findViewById(R.id.songsListEmptyTextView);
        ScreenUtil.resizeTextSize(songsListEmptyTextView, textFontSize, Constants.FontSize_Scale_Type);
        songsListEmptyTextView.setVisibility(View.GONE);

        float smallButtonFontSize = textFontSize * 0.7f;
        final Button firstPageButton = findViewById(R.id.firstPageButton);
        ScreenUtil.resizeTextSize(firstPageButton, smallButtonFontSize, Constants.FontSize_Scale_Type);
        firstPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstPage();
            }
        });

        final Button previousPageButton = findViewById(R.id.previousPageButton);
        ScreenUtil.resizeTextSize(previousPageButton, smallButtonFontSize, Constants.FontSize_Scale_Type);
        previousPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousPage();
            }
        });

        final Button nextPageButton = findViewById(R.id.nextPageButton);
        ScreenUtil.resizeTextSize(nextPageButton, smallButtonFontSize, Constants.FontSize_Scale_Type);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPage();
            }
        });

        final Button lastPageButton = findViewById(R.id.lastPageButton);
        ScreenUtil.resizeTextSize(lastPageButton, smallButtonFontSize, Constants.FontSize_Scale_Type);
        lastPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastPage();
            }
        });

        final Button songsListReturnButton = findViewById(R.id.songsListReturnButton);
        ScreenUtil.resizeTextSize(songsListReturnButton, textFontSize, Constants.FontSize_Scale_Type);
        songsListReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPrevious();
            }
        });

        restApi = new MyRestApi();
        retrieveSongList();
    }

    private void retrieveSongList() {
        Log.d(TAG, "retrieveSongList.orderedFrom = " + orderedFrom);
        if (loadingDialog == null) {
            loadingDialog = AlertDialogFragment.newInstance(loadingString,
                    Constants.FontSize_Scale_Type,
                    textFontSize, Color.RED, 0, 0, true);
            loadingDialog.show(getSupportFragmentManager(), "LoadingDialogTag");
        }
        switch (orderedFrom) {
            case Constants.SingerOrdered:
                Log.d(TAG, "retrieveSongList.SingerOrdered");
                if (filterString != null && !filterString.isEmpty()) {
                    restApi.getSongsBySinger((Singer) objectPassed, pageSize, pageNo, filterString);
                } else {
                    restApi.getSongsBySinger((Singer) objectPassed, pageSize, pageNo);
                }
                break;
            case Constants.NewSongOrdered :
                Log.d(TAG, "retrieveSongList.NewSongOrdered");
                break;
            case Constants.HotSongOrdered:
                Log.d(TAG, "retrieveSongList.HotSongOrdered");
                break;
            case Constants.NewSongLanguageOrdered:
                Log.d(TAG, "retrieveSongList.NewSongLanguageOrdered");
                if (filterString != null && !filterString.isEmpty()) {
                    restApi.getNewSongsByLanguage((Language) objectPassed, pageSize, pageNo, filterString);
                } else {
                    restApi.getNewSongsByLanguage((Language) objectPassed, pageSize, pageNo);
                }
                break;
            case Constants.HotSongLanguageOrdered:
                Log.d(TAG, "retrieveSongList.HotSongLanguageOrdered");
                if (filterString != null && !filterString.isEmpty()) {
                    restApi.getHotSongsByLanguage((Language) objectPassed, pageSize, pageNo, filterString);
                } else {
                    restApi.getHotSongsByLanguage((Language) objectPassed, pageSize, pageNo);
                }
                break;
            case Constants.LanguageOrdered:
                Log.d(TAG, "retrieveSongList.LanguageOrdered");
                if (filterString != null && !filterString.isEmpty()) {
                    restApi.getSongsByLanguage((Language) objectPassed, pageSize, pageNo, filterString);
                } else {
                    restApi.getSongsByLanguage((Language) objectPassed, pageSize, pageNo);
                }
                break;
            case Constants.LanguageWordsOrdered:
                Log.d(TAG, "retrieveSongList.LanguageWordsOrdered");
                if (filterString != null && !filterString.isEmpty()) {
                    restApi.getSongsByLanguageNumOfWords((Language) objectPassed, numOfWords, pageSize, pageNo, filterString);
                } else {
                    restApi.getSongsByLanguageNumOfWords((Language) objectPassed, numOfWords, pageSize, pageNo);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        returnToPrevious();
    }

    private void returnToPrevious() {
        Log.d(TAG, "returnToPrevious");
        finish();
    }

    private void firstPage() {
        pageNo = 1;
        retrieveSongList();
    }

    private void previousPage() {
        pageNo--;
        if (pageNo < 1) {
            pageNo = 1;
        }
        retrieveSongList();
    }

    private void nextPage() {
        pageNo++;
        if (pageNo > totalPages) {
            pageNo = totalPages;
        }
        retrieveSongList();
    }

    private void lastPage() {
        pageNo = -1;    // represent last page
        retrieveSongList();
    }

    private class MyRestApi extends RestApiAsync<SongList> {
        @Override
        public void onResponse(Call<SongList> call, Response<SongList> response) {
            Log.d(TAG, "MyRestApi.onResponse");
            if (loadingDialog != null) loadingDialog.dismissAllowingStateLoss();
            loadingDialog = null;
            Log.d(TAG, "MyRestApi.onResponse.response.isSuccessful() = " +
                    response.isSuccessful());
            songList = response.body();
            if (!response.isSuccessful() || songList == null) {
                songList = new SongList();
                songsListEmptyTextView.setText(failedMessage);
                songsListEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                pageNo = songList.getPageNo();         // get the back value from called function
                pageSize = songList.getPageSize();     // get the back value from called function
                totalPages = songList.getTotalPages(); // get the back value from called function
                Log.d(TAG, "MyRestApi.onResponse.response.songList.getSongs().size() = " +
                        songList.getSongs().size());
                if (songList.getSongs().isEmpty()) {
                    songsListEmptyTextView.setText(noResultString);
                    songsListEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    songsListEmptyTextView.setVisibility(View.GONE);
                }
            }
            // myViewAdapter = new SongListAdapter(SongListActivity.this,
            //         songList.getSongs(), textFontSize);
            Log.d(TAG, "MyRestApi.onResponse.inject()");
            SongApplication.Companion.getAppCompBuilder()
                    .activityModule(SongListActivity.this)
                    .songArrayListModule(songList.getSongs())
                    .floatModule(textFontSize).build()
                    .inject(SongListActivity.this);
            mRecyclerView.setAdapter(myViewAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            Log.d(TAG, "MyRestApi.onResponse.response.isSearchEditTextChanged = " +
                    isSearchEditTextChanged);
            if (isSearchEditTextChanged) {
                // searchEditText.setFocusable(true);              // needed for requestFocus()
                // searchEditText.setFocusableInTouchMode(true);   // needed for requestFocus()
                // searchEditText.requestFocus();  // needed for the next two statements
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                isSearchEditTextChanged = false;
            }
        }

        @Override
        public void onFailure(Call<SongList> call, Throwable t) {
            Log.d(TAG, "MyRestApi.onFailure." + t.toString());
            if (loadingDialog != null) loadingDialog.dismissAllowingStateLoss();
            loadingDialog = null;
            songList = new SongList();
            songsListEmptyTextView.setText(failedMessage);
            songsListEmptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
