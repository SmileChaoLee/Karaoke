package com.smile.karaoke;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smile.karaoke.adapters.SelectedFavoriteAdapter;
import com.smile.karaoke.constants.CommonConstants;
import com.smile.karaoke.constants.PlayerConstants;
import com.smile.karaoke.models.MySingleTon;
import com.smile.karaoke.models.SongInfo;
import com.smile.karaoke.models.SongListSQLite;
import com.smile.karaoke.utilities.LogUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;
import java.util.ArrayList;

public class BaseFavoriteListActivity extends AppCompatActivity
        implements SelectedFavoriteAdapter.OnRecyclerItemClickListener {

    private static final String TAG = "BFavoriteListActivity";
    private final String CrudActionState = "CrudAction";
    private final String PositionEditState = "PositionEdit";
    private SongListSQLite songListSQLite;
    private float textFontSize;
    private ActivityResultLauncher<Intent> editFavoritesLauncher;
    private String currentAction = CommonConstants.EDIT_ACTION;
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
        LogUtil.d(TAG, "onCreate");
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        songListSQLite = new SongListSQLite(getApplicationContext());

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        /*
        // Get the object that controls the system bar appearance
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Set the status bar icons to be dark
        windowInsetsController.setAppearanceLightStatusBars(true);
        */

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
            LogUtil.d(TAG, "onCreate.savedInstanceState is not null.tempList.size() = "
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

        LogUtil.d(TAG, "onCreate.FavoriteSingleTon.INSTANCE.getSelectedList().size() = " +
                MySingleTon.INSTANCE.getSelectedFavorites().size());

        initSelectedFavoriteRecyclerView();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LogUtil.d(TAG, "getOnBackPressedDispatcher.handleOnBackPressed");
                returnToPrevious();
            }
        });

        // Find the LinearLayout by its ID
        LinearLayout favoriteListLinearLayout = findViewById(R.id.favoriteListLinearLayout);
        // Get the ViewTreeObserver for the LinearLayout
        favoriteListLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Layout has been finished.
                        // Remove the listener to avoid it being called repeatedly.
                        // The removeOnGlobalLayoutListener() method is used for API 16 and above.
                        favoriteListLinearLayout.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        // Now it's safe to get the view's dimensions or perform other actions
                        // that depend on the layout being complete.
                        // do something after layout finished
                    }
                }
        );

        // this in here represent FrameLayout (R.id.activity_base_layout)
        // fix: the bottom navigation bar covers some contents
        ViewCompat.setOnApplyWindowInsetsListener(
                favoriteListLinearLayout, (v, windowInsets) -> {
                    // Get the insets for the system bars (status bar on top, navigation bar at bottom)
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    // Apply these insets as padding to your View
                    LogUtil.d(TAG, "setOnApplyWindowInsetsListener.insets.top = " + insets.top);
                    v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                    // Return CONSUMED to signal that you've handled the inset
                    return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        LogUtil.d(TAG, "onConfigurationChanged");
        setLayoutViewWeight();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        LogUtil.d(TAG, "onSaveInstanceState");
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
        LogUtil.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause");
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
        LogUtil.d(TAG, "returnToPrevious");
        setResult(Activity.RESULT_OK);   // no bundle data
        finish();
    }

    private Intent createIntentFromSongDataActivity() {
        LogUtil.d(TAG, "createIntentFromSongDataActivity");
        return new Intent(this, BaseSongDataActivity.class);
    }

    private void deleteOneSongFromFavoriteList(SongInfo singleSongInfo) {
        LogUtil.d(TAG, "deleteOneSongFromFavoriteList");
        currentAction = CommonConstants.DELETE_ACTION;
        Intent deleteIntent = createIntentFromSongDataActivity();
        deleteIntent.putExtra(CommonConstants.CRUD_ACTION, CommonConstants.DELETE_ACTION);
        deleteIntent.putExtra(PlayerConstants.SINGLE_SONG_INFO_STATE, singleSongInfo);
        editFavoritesLauncher.launch(deleteIntent);
    }

    private void editOneSongFromFavoriteList(SongInfo singleSongInfo) {
        LogUtil.d(TAG, "editOneSongFromFavoriteList");
        currentAction = CommonConstants.EDIT_ACTION;
        Intent editIntent = createIntentFromSongDataActivity();
        editIntent.putExtra(CommonConstants.CRUD_ACTION, CommonConstants.EDIT_ACTION);
        editIntent.putExtra(PlayerConstants.SINGLE_SONG_INFO_STATE, singleSongInfo);
        editFavoritesLauncher.launch(editIntent);
    }

    private void initSelectedFavoriteRecyclerView() {
        LogUtil.d(TAG, "initSelectedFavoriteRecyclerView.getSelectedList() = " +
                MySingleTon.INSTANCE.getSelectedFavorites().size());

        int yellow2Color = ContextCompat.getColor(this, R.color.yellow2);
        int yellow3Color = ContextCompat.getColor(this, R.color.yellow3);

        myRecyclerViewAdapter = new SelectedFavoriteAdapter(
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
        LogUtil.d(TAG, "onRecyclerItemClick.position = " + position);
    }

    @Override
    public void editSongButtonFunc(int position) {
        LogUtil.d(TAG, "editSongButtonFunc.position = " + position);
        if (position<0 || position>= MySingleTon.INSTANCE.getSelectedFavorites().size()) {
            return;
        }
        LogUtil.d(TAG, "editSongButtonFunc.positionEdit = " + positionEdit);
        LogUtil.d(TAG, "editSongButtonFunc.editOneSongFromFavoriteList()");
        positionEdit = position;
        editOneSongFromFavoriteList(MySingleTon.INSTANCE.getSelectedFavorites().get(position));
    }
    @Override
    public void deleteSongButtonFunc(int position) {
        LogUtil.d(TAG, "deleteSongButtonFunc.position = " + position);
        if (position<0 || position>= MySingleTon.INSTANCE.getSelectedFavorites().size()) {
            return;
        }
        positionEdit = position;
        LogUtil.d(TAG, "deleteSongButtonFunc.positionEdit = " + positionEdit);
        deleteOneSongFromFavoriteList(MySingleTon.INSTANCE.getSelectedFavorites().get(position));
    }
    @Override
    public void playSongButtonFunc(int position) {
        // play this item (media file)
        LogUtil.d(TAG, "playSongButtonFunc.position = " + position);
        if (position<0 || position>= MySingleTon.INSTANCE.getSelectedFavorites().size()) {
            return;
        }
        LogUtil.d(TAG, "playSongButtonFunc.positionEdit = " + positionEdit);
        positionEdit = -1;  // no edit or delete
        currentAction = CommonConstants.PLAY_ACTION;
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent bIntent = new Intent(PlayerConstants.PlaySingleSongAction);
        Bundle extras = new Bundle();
        extras.putBoolean(PlayerConstants.IS_PLAY_SINGLE_SONG_STATE, true);   // play single song
        extras.putParcelable(PlayerConstants.SINGLE_SONG_INFO_STATE,
                (MySingleTon.INSTANCE.getSelectedFavorites().get(position)));
        bIntent.putExtras(extras);
        LogUtil.d(TAG, "playSongButtonFunc.sendBroadcast().to play");
        broadcastManager.sendBroadcast(bIntent);
    }
    // Finish implementing SelectedFavoriteAdapter.OnRecyclerItemClickListener

    private void updateFavoriteList(Intent data) {
        LogUtil.d(TAG, "updateFavoriteList");
        if (data != null && positionEdit != -1) {
            LogUtil.d(TAG, "updateFavoriteList.positionEdit = " + positionEdit);
            SongInfo songInfo = data.getParcelableExtra(PlayerConstants.SINGLE_SONG_INFO_STATE);
            if (songInfo != null) {
                if (currentAction.equals(CommonConstants.EDIT_ACTION)) {
                    // edit
                    MySingleTon.INSTANCE.getSelectedFavorites().set(positionEdit, songInfo);
                    myRecyclerViewAdapter.notifyItemChanged(positionEdit);
                } else if (currentAction.equals(CommonConstants.DELETE_ACTION)) {
                    // delete
                    MySingleTon.INSTANCE.getSelectedFavorites().remove(positionEdit);
                    myRecyclerViewAdapter.notifyItemRemoved(positionEdit);
                } else {    // currentAction = CommonConstants.PlayActionString
                    LogUtil.d(TAG, "updateFavoriteList.do nothing");
                }
            }
        }
    }

    private void setLayoutViewWeight() {
        LogUtil.d(TAG, "setLayoutViewWeight.textFontSize = " + textFontSize);
        float weight = 10f;
        if (getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE) {
            weight = 20f;
        }
        LinearLayout.LayoutParams layoutP = (LinearLayout.LayoutParams)favoritesTitleLayout.getLayoutParams();
        LogUtil.d(TAG, "setLayoutViewWeight.weight = " + weight);
        layoutP.weight = weight;
        layoutP = (LinearLayout.LayoutParams)favoritesExitButtonLayout.getLayoutParams();
        layoutP.weight = weight;
        layoutP = (LinearLayout.LayoutParams)myListRecyclerView.getLayoutParams();
        layoutP.weight = weightSum - weight * 2;
    }
}
