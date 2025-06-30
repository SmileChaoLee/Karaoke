package com.smile.karaokeplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
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

import com.smile.karaokeplayer.adapters.SpinnerAdapter;
import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.models.SongInfo;
import com.smile.karaokeplayer.models.SongListSQLite;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.Objects;

public class BaseSongDataActivity extends AppCompatActivity {

    private static final String TAG = "BaseSongDataActivity";
    private float toastTextSize;
    private EditText edit_titleNameEditText;
    protected EditText edit_filePathEditText;
    private Spinner edit_musicTrackSpinner;
    private Spinner edit_musicChannelSpinner;
    private Spinner edit_vocalTrackSpinner;
    private Spinner edit_vocalChannelSpinner;
    private CheckBox editIncludedPlaylistCheckBox;
    protected LinearLayout karaokeSettingLayout;

    private String actionButtonString;
    private String crudAction;
    private SongInfo mSongInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() is called.");

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, ScreenUtil.FontSize_Pixel_Type, null);
        float textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, ScreenUtil.FontSize_Pixel_Type, 0.0f);
        // float fontScale = ScreenUtil.suitableFontScale(this, ScreenUtil.FontSize_Pixel_Type, 0.0f);
        textFontSize *= 0.8f;
        toastTextSize = 0.9f * textFontSize;

        Intent callingIntent = getIntent();
        Bundle extras = callingIntent.getExtras();
        if (savedInstanceState == null) {
            crudAction = callingIntent.getStringExtra(CommonConstants.CRUD_ACTION);
            if (crudAction == null) crudAction = "";
            switch (crudAction.toUpperCase()) {
                case CommonConstants.ADD_ACTION:
                    // add one record
                    mSongInfo = new SongInfo();
                    actionButtonString = getString(R.string.addString);
                    break;
                case CommonConstants.EDIT_ACTION:
                    // = "EDIT". Edit one record
                    if (extras != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mSongInfo = extras.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE,
                                    SongInfo.class);
                        } else
                            mSongInfo = extras.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE);
                        actionButtonString = getString(R.string.saveString);
                    }
                    break;
                case CommonConstants.DELETE_ACTION:
                    // = "DELETE". Delete one record
                    if (extras != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mSongInfo = extras.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE,
                                    SongInfo.class);
                        } else {
                            mSongInfo = extras.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE);
                        }
                        actionButtonString = getString(R.string.deleteString);
                    }
                    break;
                default:
                    returnToPreviousWithResult(Activity.RESULT_CANCELED);
                    return;
            }
            Log.d(TAG, "savedInstanceState is null.");
        } else {
            // not null, has savedInstanceState
            actionButtonString = savedInstanceState.getString("ActionButtonString");
            crudAction = savedInstanceState.getString(CommonConstants.CRUD_ACTION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mSongInfo = savedInstanceState.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE,
                        SongInfo.class);
            } else mSongInfo = savedInstanceState.getParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE);
            Log.d(TAG, "savedInstanceState is not null.");
        }

        if (crudAction == null) {
            returnToPreviousWithResult(Activity.RESULT_CANCELED);
            return;
        }
        if (mSongInfo == null) {
            returnToPreviousWithResult(Activity.RESULT_CANCELED);
            return;
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
        ArrayList<String> aList = new ArrayList<>(SmileApp.audioChannelMap.values());
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

        final Button edit_saveOneSongButton = findViewById(R.id.edit_saveOneSongButton);
        ScreenUtil.resizeTextSize(edit_saveOneSongButton, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_saveOneSongButton.setText(actionButtonString);
        edit_saveOneSongButton.setOnClickListener(view -> {
            final boolean isValid = setSongInfoFromInput(true);
            SongListSQLite songListSQLite = new SongListSQLite(getApplicationContext());
            SongInfo songInfo;
            long databaseResult = -1;
            switch (crudAction.toUpperCase()) {
                case CommonConstants.ADD_ACTION:
                    // add one record
                    if (isValid) {
                        // check if this file is already in database
                        songInfo = songListSQLite.findOneSongByUriString(mSongInfo.getFilePath());
                        if (songInfo == null) {
                            databaseResult = songListSQLite.addSongToSongList(mSongInfo);
                        } else {
                            ScreenUtil.showToast(BaseSongDataActivity.this, getString(R.string.duplicate_in_database),
                                    toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG);
                        }
                    }
                    break;
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
                returnToPreviousWithResult(Activity.RESULT_OK);
            }
        });

        final Button edit_exitEditSongButton = findViewById(R.id.edit_exitEditSongButton);
        ScreenUtil.resizeTextSize(edit_exitEditSongButton, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        edit_exitEditSongButton.setOnClickListener(view -> returnToPreviousWithResult(Activity.RESULT_CANCELED));

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "getOnBackPressedDispatcher.handleOnBackPressed");
                returnToPreviousWithResult(Activity.RESULT_CANCELED);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        setSongInfoFromInput(false);

        outState.putParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE, mSongInfo);
        outState.putString(CommonConstants.CRUD_ACTION, crudAction);
        outState.putString("ActionButtonString", actionButtonString);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mSongInfo = null;
    }

    private void returnToPreviousWithResult(int isOK) {
        Log.d(TAG, "returnToPreviousWithResult");
        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE, mSongInfo);
        returnIntent.putExtras(extras);

        setResult(isOK, returnIntent);    // can bundle some data to previous activity
        finish();
    }

    private boolean setSongInfoFromInput(boolean hasMessage) {
        Log.d(TAG, "setSongInfoFromInput");
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
        Object obj = SmileApp.audioChannelReverseMap.get(musicChannel);
        if (obj != null) {
            channel = (int) obj;
        }
        mSongInfo.setMusicChannel(channel);
        mSongInfo.setVocalTrackNo(Integer.parseInt(vocalTrack));
        obj = SmileApp.audioChannelReverseMap.get(vocalChannel);
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
