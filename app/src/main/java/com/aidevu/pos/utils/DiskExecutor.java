package com.aidevu.pos.utils;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*

    ex)
    @Override
    public void saveGoods(List<Goods> goods) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                goodsDao.saveGoods(goods);
            }
        };
        executor.execute(runnable);
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveGoods(List<Goods> goods);

 */
public class DiskExecutor implements Executor {

    private final Executor mExecutor;

    public DiskExecutor(){
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void execute(@NonNull Runnable command) {
        mExecutor.execute(command);
    }
}