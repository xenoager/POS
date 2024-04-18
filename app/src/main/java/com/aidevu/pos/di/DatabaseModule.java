package com.aidevu.pos.di;

import android.app.Application;

import androidx.room.Room;

import com.aidevu.pos.db.PaymentResultDao;
import com.aidevu.pos.db.PaymentResultDatabase;
import com.aidevu.pos.utils.Constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    PaymentResultDatabase providePaymentResultDatabase(Application application) {
        return Room.databaseBuilder(application, PaymentResultDatabase.class, Constants.DataBaseName)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    @Provides
    @Singleton
    PaymentResultDao providePaymentResultDao(PaymentResultDatabase paymentResultDatabase) {
        return paymentResultDatabase.paymentResultDao();
    }
}
