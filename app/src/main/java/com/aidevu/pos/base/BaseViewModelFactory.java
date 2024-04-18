package com.aidevu.pos.base;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.aidevu.pos.ui.main.MainActivity;
import com.aidevu.pos.utils.Log;
import com.aidevu.pos.viewmodels.main.MainViewModel;

public class BaseViewModelFactory implements ViewModelProvider.Factory {

    Activity activity;

    public BaseViewModelFactory(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            Log.d("BaseViewModelFactory MainViewModel");
            return (T) new ViewModelProvider((MainActivity) activity).get(MainViewModel.class);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
