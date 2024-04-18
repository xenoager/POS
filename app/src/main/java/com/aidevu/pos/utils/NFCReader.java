
package com.aidevu.pos.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.acs.smartcard.Features;
import com.acs.smartcard.PinModify;
import com.acs.smartcard.PinProperties;
import com.acs.smartcard.PinVerify;
import com.acs.smartcard.ReadKeyOption;
import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;
import com.acs.smartcard.TlvProperties;
import com.aidevu.pos.interfaces.NfcListener;

public class NFCReader {

    private static final String ACTION_USB_PERMISSION = "com.aidevu.pos.USB_PERMISSION";
    public static final String ACTION_USB_REFRESH = "com.aidevu.pos.USB_REFRESH";

    private static final String[] powerActionStrings = { "Power Down",
            "Cold Reset", "Warm Reset" };

    private static final String[] stateStrings = { "Unknown", "Absent",
            "Present", "Swallowed", "Powered", "Negotiable", "Specific" };

    private static final String[] featureStrings = { "FEATURE_UNKNOWN",
            "FEATURE_VERIFY_PIN_START", "FEATURE_VERIFY_PIN_FINISH",
            "FEATURE_MODIFY_PIN_START", "FEATURE_MODIFY_PIN_FINISH",
            "FEATURE_GET_KEY_PRESSED", "FEATURE_VERIFY_PIN_DIRECT",
            "FEATURE_MODIFY_PIN_DIRECT", "FEATURE_MCT_READER_DIRECT",
            "FEATURE_MCT_UNIVERSAL", "FEATURE_IFD_PIN_PROPERTIES",
            "FEATURE_ABORT", "FEATURE_SET_SPE_MESSAGE",
            "FEATURE_VERIFY_PIN_DIRECT_APP_ID",
            "FEATURE_MODIFY_PIN_DIRECT_APP_ID", "FEATURE_WRITE_DISPLAY",
            "FEATURE_GET_KEY", "FEATURE_IFD_DISPLAY_PROPERTIES",
            "FEATURE_GET_TLV_PROPERTIES", "FEATURE_CCID_ESC_COMMAND" };

    private static final String[] propertyStrings = { "Unknown", "wLcdLayout",
            "bEntryValidationCondition", "bTimeOut2", "wLcdMaxCharacters",
            "wLcdMaxLines", "bMinPINSize", "bMaxPINSize", "sFirmwareID",
            "bPPDUSupport", "dwMaxAPDUDataSize", "wIdVendor", "wIdProduct" };

    private static final int DIALOG_VERIFY_PIN_ID = 0;
    private static final int DIALOG_MODIFY_PIN_ID = 1;
    private static final int DIALOG_READ_KEY_ID = 2;
    private static final int DIALOG_DISPLAY_LCD_MESSAGE_ID = 3;

    private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;

    private static final int MAX_LINES = 25;
    private ArrayAdapter<String> mReaderAdapter;

    private Features mFeatures = new Features();
    private PinVerify mPinVerify = new PinVerify();
    private PinModify mPinModify = new PinModify();
    private ReadKeyOption mReadKeyOption = new ReadKeyOption();
    private String mLcdMessage;
    private Context context;
    private ArrayAdapter<String> mSlotAdapter;
    private String mDeviceName;
    private NfcListener nfcListener;

    public NFCReader(Context context, NfcListener nfcListener){
        this.context = context;
        this.nfcListener = nfcListener;
        mReaderAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        mSlotAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        // Get USB manager
        mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);



