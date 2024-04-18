package com.aidevu.pos.base;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.aidevu.pos.App;

public abstract class BaseDialog<BINDING extends ViewBinding> extends Dialog {

    protected BINDING binding;
    private App app;

    @NonNull
    protected abstract BINDING createViewBinding(LayoutInflater layoutInflater);

    protected BaseDialog(Context context) {
        super(context);
        binding = createViewBinding(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        init(context);
        setOption();
        hideSystemBar();
    }

    protected BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
        setOption();
        hideSystemBar();
    }

    protected BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
        setOption();
        hideSystemBar();
    }

    private void init(Context context) {
        app = (App) context.getApplicationContext();
    }

    private void setOption(){
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void hideSystemBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int userAction = event.getAction();
        switch (userAction) {
            case MotionEvent.ACTION_DOWN:
                hideKeyboard(app);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard(Context context) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void show() {
        super.show();
        // kts 다이얼로그 바깥쪽 클릭이 불가능하게 설정한게 풀려서 flag clear 해줘야한다.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    @Override
    public void hide() {
        if(this != null && this.isShowing()) {
            super.hide();
        }
    }
}