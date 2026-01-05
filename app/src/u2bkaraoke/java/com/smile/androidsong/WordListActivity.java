package com.smile.androidsong;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smile.androidsong.model.Constants;
import com.smile.androidsong.model.Language;
import com.smile.smilelibraries.utilities.ScreenUtil;
import com.smile.androidsong.view_adapter.WordListAdapter;

import android.util.Log;
import java.util.ArrayList;

import javax.inject.Inject;

public class WordListActivity extends AppCompatActivity {

    private static final String TAG = "WordListActivity";
    private String languageTitle;
    private float textFontSize;
    private RecyclerView mRecyclerView;
    @Inject
    WordListAdapter myViewAdapter;
    private ArrayList<Pair<Integer, String>> mWordList;

    private int orderedFrom = 0;
    private Language language;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        Log.d(TAG, "onCreate.inject()");
        SongApplication.Companion.getAppComponent().inject(this);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, Constants.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, Constants.FontSize_Scale_Type, 0.0f);

        orderedFrom = Constants.WordsOrdered;
        String wordsListTitle = getString(R.string.wordsListString);
        languageTitle = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            orderedFrom = extras.getInt(Constants.OrderedFrom, Constants.WordsOrdered);
            languageTitle = extras.getString(Constants.LanguageTitle).trim();
            language = extras.getParcelable(Constants.LanguageParcelable);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_word_list);

        final TextView wordsListMenuTextView = findViewById(R.id.wordsListMenuTextView);
        ScreenUtil.resizeTextSize(wordsListMenuTextView, textFontSize, Constants.FontSize_Scale_Type);
        wordsListMenuTextView.setText(languageTitle + " " + wordsListTitle);

        mWordList = new ArrayList<Pair<Integer, String>>();
        mWordList.add(new Pair<>(1, getString(R.string.oneWordOrderString)));
        mWordList.add(new Pair<>(2, getString(R.string.twoWordsOrderString)));
        mWordList.add(new Pair<>(3, getString(R.string.threeWordsOrderString)));
        mWordList.add(new Pair<>(4, getString(R.string.fourWordsOrderString)));
        mWordList.add(new Pair<>(5, getString(R.string.fiveWordsOrderString)));
        mWordList.add(new Pair<>(6, getString(R.string.sixWordsOrderString)));
        mWordList.add(new Pair<>(7, getString(R.string.sevenWordsOrderString)));
        mWordList.add(new Pair<>(8, getString(R.string.eightWordsOrderString)));
        mWordList.add(new Pair<>(9, getString(R.string.nineWordsOrderString)));
        mWordList.add(new Pair<>(10, getString(R.string.tenWordsOrderString)));

        mRecyclerView = findViewById(R.id.wordListRecyclerView);

        final Button wordsListReturnButton = findViewById(R.id.wordsListReturnButton);
        ScreenUtil.resizeTextSize(wordsListReturnButton, textFontSize, Constants.FontSize_Scale_Type);
        wordsListReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPrevious();
            }
        });

        // myViewAdapter = new WordListAdapter(WordListActivity.this,
        //         language, languageTitle, mWordList, textFontSize);
        myViewAdapter.setParameters(WordListActivity.this,
                language, languageTitle, mWordList, textFontSize);
        mRecyclerView.setAdapter(myViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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
        finish();
    }
}
