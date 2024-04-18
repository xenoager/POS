package com.aidevu.pos.ui.common.moduleClass.liveData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.lifecycle.LiveData;

public class ConnLiveData extends LiveData<ConnModel> {

    private Context context;

    public ConnLiveData(Context context) {
        this.context = context;
    }

    @Override
    protected void onActive() {
        super.onActive();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        context.unregisterReceiver(networkReceiver);
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                NetworkInfo activeNetwork = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    switch (activeNetwork.getType()) {
                        case ConnectivityManager.TYPE_ETHERNET:
                            postValue(new ConnModel(ConnectivityManager.TYPE_ETHERNET, true));
                            break;
                        case ConnectivityManager.TYPE_WIFI:
                            postValue(new ConnModel(ConnectivityManager.TYPE_WIFI, true));
                            break;
                        case ConnectivityManager.TYPE_MOBILE:
                            postValue(new ConnModel(ConnectivityManager.TYPE_MOBILE, true));
                            break;
                    }
                } else {
                    postValue(new ConnModel(0, false));
                }
            }
        }
    };

}
