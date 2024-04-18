package com.aidevu.pos.base;

import static com.aidevu.pos.ui.common.dialog.AlertDialogView.TYPE_ONE_BUTTON;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;
import com.aidevu.pos.App;
import com.aidevu.pos.interfaces.DialogOnClickListener;
import com.aidevu.pos.ui.common.dialog.AlertDialogView;
import com.aidevu.pos.utils.Constants;

public abstract class BaseActivity<BINDING extends ViewBinding, VM extends BaseViewModel> extends AppCompatActivity {

    protected VM viewModel;
    protected BINDING binding;

    @NonNull
    protected abstract VM createViewModel();

    @NonNull
    protected abstract BINDING createViewBinding(LayoutInflater layoutInflater);

    protected App app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemBar();
        binding = createViewBinding(LayoutInflater.from(this));

        if (binding != null) setContentView(binding.getRoot());

        viewModel = createViewModel();
        setFinishOnTouchOutside(false);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpWidth > dpHeight) {
            Constants.POS = true;
        }

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION));

        app = (App) getApplication();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(0, 0);
        binding = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    protected void observeData() {}

    protected void showDialog() {
        hideKeyboard();
    }

    protected void hideDialog() {
        hideKeyboard();
    }

    private void hideSystemBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBar();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int userAction = event.getAction();
        switch (userAction) {
            case MotionEvent.ACTION_DOWN:
                hideKeyboard();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    protected void onNfcResult(String nfcId) {}

    protected void showErrorDialog(String title, String content) {
        AlertDialogView dialogView = new AlertDialogView(this, new DialogOnClickListener() {
            @Override
            public void onClick(String str) {}
            @Override
            public void onCancel() {}
        }, TYPE_ONE_BUTTON);
        dialogView.setTitle(title);
        dialogView.setContent(content);
        dialogView.show();
    }

    protected void showErrorDialog(String title, String content, DialogOnClickListener clickListener) {
        AlertDialogView dialogView = new AlertDialogView(this, clickListener , TYPE_ONE_BUTTON);
        dialogView.setTitle(title);
        dialogView.setContent(content);
        dialogView.show();
    }
}