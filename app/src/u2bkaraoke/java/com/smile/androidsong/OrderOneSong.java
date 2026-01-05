package com.smile.androidsong;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.smile.androidsong.model.Computer;
import com.smile.androidsong.model.Constants;

public class OrderOneSong extends AppCompatActivity {

    private static final String TAG = "OrderOneSong";
    private final Computer mComputer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_one_song);

        Bundle extras = getIntent().getExtras();
        String song_no, song_na, sing_na, lang_na;
        if (extras == null) {
            return;
        } else {
            song_no = extras.getString(Constants.SONG_NO).trim();
            song_na = extras.getString(Constants.SONG_NAME).trim();
            sing_na  = extras.getString(Constants.SINGER_NAME).trim();
            lang_na = extras.getString(Constants.LANGUAGE_NAME).trim();
        }

        if (song_no.isEmpty()) {
            return;
        }

        TextView songNameView = findViewById(R.id.songName);
        songNameView.setText(song_na);
        TextView songNoView = findViewById(R.id.songNo);
        songNoView.setText(song_no);
        TextView singerNameView = findViewById(R.id.singerName);
        singerNameView.setText(sing_na);
        TextView languageNameView = findViewById(R.id.languageName);
        languageNameView.setText(lang_na);

        Button SubmitButton = findViewById(R.id.SubmitButton);
        SubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "SubmitButton.onClick() = " + mComputer.getComputerId());
                final EditText editText = findViewById(R.id.inputComputerId);
                String computerID = editText.getText().toString().trim();
                if (computerID.length() >= 5) {
                    Log.d(TAG, "onCreate.Computer id = " + mComputer.getComputerId());
                    Log.d(TAG, "onCreate.Branch id = " + mComputer.getBranchId());
                    Log.d(TAG, "onCreate.Room no = " + mComputer.getRoomNo());
                    Log.d(TAG, "onCreate.Song no = " + mComputer.getSongNo());
                }
                finish();
            }
        });

        Button CancelButton = findViewById(R.id.CancelButton);
        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
