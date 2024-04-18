package com.aidevu.pos.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.aidevu.pos.repository.remote.Repository;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class SessionExtWorker extends Worker {

    private static final String TAG = SessionExtWorker.class.getSimpleName();

    public Repository repository;

    public SessionExtWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @AssistedInject
    public SessionExtWorker(@Assisted Context context, @Assisted WorkerParameters workerParams, Repository repository) {
        super(context, workerParams);
        this.repository = repository;
    }

    @NonNull
    @Override
    public Worker.Result doWork() {
        try {
            return Result.success();
        } catch (Throwable throwable) {
            return Result.failure();
        }
    }
}