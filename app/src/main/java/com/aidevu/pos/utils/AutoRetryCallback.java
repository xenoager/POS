package com.aidevu.pos.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;

public abstract class AutoRetryCallback<T> implements Callback<T> {

    private static Logger LOG = LoggerFactory.getLogger("AutoRetryCallback");

    private final int mRetryLimitCount;
    private int mRetryCount = 0;

    public AutoRetryCallback() {
        this.mRetryLimitCount = 5;
    }

    public AutoRetryCallback(int retryLimitCount) {
        this.mRetryLimitCount = retryLimitCount;
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        call.cancel();
        mRetryCount++;
        if (mRetryCount > mRetryLimitCount) {
            onFinalFailure(call, t);
            return;
        }
        retry(call);
    }

    private void retry(Call<T> call) {
        call.clone().enqueue(this);
    }

    public abstract void onFinalFailure(Call<T> call, Throwable t);
}
