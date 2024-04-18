package com.aidevu.pos.service;

import android.content.Context;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddCookiesInterceptor implements Interceptor {

    private SharedPreference mDsp;

    public AddCookiesInterceptor(Context context) {
        mDsp = SharedPreference.getInstanceOf(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        HashSet<String> preferences = mDsp.getHashSet(SharedPreference.KEY_COOKIE, new HashSet<String>());

        for (String cookie : preferences) {
            if (!cookie.isEmpty() && !cookie.isEmpty()) {
                builder.addHeader("Cookie", cookie);
            }
        }
        return chain.proceed(builder.build());
    }
}