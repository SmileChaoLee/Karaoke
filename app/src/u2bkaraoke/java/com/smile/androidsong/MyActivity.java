package com.smile.androidsong;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smile.androidsong.model.Constants;
import com.smile.smilelibraries.utilities.ScreenUtil;

public class MyActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    private float textFontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MyActivity");
        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, Constants.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, Constants.FontSize_Scale_Type, 0.0f);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my);

        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        final TextView mainMenuTextView = findViewById(R.id.mainMenuTextView);
        ScreenUtil.resizeTextSize(mainMenuTextView, textFontSize, Constants.FontSize_Scale_Type);

        final Button singerOrderButton = findViewById(R.id.singerOrderButton);
        ScreenUtil.resizeTextSize(singerOrderButton, textFontSize, Constants.FontSize_Scale_Type);
        singerOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent singerTypesIntent = new Intent(MyActivity.this, SingerTypeListActivity.class);
                startActivity(singerTypesIntent);
            }
        });

        final Button newSongOrderButton = findViewById(R.id.newSongOrderButton);
        ScreenUtil.resizeTextSize(newSongOrderButton, textFontSize, Constants.FontSize_Scale_Type);
        newSongOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent languagesIntent = new Intent(MyActivity.this, LanguageListActivity.class);
                languagesIntent.putExtra(Constants.OrderedFrom, Constants.NewSongOrdered);
                startActivity(languagesIntent);
            }
        });

        final Button hotSongOrderButton = findViewById(R.id.hotSongOrderButton);
        ScreenUtil.resizeTextSize(hotSongOrderButton, textFontSize, Constants.FontSize_Scale_Type);
        hotSongOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent languagesIntent = new Intent(MyActivity.this, LanguageListActivity.class);
                languagesIntent.putExtra(Constants.OrderedFrom, Constants.HotSongOrdered);
                startActivity(languagesIntent);
            }
        });

        final Button languageOrderButton = findViewById(R.id.languageOrderButton);
        ScreenUtil.resizeTextSize(languageOrderButton, textFontSize, Constants.FontSize_Scale_Type);
        languageOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLanguageOrder = new Intent(MyActivity.this , LanguageListActivity.class);
                startActivity(intentLanguageOrder);
            }
        });

        final Button exitProgramButton = findViewById(R.id.exitProgramButton);
        ScreenUtil.resizeTextSize(exitProgramButton, textFontSize, Constants.FontSize_Scale_Type);
        exitProgramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitApplication();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        quitApplication();
    }

    private void quitApplication() {
        final Handler handlerClose = new Handler();
        final int timeDelay = 200;
        handlerClose.postDelayed(new Runnable() {
            public void run() {
                // quit game
                finish();
            }
        },timeDelay);
    }
}