        // Initialize reader
        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener(new OnStateChangeListener() {

            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                // Create output string
                final String outputString = "Slot " + slotNum + ": "
                        + stateStrings[prevState] + " -> "
                        + stateStrings[currState];


                if (currState == Reader.CARD_PRESENT) {

                    Log.e("currState == Reader.CARD_PRESENT");

                    final byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};

                    final byte[] response = new byte[256];
                    try {
                        int byteCount = mReader.control(slotNum, 3500,
                                command, command.length, response, response.length);

                        //get UID
                        StringBuffer uid = new StringBuffer();

                        for (int i = 0; i < (byteCount - 2); i++) {
                            uid.append(String.format("%02X", response[i]));
                        }

                        try {
                            byte[] byteArr = toByteArray(uid.toString());
                            String str = toHexString(byteArr);
                            Log.d("value : " + str);
                            nfcListener.setValue(str);
                        } catch (NumberFormatException e) {
                            Looper.prepare();
                            Toast.makeText(context, "NumberFormatException", Toast.LENGTH_LONG);
                            Looper.loop();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                mReaderAdapter.add(device.getDeviceName());
            }
        }

        // Register receiver for USB permission
        try {
            mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_USB_PERMISSION);
            filter.addAction(ACTION_USB_REFRESH);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            context.registerReceiver(mReceiver, filter);
        } catch (Exception e) {

        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {

                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (device != null) {

                            // Open reader
                            logMsg("Opening reader: " + device.getDeviceName()
                                    + "...");
                            new OpenTask().execute(device);
                        }

                    } else {
                        logMsg("Permission denied for device " + device.getDeviceName());
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                synchronized (this) {

                    // Update reader list
                    mReaderAdapter.clear();

                    // 2개 이상일때 문제다. 선택해야하는 화면 추가해야한다.
                    for (UsbDevice device : mManager.getDeviceList().values()) {
                        if (mReader.isSupported(device)) {
                            mReaderAdapter.add(device.getDeviceName());
                        }
                    }

                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {
                        // Close reader
                        logMsg("Closing reader...");
                        new CloseTask().execute();
                    }
                }
            } else if (ACTION_USB_REFRESH.equals(action)) {
                Log.d("ACTION_USB_REFRESH");
                reFreshNFC();
            }
        }
    };

    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

