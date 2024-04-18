package com.aidevu.pos.repository.remote;

import com.aidevu.pos.service.ApiService;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;

public class Repository {

    ApiService apiService;

    @Inject
    public Repository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Observable<String> getString(String data) {
        return apiService.getString(data);
    }
}