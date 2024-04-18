package com.aidevu.pos.viewmodels.main;

import androidx.lifecycle.MutableLiveData;

import com.aidevu.pos.base.BaseViewModel;
import com.aidevu.pos.repository.remote.Repository;
import com.aidevu.pos.utils.ErrorMessage;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class MainViewModel extends BaseViewModel {

    private final Repository repository;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

    @Inject
    public MainViewModel(Repository repository) {
        this.repository = repository;
    }

    public MutableLiveData<String> getString() {
        return mutableLiveData;
    }

    public void getString(String data) {
        disposable.add(repository.getString(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> mutableLiveData.setValue(result),
                        error -> errorState.setValue(new ErrorMessage("getString : " + error.getMessage(), System.currentTimeMillis()))
                )
        );
    }
}