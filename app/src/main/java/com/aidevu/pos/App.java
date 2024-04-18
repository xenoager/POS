package com.aidevu.pos;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.aidevu.pos.interfaces.NfcListener;
import com.aidevu.pos.nfc.control.SerialPortListener;
import com.aidevu.pos.ui.common.dialog.AlertDialogView;
import com.aidevu.pos.ui.main.MainActivity;
import com.aidevu.pos.utils.Log;
import com.aidevu.pos.utils.NFCReader;
import com.aidevu.pos.utils.Utils;
import com.google.firebase.FirebaseApp;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

@HiltAndroidApp
public class App extends Application implements Configuration.Provider {

    private static App sInstance;
    private NFCReader nfc;

    private Context autoLogoutContext;
    private CountDownTimer autoLogoutTimer;
    private AlertDialogView autoLogoutDialog = null;

    @Inject
    public HiltWorkerFactory workerFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        FirebaseApp.initializeApp(getApplicationContext());
        nfc = new NFCReader(this, nfcListener);
        nfc.readyNFC();
        startBuiltInNfc();
        WorkManager.getInstance(this).cancelAllWork();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        WorkManager.getInstance(this).cancelAllWork();
        stopBuiltInNfc();
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .setWorkerFactory(workerFactory)
                .build();
    }

    public static App getInstance() {
        return sInstance;
    }

    private final NfcListener nfcListener = new NfcListener() {

        @Override
        public void setValue(String value) {
            String nfcId = value.replaceAll(" ", "");
            if (MainActivity.class.getName().equals(Utils.getCurrentActivity(sInstance))) {
                try {
                    if (MainActivity.context instanceof MainActivity) {
                        ((MainActivity) (MainActivity.context)).runOnUiThread(() -> ((MainActivity) (MainActivity.context)).onNfcResult(nfcId));
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        }
    };

    @SuppressLint("CheckResult")
    private void startBuiltInNfc() {
        try {
            SerialPortListener.statListen("/dev/ttyS1", 115200, 0)
                    .subscribeOn(Schedulers.io())
                    .subscribe(bytes -> {
                        Timber.e("SEO : %s", Utils.byteArrToHex(bytes));

                        int size = bytes.length;
                        int[] recvChars = new int[size];

                        for (int i = 0; i < size; i++) {
                            recvChars[i] = bytes[i] & 0xFF;
                        }

                        parseCommRecvNFC(new String(recvChars, 0, size));

                    }, Throwable::printStackTrace);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void stopBuiltInNfc() {
        try {
            SerialPortListener.stopAll();
            SerialPortListener.stop("/dev/ttyS1");
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void parseCommRecvNFC(String recvTotal) {
        int stxIdx, etxIdx, size;

        try {
            stxIdx = recvTotal.indexOf(0xAA);
            if (stxIdx < 0) {
                return;
            }
            if (recvTotal.length() < stxIdx + 5) {
                return;
            }
            size = recvTotal.charAt(stxIdx + 3) * 0xFF + recvTotal.charAt(stxIdx + 4);
            etxIdx = stxIdx + 6 + size;
            if (recvTotal.length() < stxIdx + 7 + size) {
                Timber.e("SEO [nfc] recv_len=" + recvTotal.length() + " size=" + size);
                return;
            }
            if (recvTotal.charAt(stxIdx + 6 + size) != 0xFF) { // 수신 프레임 0xFF 확인
                Timber.e("SEO [nfc] ETX 0xFF not matched");
                return;
            }

            parseDataNFC(recvTotal.substring(stxIdx + 1, etxIdx));

        } catch (Exception e) {
            Log.e(e.toString());
        }
    }

    private void parseDataNFC(String pRcv) {
        String NFC_UID;
        try {
            if (pRcv.charAt(0) == 0x01 && pRcv.charAt(1) == 0x01 && pRcv.charAt(2) == 0x00 && pRcv.charAt(3) > 0) { // ISO/IEC 14443A, Mifare Classic, Mifare Ultralight
                NFC_UID = Utils.charArray2HexString(pRcv.substring(4).toCharArray(), pRcv.charAt(3)).toUpperCase();
                Timber.i("NFC UID: %s", NFC_UID);
                Utils.beep();
                //NFC read 이벤트 발생
                nfcListener.setValue(NFC_UID);
            } else if (pRcv.charAt(0) == 0x02 && pRcv.charAt(1) == 0x01 && pRcv.charAt(2) == 0x00 && pRcv.charAt(3) > 0) { // ISO/IEC 14443B
                NFC_UID = Utils.charArray2HexString(pRcv.substring(4).toCharArray(), pRcv.charAt(3)).toUpperCase();
                Timber.i("NFC UID: %s", NFC_UID);
                Utils.beep();
                //NFC read 이벤트 발생
                nfcListener.setValue(NFC_UID);
            } else if (pRcv.charAt(0) == 0x03 && pRcv.charAt(1) == 0x01 && pRcv.charAt(2) == 0x00 && pRcv.charAt(3) > 0) { // ISO/IEC 15693
                NFC_UID = Utils.charArray2HexString(pRcv.substring(4).toCharArray(), pRcv.charAt(3)).toUpperCase();
                Timber.i("NFC UID: %s", NFC_UID);
                Utils.beep();
                //NFC read 이벤트 발생
                nfcListener.setValue(NFC_UID);
            } else if (pRcv.charAt(0) == 0x04 && pRcv.charAt(1) == 0x01 && pRcv.charAt(2) == 0x00 && pRcv.charAt(3) > 0) { // Felica
                NFC_UID = Utils.charArray2HexString(pRcv.substring(4).toCharArray(), pRcv.charAt(3)).toUpperCase();
                Timber.i("NFC UID: %s", NFC_UID);
                Utils.beep();
                //NFC read 이벤트 발생
                nfcListener.setValue(NFC_UID);
            }
        } catch (Exception e) {
            Timber.e(e.toString());
        }
    }
}