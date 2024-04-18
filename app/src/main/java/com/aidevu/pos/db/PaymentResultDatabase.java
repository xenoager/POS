package com.aidevu.pos.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PaymentResult.class}, version = 1,exportSchema = false)
public abstract class PaymentResultDatabase extends RoomDatabase {

    public abstract PaymentResultDao paymentResultDao();

}