        @Override
        protected Exception doInBackground(UsbDevice... params) {

            Exception result = null;

            try {

                mReader.open(params[0]);

            } catch (Exception e) {

                result = e;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {

            if (result != null) {

                logMsg(result.toString());

            } else {

                logMsg("Reader name: " + mReader.getReaderName());

                int numSlots = mReader.getNumSlots();
                logMsg("Number of slots: " + numSlots);

                // 2개 이상일때 문제다. 선택해야하는 화면 추가해야한다.
                mSlotAdapter.clear();
                for (int i = 0; i < numSlots; i++) {
                    mSlotAdapter.add(Integer.toString(i));
                }

                mFeatures.clear();
            }
        }
    }

    private class CloseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            if(mReader != null) {
                mReader.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }

    private class PowerParams {

        public int slotNum;
        public int action;
    }

    private class PowerResult {

        public byte[] atr;
        public Exception e;
    }

    private class PowerTask extends AsyncTask<PowerParams, Void, PowerResult> {

        @Override
        protected PowerResult doInBackground(PowerParams... params) {

            PowerResult result = new PowerResult();

            try {

                result.atr = mReader.power(params[0].slotNum, params[0].action);

            } catch (Exception e) {

                result.e = e;
            }

            return result;
        }

        @Override
        protected void onPostExecute(PowerResult result) {

            if (result.e != null) {

                logMsg(result.e.toString());

            } else {

                // Show ATR
                if (result.atr != null) {

                    logMsg("ATR:");
                    logBuffer(result.atr, result.atr.length);

                } else {

                    logMsg("ATR: None");
                }
            }
        }
    }

    private class SetProtocolParams {

        public int slotNum;
        public int preferredProtocols;
    }

    private class SetProtocolResult {

        public int activeProtocol;
        public Exception e;
    }

    private class SetProtocolTask extends
            AsyncTask<SetProtocolParams, Void, SetProtocolResult> {

        @Override
        protected SetProtocolResult doInBackground(SetProtocolParams... params) {

            SetProtocolResult result = new SetProtocolResult();

            try {

                result.activeProtocol = mReader.setProtocol(params[0].slotNum,
                        params[0].preferredProtocols);

            } catch (Exception e) {

                result.e = e;
            }

            return result;
        }

        @Override
        protected void onPostExecute(SetProtocolResult result) {

            if (result.e != null) {

                logMsg(result.e.toString());

            } else {

                String activeProtocolString = "Active Protocol: ";

                switch (result.activeProtocol) {

                case Reader.PROTOCOL_T0:
                    activeProtocolString += "T=0";
                    break;

                case Reader.PROTOCOL_T1:
                    activeProtocolString += "T=1";
                    break;

                default:
                    activeProtocolString += "Unknown";
                    break;
                }

                // Show active protocol
                logMsg(activeProtocolString);
            }
        }
    }

    private class TransmitParams {

        public int slotNum;
        public int controlCode;
        public String commandString;
    }

    private class TransmitProgress {

        public int controlCode;
        public byte[] command;
        public int commandLength;
        public byte[] response;
        public int responseLength;
        public Exception e;
    }

    private class TransmitTask extends
            AsyncTask<TransmitParams, TransmitProgress, Void> {

        @Override
        protected Void doInBackground(TransmitParams... params) {

            TransmitProgress progress = null;

            byte[] command = null;
            byte[] response = null;
            int responseLength = 0;
            int foundIndex = 0;
            int startIndex = 0;

            do {

                // Find carriage return
                foundIndex = params[0].commandString.indexOf('\n', startIndex);
                if (foundIndex >= 0) {
                    command = toByteArray(params[0].commandString.substring(
                            startIndex, foundIndex));
                } else {
                    command = toByteArray(params[0].commandString
                            .substring(startIndex));
                }

                // Set next start index
                startIndex = foundIndex + 1;

                response = new byte[65538];
                progress = new TransmitProgress();
                progress.controlCode = params[0].controlCode;
                try {

                    if (params[0].controlCode < 0) {

                        // Transmit APDU
                        responseLength = mReader.transmit(params[0].slotNum,
                                command, command.length, response,
                                response.length);

                    } else {

                        // Transmit control command
                        responseLength = mReader.control(params[0].slotNum,
                                params[0].controlCode, command, command.length,
                                response, response.length);
                    }

                    progress.command = command;
                    progress.commandLength = command.length;
                    progress.response = response;
                    progress.responseLength = responseLength;
                    progress.e = null;

                } catch (Exception e) {

                    progress.command = null;
                    progress.commandLength = 0;
                    progress.response = null;
                    progress.responseLength = 0;
                    progress.e = e;
                }

                publishProgress(progress);

            } while (foundIndex >= 0);

            return null;
        }

        @Override
        protected void onProgressUpdate(TransmitProgress... progress) {

            if (progress[0].e != null) {

                logMsg(progress[0].e.toString());

            } else {

                logMsg("Command:");
                logBuffer(progress[0].command, progress[0].commandLength);

                logMsg("Response:");
                logBuffer(progress[0].response, progress[0].responseLength);

                if (progress[0].response != null
                        && progress[0].responseLength > 0) {

                    int controlCode;
                    int i;

                    // Show control codes for IOCTL_GET_FEATURE_REQUEST
                    if (progress[0].controlCode == Reader.IOCTL_GET_FEATURE_REQUEST) {

                        mFeatures.fromByteArray(progress[0].response,
                                progress[0].responseLength);

                        logMsg("Features:");
                        for (i = Features.FEATURE_VERIFY_PIN_START; i <= Features.FEATURE_CCID_ESC_COMMAND; i++) {

                            controlCode = mFeatures.getControlCode(i);
                            if (controlCode >= 0) {
                                logMsg("Control Code: " + controlCode + " ("
                                        + featureStrings[i] + ")");
                            }
                        }
                    }

                    controlCode = mFeatures
                            .getControlCode(Features.FEATURE_IFD_PIN_PROPERTIES);
                    if (controlCode >= 0
                            && progress[0].controlCode == controlCode) {

                        PinProperties pinProperties = new PinProperties(
                                progress[0].response,
                                progress[0].responseLength);

                        logMsg("PIN Properties:");
                        logMsg("LCD Layout: "
                                + toHexString(pinProperties.getLcdLayout()));
                        logMsg("Entry Validation Condition: "
                                + toHexString(pinProperties
                                        .getEntryValidationCondition()));
                        logMsg("Timeout 2: "
                                + toHexString(pinProperties.getTimeOut2()));
                    }

                    controlCode = mFeatures
                            .getControlCode(Features.FEATURE_GET_TLV_PROPERTIES);
                    if (controlCode >= 0
                            && progress[0].controlCode == controlCode) {

                        TlvProperties readerProperties = new TlvProperties(
                                progress[0].response,
                                progress[0].responseLength);

                        Object property;
                        logMsg("TLV Properties:");
                        for (i = TlvProperties.PROPERTY_wLcdLayout; i <= TlvProperties.PROPERTY_wIdProduct; i++) {

                            property = readerProperties.getProperty(i);
                            if (property instanceof Integer) {
                                logMsg(propertyStrings[i] + ": "
                                        + toHexString((Integer) property));
                            } else if (property instanceof String) {
                                logMsg(propertyStrings[i] + ": " + property);
                            }
                        }
                    }
                }
            }
        }
    }

    public void readyNFC(){
        boolean requested = false;

        if(mReaderAdapter.getCount()  == 0){
            Log.d("연결된 NFC 없습니다.");
            return;
        } else if(mReaderAdapter.getCount() > 1){
            Log.d("두개 이상의 USB 인식 되어있음. 이부분 선택 할수 있게 추가 개발 해야함.");
            return;
        }

        String deviceName = mReaderAdapter.getItem(0);
//        String deviceName = (String) mReaderSpinner.getSelectedItem();

        if (deviceName != null) {

            // For each device
            for (UsbDevice device : mManager.getDeviceList().values()) {

                // If device name is found
                if (deviceName.equals(device.getDeviceName())) {

                    // Request permission
                    mManager.requestPermission(device,
                            mPermissionIntent);

                    requested = true;
                    break;
                }
            }
        }

        if (!requested) {
            Log.d("NFC usb 연결 되지 않았습니다.");
        }else{
            Log.d("NFC usb 연결 되었습니다.");
        }
    }

    public void disconnectNFC(){
        // Clear slot items
        mSlotAdapter.clear();

        // Close reader
        logMsg("Closing reader...");
        new CloseTask().execute();


        if(mReader != null) {
            mReader.close();
        }

        // Unregister receiver
        context.unregisterReceiver(mReceiver);
    }

    public void reFreshNFC(){
        mReaderAdapter.clear();
        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                mReaderAdapter.add(device.getDeviceName());
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                readyNFC();
            }
        }, 1000);
    }

    private void logMsg(String msg) {
        Log.d(msg);
    }

    /**
     * Logs the contents of buffer.
     *
     * @param buffer
     *            the buffer.
     * @param bufferLength
     *            the buffer length.
     */
    private void logBuffer(byte[] buffer, int bufferLength) {

        String bufferString = "";

        for (int i = 0; i < bufferLength; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            if (i % 16 == 0) {

                if (bufferString != "") {

                    logMsg(bufferString);
                    bufferString = "";
                }
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        if (bufferString != "") {
            logMsg(bufferString);
        }
    }

    /**
     * Converts the HEX string to byte array.
     *
     * @param hexString
     *            the HEX string.
     * @return the byte array.
     */
    private byte[] toByteArray(String hexString) {

        int hexStringLength = hexString.length();
        byte[] byteArray = null;
        int count = 0;
        char c;
        int i;

        // Count number of hex characters
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        boolean first = true;
        int len = 0;
        int value;
        for (i = 0; i < hexStringLength; i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[len] = (byte) (value << 4);

                } else {

                    byteArray[len] |= value;
                    len++;
                }

                first = !first;
            }
        }

        return byteArray;
    }

    /**
     * Converts the integer to HEX string.
     *
     * @param i
     *            the integer.
     * @return the HEX string.
     */
    private String toHexString(int i) {

        String hexString = Integer.toHexString(i);
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        return hexString.toUpperCase();
    }

    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    private String toHexString(byte[] buffer) {

        String bufferString = "";

        for (int i = 0; i < buffer.length; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        return bufferString;
    }
}
