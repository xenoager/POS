package com.aidevu.pos.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.aidevu.pos.repository.remote.Repository;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class DeviceInfoWorker extends Worker {

    private static final String TAG = DeviceInfoWorker.class.getSimpleName();

    public Repository repository;

    public DeviceInfoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @AssistedInject
    public DeviceInfoWorker(@Assisted Context context, @Assisted WorkerParameters workerParams, Repository repository) {
        super(context, workerParams);
        this.repository = repository;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            return Result.success();
        } catch (Throwable throwable) {
            Log.d(TAG, "Error ", throwable);
            return Result.failure();
        }
    }

}