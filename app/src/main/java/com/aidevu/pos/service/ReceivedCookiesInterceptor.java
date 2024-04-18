package com.aidevu.pos.service;

import android.content.Context;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ReceivedCookiesInterceptor implements Interceptor {

    private SharedPreference mDsp;

    public ReceivedCookiesInterceptor(Context context) {
        mDsp = SharedPreference.getInstanceOf(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        String sJsessionId_header = "_X_";
        String ssJsessionId_SP = "_X_";
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

            for (String header : originalResponse.headers("Set-Cookie")) {
                sJsessionId_header = "_X_";
                if (header.contains("JSESSIONID")) {
                    sJsessionId_header = header;
                    break;
                }
            }

            HashSet<String> preferences = mDsp.getHashSet(SharedPreference.KEY_COOKIE, new HashSet<String>());
            for (String cookie : preferences) {
                if (!cookie.isEmpty() && !cookie.isEmpty()) {
                    if (cookie.contains("JSESSIONID")) {
                        ssJsessionId_SP = cookie;
                        break;
                    }
                }
            }

            if (!"_X_".equals(sJsessionId_header)
                    && !sJsessionId_header.equals(ssJsessionId_SP)) {
                cookies.add(sJsessionId_header);
                mDsp.putHashSet(SharedPreference.KEY_COOKIE, cookies);
            } else if ("_X_".equals(sJsessionId_header)
                    && !"_X_".equals(ssJsessionId_SP)) {
                cookies.add(ssJsessionId_SP);
                mDsp.putHashSet(SharedPreference.KEY_COOKIE, cookies);
            }

        }

        return originalResponse;
    }
}