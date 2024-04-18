package com.aidevu.pos.service;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiService {

    @POST("/jsonAction.do?")
    @FormUrlEncoded
    Observable<String> getString(String data);

    @GET
    @Streaming
    Call<ResponseBody> downloadFile(@Url String url);
}


