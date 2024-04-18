package com.aidevu.pos.di;

import com.aidevu.pos.App;
import com.aidevu.pos.service.AddCookiesInterceptor;
import com.aidevu.pos.service.ApiService;
import com.aidevu.pos.service.ReceivedCookiesInterceptor;
import com.aidevu.pos.utils.Constants;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static ConnectionPool connectionPool_ = null;
    private static OkHttpClient client = null;

    private static NetworkModule instance = null;

    //api
    private static ApiService service = null;
    private static Retrofit retrofit = null;
    //=====================================

    public ApiService getRetroService() {
        return service;
    }

    public NetworkModule(){

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

        AddCookiesInterceptor in1 = new AddCookiesInterceptor(App.getInstance());
        ReceivedCookiesInterceptor in2 = new ReceivedCookiesInterceptor(App.getInstance());

        if (null == connectionPool_) {
            connectionPool_ = new ConnectionPool();
        }

        if(client == null) {

            client = new OkHttpClient.Builder().connectionPool(connectionPool_)

                    //.addNetworkInterceptor(interceptor)
                    .addInterceptor(interceptor)

                    //.addNetworkInterceptor(in1)
                    .addInterceptor(in1)

                    .addInterceptor(in2)

                    //http 4xx 오류대응 true -> false
                    .retryOnConnectionFailure(false)  // ConnectionFailure시 다시 시도~. 여러번 호출될수 있기때문에 false. 기본 true 임

                    .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    //.sslSocketFactory(SfUtil.getPinnedCertSslSocketFactory(context))
                    .build();
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(ApiService.class);
    }


    @Provides
    @Singleton
    public static ApiService providerApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @Singleton
    public static Retrofit retrofit(OkHttpClient okHttpClient) {

        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    public static OkHttpClient providerOkHttpClient() {

        if (null == connectionPool_) {
            connectionPool_ = new ConnectionPool();
        }

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

        AddCookiesInterceptor in1 = new AddCookiesInterceptor(App.getInstance());
        ReceivedCookiesInterceptor in2 = new ReceivedCookiesInterceptor(App.getInstance());

        client = new OkHttpClient.Builder().connectionPool(connectionPool_)

                //.addNetworkInterceptor(interceptor)
                .addInterceptor(interceptor)

                //.addNetworkInterceptor(in1)
                .addInterceptor(in1)

                .addInterceptor(in2)

                //http 4xx 오류대응 true -> false
                .retryOnConnectionFailure(false)  // ConnectionFailure시 다시 시도~. 여러번 호출될수 있기때문에 false. 기본 true 임

                .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                //.sslSocketFactory(SfUtil.getPinnedCertSslSocketFactory(context))
                .build();


        return client;
    }

    public static NetworkModule getInstance() {
        if (instance == null) {
            instance = new NetworkModule();

            // 어플 새로 시작 후 새로 연결시 기존에 저장되어 있던 정보 삭제
        }

        return instance;
    }

}
