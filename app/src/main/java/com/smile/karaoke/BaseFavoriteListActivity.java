package com.smile.karaoke;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smile.karaoke.adapters.SelectedFavoriteAdapter;
import com.smile.karaoke.constants.CommonConstants;
import com.smile.karaoke.constants.PlayerConstants;
import com.smile.karaoke.models.MySingleTon;
import com.smile.karaoke.models.SongInfo;
import com.smile.karaoke.models.SongListSQLite;
import com.smile.smilelibraries.utilities.ScreenUtil;
import java.util.ArrayList;

public class BaseFavoriteListActivity extends AppCompatActivity
        implements SelectedFavoriteAdapter.OnRecyclerItemClickListener {

    private static final String TAG = "BFavoriteListActivity";
    private final String CrudActionState = "CrudAction";
    private final String PositionEditState = "PositionEdit";
    private SongListSQLite songListSQLite;
    private float textFontSize;
    private float toastTextSize;
    private ActivityResultLauncher<Intent> editFavoritesLauncher;
    private String currentAction = CommonConstants.ADD_ACTION;
    private float weightSum = 0.f;
    private LinearLayout favoriteListLinearLayout;
    private LinearLayout favoritesTitleLayout;
    private LinearLayout favoritesExitButtonLayout;
    private RecyclerView myListRecyclerView;
    private SelectedFavoriteAdapter myRecyclerViewAdapter;
    private int positionEdit = -1;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, ScreenUtil.FontSize_Pixel_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, ScreenUtil.FontSize_Pixel_Type, 0.0f);
        // float fontScale = ScreenUtil.suitableFontScale(this, ScreenUtil.FontSize_Pixel_Type, 0.0f);
        toastTextSize = 0.8f * textFontSize;
        songListSQLite = new SongListSQLite(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        TextView myFavoritesTextView = findViewById(R.id.myFavoritesTextView);
        ScreenUtil.resizeTextSize(myFavoritesTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        Button exitFavoriteListButton = findViewById(R.id.exitFavoriteListButton);
        ScreenUtil.resizeTextSize(exitFavoriteListButton, textFontSize, ScreenUtil.FontSize_Pixel_Type);
        exitFavoriteListButton.setOnClickListener(v -> returnToPrevious());

        favoriteListLinearLayout = findViewById(R.id.favoriteListLinearLayout);
        weightSum = favoriteListLinearLayout.getWeightSum();
        favoritesTitleLayout = findViewById(R.id.favoritesTitleLayout);
        myListRecyclerView = findViewById(R.id.selectedFavoriteRecyclerView);
        myListRecyclerView.setHasFixedSize(true);
        favoritesExitButtonLayout = findViewById(R.id.favoritesExitButtonLayout);
        setLayoutViewWeight();

        ArrayList<SongInfo> tempList;
        if (savedInstanceState != null) {
            // activity being recreated
            currentAction = savedInstanceState.getString(CrudActionState);
            positionEdit = savedInstanceState.getInt(PositionEditState, -1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                tempList = (ArrayList<SongInfo>) savedInstanceState
                        .getSerializable(PlayerConstants.MyFavoriteListState, ArrayList.class);
            else
                tempList = (ArrayList<SongInfo>) savedInstanceState
                        .getSerializable(PlayerConstants.MyFavoriteListState);
            if (tempList == null) tempList = new ArrayList<>();
            Log.d(TAG, "onCreate.savedInstanceState is not null.tempList.size() = "
                    + tempList.size());
            MySingleTon.INSTANCE.getSelectedFavorites().clear();
            MySingleTon.INSTANCE.getSelectedFavorites().addAll(tempList);
        }

        editFavoritesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result == null) {
                        return;
                    }
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        updateFavoriteList(result.getData());
                    }
                });

        Log.d(TAG, "onCreate.FavoriteSingleTon.INSTANCE.getSelectedList().size() = " +
                MySingleTon.INSTANCE.getSelectedFavorites().size());

        initSelectedFavoriteRecyclerView();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "getOnBackPressedDispatcher.handleOnBackPressed");
                returnToPrevious();
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        setLayoutViewWeight();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        outState.putString(CrudActionState, currentAction);
        outState.putInt(PositionEditState, positionEdit);
        // must create a new instance for FavoriteSingleTon.INSTANCE.getSelectedList()
        // in this case
        ArrayList<SongInfo> tempList = new ArrayList<>(MySingleTon.INSTANCE.getSelectedFavorites());
        outState.putSerializable(PlayerConstants.MyFavoriteListState, tempList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MySingleTon.INSTANCE.getSelectedFavorites().clear();
        if (songListSQLite != null) {
            songListSQLite.closeDatabase();
            songListSQLite = null;
        }
        Runtime.getRuntime().gc();
        super.onDestroy();
    }

    private void returnToPrevious() {
        Log.d(TAG, "returnToPrevious");
        setResult(Activity.RESULT_OK);   // no bundle data
        finish();
    }

    private Intent createIntentFromSongDataActivity() {
        Log.d(TAG, "createIntentFromSongDataActivity");
        return new Intent(this, BaseSongDataActivity.class);
    }

    private void deleteOneSongFromFavoriteList(SongInfo singleSongInfo) {
        Log.d(TAG, "deleteOneSongFromFavoriteList");
        currentAction = CommonConstants.DELETE_ACTION;
        Intent deleteIntent = createIntentFromSongDataActivity();
        deleteIntent.putExtra(CommonConstants.CRUD_ACTION, CommonConstants.DELETE_ACTION);
        deleteIntent.putExtra(PlayerConstants.SINGLE_SONG_INFO_STATE, singleSongInfo);
        editFavoritesLauncher.launch(deleteIntent);
    }

    private void editOneSongFromFavoriteList(SongInfo singleSongInfo) {
        Log.d(TAG, "editOneSongFromFavoriteList");
        currentAction = CommonConstants.EDIT_ACTION;
        Intent editIntent = createIntentFromSongDataActivity();
        editIntent.putExtra(CommonConstants.CRUD_ACTION, CommonConstants.EDIT_ACTION);
        editIntent.putExtra(PlayerConstants.SINGLE_SONG_INFO_STATE, singleSongInfo);
        editFavoritesLauncher.launch(editIntent);
    }

    private void initSelectedFavoriteRecyclerView() {
        Log.d(TAG, "initSelectedFavoriteRecyclerView.getSelectedList() = " +
                MySingleTon.INSTANCE.getSelectedFavorites().size());

        int yellow2Color = ContextCompat.getColor(this, R.color.yellow2);
        int yellow3Color = ContextCompat.getColor(this, R.color.yellow3);

        myRecyclerViewAdapter = SelectedFavoriteAdapter.getInstance(
                this, songListSQLite,
                MySingleTon.INSTANCE.getSelectedFavorites(),
                textFontSize, yellow2Color, yellow3Color);

        myListRecyclerView.setAdapter(myRecyclerViewAdapter);
        myListRecyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean isAutoMeasureEnabled() {
                return false;
            }
        });
    }

    // implement SelectedFavoriteAdapter.OnRecyclerItemClickListener
    @Override
    public void onRecyclerItemClick(View v, int position) {
        Log.d(TAG, "onRecyclerItemClick.position = " + position);
    }

    @Override
    public void editSongButtonFunc(int position) {
        Log.d(TAG, "editSongButtonFunc.position = " + position);
        if (position<0 || position>= MySingleTon.INSTANCE.getSelectedFavorites().size()) {
            return;
        }
        Log.d(TAG, "editSongButtonFunc.positionEdit = " + positionEdit);
        Log.d(TAG, "editSongButtonFunc.editOneSongFromFavoriteList()");
        positionEdit = position;
        editOneSongFromFavoriteList(MySingleTon.INSTANCE.getSelectedFavorites().get(position));
    }
    @Override
    public void deleteSongButtonFunc(int position) {
        Log.d(TAG, "deleteSongButtonFunc.position = " + position);
        if (position<0 || position>= MySingleTon.INSTANCE.getSelectedFavorites().size()) {
            return;
        }
        positionEdit = position;
        Log.d(TAG, "deleteSongButtonFunc.positionEdit = " + positionEdit);
        deleteOneSongFromFavoriteList(MySingleTon.INSTANCE.getSelectedFavorites().get(position));
    }
    @Override
    public void playSongButtonFunc(int position) {
        // play this item (media file)
        Log.d(TAG, "playSongButtonFunc.position = " + position);
        if (position<0 || position>= MySingleTon.INSTANCE.getSelectedFavorites().size()) {
            return;
        }
        Log.d(TAG, "playSongButtonFunc.positionEdit = " + positionEdit);
        positionEdit = -1;  // no edit or delete
        currentAction = CommonConstants.PLAY_ACTION;
        /*
        // getCallingActivity() only works from startActivityForResult
        Log.d(TAG, "playSongButtonFunc.getCallingActivity() = " + getCallingActivity());
        Intent playerActivityIntent = new Intent();
        playerActivityIntent.setComponent(getCallingActivity());
        playerActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Bundle extras = new Bundle();
        extras.putBoolean(PlayerConstants.IsPlaySingleSongState, true);   // play single song
        extras.putParcelable(PlayerConstants.SingleSongInfoState,
                (MySingleTon.INSTANCE.getSelectedFavorites().get(position)));
        playerActivityIntent.putExtras(extras);
        ActivityResultLauncher<Intent> playSongLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    Log.d(TAG, "playSongButtonFunc.playSongLauncher.result");
                });
        playSongLauncher.launch(playerActivityIntent);
        */
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent bIntent = new Intent(PlayerConstants.PlaySingleSongAction);
        Bundle extras = new Bundle();
        extras.putBoolean(PlayerConstants.IS_PLAY_SINGLE_SONG_STATE, true);   // play single song
        extras.putParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE,
                (MySingleTon.INSTANCE.getSelectedFavorites().get(position)));
        bIntent.putExtras(extras);
        Log.d(TAG, "playSongButtonFunc.sendBroadcast().to play");
        broadcastManager.sendBroadcast(bIntent);
    }
    // Finish implementing SelectedFavoriteAdapter.OnRecyclerItemClickListener

    private void updateFavoriteList(Intent data) {
        Log.d(TAG, "updateFavoriteList");
        if (data != null && positionEdit != -1) {
            Log.d(TAG, "updateFavoriteList.positionEdit = " + positionEdit);
            SongInfo songInfo = data.getParcelableExtra(PlayerConstants.SINGLE_SONG_INFO_STATE);
            if (currentAction.equals(CommonConstants.EDIT_ACTION)) {
                // edit
                MySingleTon.INSTANCE.getSelectedFavorites().set(positionEdit, songInfo);
                myRecyclerViewAdapter.notifyItemChanged(positionEdit);
            } else if (currentAction.equals(CommonConstants.DELETE_ACTION)){
                // delete
                MySingleTon.INSTANCE.getSelectedFavorites().remove(positionEdit);
                myRecyclerViewAdapter.notifyItemRemoved(positionEdit);
            } else {    // currentAction = CommonConstants.PlayActionString
                Log.d(TAG, "updateFavoriteList.do nothing");
            }
        }
    }

    private void setLayoutViewWeight() {
        Point screen = ScreenUtil.getScreenSize(this);
        Log.d(TAG, "onCreate.textFontSize = " + textFontSize);
        float factor = (textFontSize * 2.5f) / screen.y;
        LinearLayout.LayoutParams layoutP = (LinearLayout.LayoutParams)favoritesTitleLayout.getLayoutParams();
        float weight = weightSum * factor;
        layoutP.weight = weight;
        layoutP = (LinearLayout.LayoutParams)favoritesExitButtonLayout.getLayoutParams();
        layoutP.weight = weight;
        layoutP = (LinearLayout.LayoutParams)myListRecyclerView.getLayoutParams();
        layoutP.weight = weightSum - weight * 2;
    }
}
