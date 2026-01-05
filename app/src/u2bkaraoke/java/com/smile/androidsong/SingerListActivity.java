package com.smile.androidsong;

import android.annotation.SuppressLint;
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

import com.smile.androidsong.model.Constants;
import com.smile.androidsong.model.SingerList;
import com.smile.androidsong.model.SingerType;
import com.smile.androidsong.retrofit.RestApiAsync;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.utilities.ScreenUtil;
import com.smile.androidsong.view_adapter.SingerListAdapter;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;

public class SingerListActivity extends AppCompatActivity {
    private static final String TAG = "SingerListActivity";
    private float textFontSize;
    private EditText searchEditText;
    private boolean isSearchEditTextChanged;
    private String filterString;
    private TextView singerListEmptyTextView;
    private RecyclerView mRecyclerView;
    @Inject
    SingerListAdapter myViewAdapter;
    private SingerList singerList = null;
    private SingerType singerType;

    private int pageNo = 1;
    private int pageSize = 10;
    private int totalPages = 0;
    private String noResultString;
    private String failedMessage;
    private String loadingString;
    private AlertDialogFragment loadingDialog = null;

    private MyRestApi restApi;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        noResultString = getString(R.string.noResultString);
        failedMessage = getString(R.string.failedMessage);
        loadingString = getString(R.string.loadingString);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, Constants.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, Constants.FontSize_Scale_Type, 0.0f);

        String singerListTitle = getString(R.string.singersListString);
        String activityTitle = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null ) {
            activityTitle = extras.getString(Constants.SingerListActivityTitle, "").trim();
            singerType = extras.getParcelable(Constants.SingerTypeParcelable);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_singer_list);

        final TextView singersListMenuTextView = findViewById(R.id.singersListMenuTextView);
        ScreenUtil.resizeTextSize(singersListMenuTextView, textFontSize, Constants.FontSize_Scale_Type);
        singersListMenuTextView.setText(activityTitle + " " + singerListTitle);

        filterString = "";
        searchEditText = findViewById(R.id.singerSearchEditText);
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

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "searchEditText.addTextChangedListener.afterTextChanged");
                String content = editable.toString().trim();
                filterString = content.isEmpty() ? "" : "SingNa+" + content;
                Log.d(TAG, "searchEditText.addTextChangedListener.afterTextChanged.filterString = "
                        + filterString);
                pageNo = 1;
                isSearchEditTextChanged = true;
                // searchEditText.clearFocus();
                retrieveSingerList();
            }
        });

        singerListEmptyTextView = findViewById(R.id.singerListEmptyTextView);
        ScreenUtil.resizeTextSize(singerListEmptyTextView, textFontSize, Constants.FontSize_Scale_Type);
        singerListEmptyTextView.setVisibility(View.GONE);

        mRecyclerView = findViewById(R.id.singerListRecyclerView);

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

        final Button singersListReturnButton = findViewById(R.id.singersListReturnButton);
        ScreenUtil.resizeTextSize(singersListReturnButton, textFontSize, Constants.FontSize_Scale_Type);
        singersListReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPrevious();
            }
        });

        restApi = new MyRestApi();
        retrieveSingerList();
    }

    private void retrieveSingerList() {
        Log.d(TAG, "retrieveSingerList.filterString = " + filterString);
        if (loadingDialog == null) {
            loadingDialog = AlertDialogFragment.newInstance(loadingString,
                    Constants.FontSize_Scale_Type,
                    textFontSize, Color.RED, 0, 0, true);
            loadingDialog.show(getSupportFragmentManager(), "LoadingDialogTag");
        }
        if (filterString != null && !filterString.isEmpty()) {
            restApi.getSingersBySingerType(singerType, pageSize, pageNo, filterString);
        } else {
            restApi.getSingersBySingerType(singerType, pageSize, pageNo);
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
        retrieveSingerList();
    }

    private void previousPage() {
        pageNo--;
        if (pageNo < 1) {
            pageNo = 1;
        }
        retrieveSingerList();
    }

    private void nextPage() {
        pageNo++;
        if (pageNo > totalPages) {
            pageNo = totalPages;
        }
        retrieveSingerList();
    }

    private void lastPage() {
        pageNo = -1;    // represent last page
        retrieveSingerList();
    }

    private class MyRestApi extends RestApiAsync<SingerList> {
        @Override
        public void onResponse(Call<SingerList> call, Response<SingerList> response) {
            if (loadingDialog != null) loadingDialog.dismissAllowingStateLoss();
            loadingDialog = null;
            Log.d(TAG, "MyRestApi.onResponse.response.isSuccessful() = " +
                    response.isSuccessful());
            singerList = response.body();
            if (!response.isSuccessful() || singerList == null) {
                singerList = new SingerList();
                singerListEmptyTextView.setText(failedMessage);
                singerListEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                pageNo = singerList.getPageNo();         // get the back value from called function
                pageSize = singerList.getPageSize();     // get the back value from called function
                totalPages = singerList.getTotalPages(); // get the back value from called function
                if (singerList.getSingers().isEmpty()) {
                    singerListEmptyTextView.setText(noResultString);
                    singerListEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    singerListEmptyTextView.setVisibility(View.GONE);
                }
            }
            // myViewAdapter.setParameters(SingerListActivity.this,
            //         singerList.getSingers(), textFontSize);
            Log.d(TAG, "MyRestApi.onResponse.inject()");
            SongApplication.Companion.getAppCompBuilder()
                    .activityModule(SingerListActivity.this)
                    .singerArrayListModule(singerList.getSingers())
                    .floatModule(textFontSize).build()
                    .inject(SingerListActivity.this);
            mRecyclerView.setAdapter(myViewAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            Log.d(TAG, "MyRestApi.onResponse.response.isSearchEditTextChanged = "
                    + isSearchEditTextChanged);
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
        public void onFailure(Call call, Throwable t) {
            Log.d(TAG, "MyRestApi.onFailure." + t.toString());
            if (loadingDialog != null) loadingDialog.dismissAllowingStateLoss();
            loadingDialog = null;
            singerList = new SingerList();
            singerListEmptyTextView.setText(failedMessage);
            singerListEmptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
