package com.aidevu.pos.fcm;

import androidx.annotation.NonNull;

import com.aidevu.pos.repository.remote.Repository;
import com.aidevu.pos.utils.Constants;
import com.aidevu.pos.utils.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

public class PosMessagingService extends FirebaseMessagingService {

    @Inject
    public Repository repository;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("RemoteMessage : " + remoteMessage.getNotification().getBody());
        if (remoteMessage.getNotification() != null) {
            String push_string = remoteMessage.getNotification().getBody();
            Log.d("push_string : " + push_string);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("Refreshed token: " + token);
        Constants.FCM_TOKEN = token;
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        Log.d("sendRegistrationToServer 토큰 서버로 전송해야한다.: " + token);
    }
}