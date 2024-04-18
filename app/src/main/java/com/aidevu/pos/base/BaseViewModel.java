package com.aidevu.pos.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aidevu.pos.utils.ErrorMessage;

public abstract class BaseViewModel extends ViewModel {
    protected MutableLiveData<ErrorMessage> errorState = new MutableLiveData<>();

    public MutableLiveData<ErrorMessage> getErrorState() {
        return errorState;
    }
}
