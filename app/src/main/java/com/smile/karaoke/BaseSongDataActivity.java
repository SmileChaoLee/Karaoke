package com.smile.karaoke;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.smile.karaoke.adapters.SpinnerAdapter;
import com.smile.karaoke.constants.CommonConstants;
import com.smile.karaoke.constants.PlayerConstants;
import com.smile.karaoke.models.SongInfo;
import com.smile.karaoke.models.SongListSQLite;
import com.smile.karaoke.utilities.LogUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.Objects;

public class BaseSongDataActivity extends AppCompatActivity {

    private static final String TAG = "BaseSongDataActivity";
    private float toastTextSize;
    private EditText edit_titleNameEditText;
    private EditText edit_filePathEditText;
    private Spinner edit_musicTrackSpinner;
    private Spinner edit_musicChannelSpinner;
    private Spinner edit_vocalTrackSpinner;
    private Spinner edit_vocalChannelSpinner;
    private CheckBox editIncludedPlaylistCheckBox;
    protected LinearLayout karaokeSettingLayout;
    private String crudAction = null;
    private SongInfo mSongInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "onCreate() is called.");

        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        textFontSize *= 0.8f;
        toastTextSize = 0.9f * textFontSize;
        Bundle extras;
        if (savedInstanceState == null) {
            Intent callingIntent = getIntent();
            extras = callingIntent.getExtras();
            crudAction = callingIntent.getStringExtra(CommonConstants.CRUD_ACTION);
            LogUtil.d(TAG, "savedInstanceState is null.");
        } else {
            // not null, has savedInstanceState
            extras = savedInstanceState;
            crudAction = extras.getString(CommonConstants.CRUD_ACTION);
            LogUtil.d(TAG, "savedInstanceState is not null.");
        }
        if (extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                mSongInfo = extras.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE,
                        SongInfo.class);
            else mSongInfo = extras.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE);
        }

        setContentView(R.layout.activity_song_data);

        // ArrayAdapters for spinners
        ArrayList<String> numList = new ArrayList<>();
        numList.add("1");
        numList.add("2");
        numList.add("3");
        numList.add("4");
        numList.add("5");
        numList.add("6");
        numList.add("7");
        numList.add("8");
        SpinnerAdapter audioMusicTrackAdapter = new SpinnerAdapter(this, R.layout.spinner_item_layout,
                R.id.spinnerTextView, numList, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        SpinnerAdapter audioVocalTrackAdapter = new SpinnerAdapter(this, R.layout.spinner_item_layout,
                R.id.spinnerTextView, numList, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        ArrayList<String> aList = new ArrayList<>(SmileAppBase.audioChannelMap.values());
        SpinnerAdapter audioMusicChannelAdapter = new SpinnerAdapter(this, R.layout.spinner_item_layout,
                R.id.spinnerTextView, aList, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        SpinnerAdapter audioVocalChannelAdapter = new SpinnerAdapter(this, R.layout.spinner_item_layout,
                R.id.spinnerTextView, aList, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        // audioVocalChannelAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_layout);

        TextView edit_titleStringTextView = findViewById(R.id.edit_titleStringTextView);
        ScreenUtil.resizeTextSize(edit_titleStringTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_titleNameEditText = findViewById(R.id.edit_titleNameEditText);
        ScreenUtil.resizeTextSize(edit_titleNameEditText, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_titleNameEditText.setText(mSongInfo.getSongName());

        TextView edit_filePathStringTextView = findViewById(R.id.edit_filePathStringTextView);
        ScreenUtil.resizeTextSize(edit_filePathStringTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_filePathEditText = findViewById(R.id.edit_filePathEditText);
        edit_filePathEditText.setEnabled(false);
        ScreenUtil.resizeTextSize(edit_filePathEditText, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_filePathEditText.setText(mSongInfo.getFilePath());

        karaokeSettingLayout = findViewById(R.id.karaokeSettingLayout);
        //
        TextView edit_musicTrackStringTextView = findViewById(R.id.edit_musicTrackStringTextView);
        ScreenUtil.resizeTextSize(edit_musicTrackStringTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_musicTrackSpinner = findViewById(R.id.edit_musicTrackSpinner);
        edit_musicTrackSpinner.setAdapter(audioMusicTrackAdapter);
        edit_musicTrackSpinner.setSelection(mSongInfo.getMusicTrackNo() - 1);

        TextView edit_musicChannelStringTextView = findViewById(R.id.edit_musicChannelStringTextView);
        ScreenUtil.resizeTextSize(edit_musicChannelStringTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_musicChannelSpinner = findViewById(R.id.edit_musicChannelSpinner);
        edit_musicChannelSpinner.setAdapter(audioMusicChannelAdapter);
        edit_musicChannelSpinner.setSelection(mSongInfo.getMusicChannel());

        TextView edit_vocalTrackStringTextView = findViewById(R.id.edit_vocalTrackStringTextView);
        ScreenUtil.resizeTextSize(edit_vocalTrackStringTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_vocalTrackSpinner = findViewById(R.id.edit_vocalTrackSpinner);
        edit_vocalTrackSpinner.setAdapter(audioVocalTrackAdapter);
        edit_vocalTrackSpinner.setSelection(mSongInfo.getVocalTrackNo() - 1);

        TextView edit_vocalChannelStringTextView = findViewById(R.id.edit_vocalChannelStringTextView);
        ScreenUtil.resizeTextSize(edit_vocalChannelStringTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_vocalChannelSpinner = findViewById(R.id.edit_vocalChannelSpinner);
        edit_vocalChannelSpinner.setAdapter(audioVocalChannelAdapter);
        edit_vocalChannelSpinner.setSelection(mSongInfo.getVocalChannel());

        //
        // setKaraokeSettingLayoutVisibility();    // abstract method
        karaokeSettingLayout.setVisibility(View.VISIBLE);
        //

        TextView editIncludedPlaylistTextView = findViewById(R.id.editIncludedPlayListTextView);
        ScreenUtil.resizeTextSize(editIncludedPlaylistTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        editIncludedPlaylistCheckBox = findViewById(R.id.editIncludedPlaylistCheckBox);
        ScreenUtil.resizeTextSize(editIncludedPlaylistCheckBox, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        boolean isChecked = Objects.equals(mSongInfo.getIncluded(), "1");
        editIncludedPlaylistCheckBox.setChecked(isChecked);
        editIncludedPlaylistCheckBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            editIncludedPlaylistCheckBox.setChecked(isChecked1);
            editIncludedPlaylistCheckBox.jumpDrawablesToCurrentState();
        });
        editIncludedPlaylistCheckBox.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                editIncludedPlaylistTextView.setTextColor(Color.RED);
                // editIncludedPlaylistCheckBox.setBackgroundColor(Color.BLUE);
            } else {
                editIncludedPlaylistTextView.setTextColor(Color.BLACK);
                // editIncludedPlaylistCheckBox.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        final Button edit_saveOneSongButton = findViewById(R.id.edit_saveOneSongButton);
        ScreenUtil.resizeTextSize(edit_saveOneSongButton, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_saveOneSongButton.setOnClickListener(view -> {
            final boolean isValid = setSongInfoFromInput(true);
            SongListSQLite songListSQLite = new SongListSQLite(getApplicationContext());
            SongInfo songInfo;
            long databaseResult = -1;
            if (crudAction == null) return;
            switch (crudAction.toUpperCase()) {
                case CommonConstants.EDIT_ACTION:
                    // = "EDIT". Edit one record
                    if (isValid) {
                        songInfo = songListSQLite.findOneSongByUriString(mSongInfo.getFilePath());
                        if (songInfo == null) {
                            // not in the database
                            databaseResult = songListSQLite.updateOneSongFromSongList(mSongInfo);
                        } else {
                            // already in the database
                            // if (songInfo.getFilePath() == mSongInfo.getFilePath()) {
                            if (songInfo.getId() == mSongInfo.getId()) {
                                // same record because same id so update
                                databaseResult = songListSQLite.updateOneSongFromSongList(mSongInfo);
                            } else {
                                // different id then duplicate
                                ScreenUtil.showToast(BaseSongDataActivity.this, getString(R.string.duplicate_in_database),
                                        toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG);
                            }
                        }
                    }
                    break;
                case CommonConstants.DELETE_ACTION:
                    // = "DELETE". Delete one record
                    databaseResult = songListSQLite.deleteOneSongFromSongList(mSongInfo);
                    break;
            }
            songListSQLite.closeDatabase();

            if (databaseResult != -1) {
                LogUtil.d(TAG, "edit_saveOneSongButton.databaseResult != -1");
                returnToPreviousWithResult(Activity.RESULT_OK);
            }
        });

        final Button edit_exitEditSongButton = findViewById(R.id.edit_exitEditSongButton);
        ScreenUtil.resizeTextSize(edit_exitEditSongButton, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_exitEditSongButton.setOnClickListener(view -> returnToPreviousWithResult(Activity.RESULT_CANCELED));

        if (crudAction == null) {
            LogUtil.d(TAG, "onCreate.crudAction = null");
            returnToPreviousWithResult(Activity.RESULT_CANCELED);
            return;
        }
        if (mSongInfo == null) {
            LogUtil.d(TAG, "onCreate.mSongInfo = null");
            returnToPreviousWithResult(Activity.RESULT_CANCELED);
            return;
        }
        String actionButtonString;
        switch (crudAction.toUpperCase()) {
            case CommonConstants.EDIT_ACTION:
                // = "EDIT". Edit one record
                actionButtonString = getString(R.string.saveString);
                enableEditing();
                break;
            case CommonConstants.DELETE_ACTION:
                // = "DELETE". Delete one record
                actionButtonString = getString(R.string.deleteString);
                disableEditing();
                break;
            default:
                actionButtonString = "";
                returnToPreviousWithResult(Activity.RESULT_CANCELED);
        }

        edit_saveOneSongButton.setText(actionButtonString);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LogUtil.d(TAG, "getOnBackPressedDispatcher.handleOnBackPressed");
                returnToPreviousWithResult(Activity.RESULT_CANCELED);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        LogUtil.d(TAG, "onSaveInstanceState");
        setSongInfoFromInput(false);

        outState.putParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE, mSongInfo);
        outState.putString(CommonConstants.CRUD_ACTION, crudAction);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        mSongInfo = null;
    }

    private void enableEditing() {
        LogUtil.d(TAG, "enableEditing");
        edit_titleNameEditText.setEnabled(true);
        edit_filePathEditText.setEnabled(false);    // disabled all the time
        edit_musicTrackSpinner.setEnabled(true);
        edit_musicChannelSpinner.setEnabled(true);
        edit_vocalTrackSpinner.setEnabled(true);
        edit_vocalChannelSpinner.setEnabled(true);
        editIncludedPlaylistCheckBox.setEnabled(true);
    }

    private void disableEditing() {
        LogUtil.d(TAG, "disableEditing");
        edit_titleNameEditText.setEnabled(false);
        edit_filePathEditText.setEnabled(false);    // disabled all the time
        edit_musicTrackSpinner.setEnabled(false);
        edit_musicChannelSpinner.setEnabled(false);
        edit_vocalTrackSpinner.setEnabled(false);
        edit_vocalChannelSpinner.setEnabled(false);
        editIncludedPlaylistCheckBox.setEnabled(false);
    }

    private void returnToPreviousWithResult(int isOK) {
        LogUtil.d(TAG, "returnToPreviousWithResult");
        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE, mSongInfo);
        returnIntent.putExtras(extras);

        setResult(isOK, returnIntent);    // can bundle some data to previous activity
        finish();
    }

    private boolean setSongInfoFromInput(boolean hasMessage) {
        LogUtil.d(TAG, "setSongInfoFromInput");
        boolean isValid = true;

        String title = "";
        Editable text = edit_titleNameEditText.getText();
        if (text != null) {
            title = text.toString().trim();
        }
        text = edit_filePathEditText.getText();
        String filePath = "";
        if (text != null) {
            filePath = text.toString().trim();
        }

        String musicTrack = edit_musicTrackSpinner.getSelectedItem().toString();
        String musicChannel = edit_musicChannelSpinner.getSelectedItem().toString();
        String vocalTrack = edit_vocalTrackSpinner.getSelectedItem().toString();
        String vocalChannel = edit_vocalChannelSpinner.getSelectedItem().toString();
        String included = editIncludedPlaylistCheckBox.isChecked() ? "1" : "0";

        mSongInfo.setSongName(title);
        mSongInfo.setFilePath(filePath);
        mSongInfo.setMusicTrackNo(Integer.parseInt(musicTrack));
        int channel = CommonConstants.STEREO;
        Object obj = SmileAppBase.audioChannelReverseMap.get(musicChannel);
        if (obj != null) {
            channel = (int) obj;
        }
        mSongInfo.setMusicChannel(channel);
        mSongInfo.setVocalTrackNo(Integer.parseInt(vocalTrack));
        obj = SmileAppBase.audioChannelReverseMap.get(vocalChannel);
        channel = CommonConstants.STEREO;
        if (obj != null) {
            channel =(int) obj;
        }
        mSongInfo.setVocalChannel(channel);
        mSongInfo.setIncluded(included);

        if (filePath.isEmpty()) {
            isValid = false;
            if (hasMessage) {
                ScreenUtil.showToast(this, getString(R.string.filepathEmptyString),
                        toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
            }
        }

        return isValid;
    }
}
