package com.aidevu.pos.nfc.control;

import com.aidevu.pos.nfc.serialport.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.core.Observable;

public class SerialPortListener {
    private static final Map<String, Boolean> mapIsListening = new ConcurrentHashMap<>();
    private static final Map<String, SerialPort> mapSerialPort = new ConcurrentHashMap<>();
    private static final List<Byte> nfcData = new ArrayList<>();

    /**
     * Open the serial port and start listening
     *
     * @param serialPortFileName
     * @param baudrate
     * @param flags
     * @return
     * @throws Exception
     */
    public static Observable<byte[]> statListen(final String serialPortFileName, int baudrate, int flags) throws Exception {
        if (mapSerialPort.get(serialPortFileName) != null) {
            throw new Exception(serialPortFileName + " : Serial port is already being received.");
        }
        SerialPort serialPort = null;
        serialPort = new SerialPort(serialPortFileName, baudrate, flags);
        final InputStream inputStream = serialPort.getInputStream();
        if (inputStream == null) {
            throw new Exception(serialPortFileName + " : There is a problem with the serial input stream");
        }
        mapSerialPort.put(serialPortFileName, serialPort);
        mapIsListening.put(serialPortFileName, true);
        return Observable.create(emitter -> {
            while (null != serialPortFileName && Boolean.TRUE.equals(mapIsListening.get(serialPortFileName))) {
                try {
                    byte[] buffer = new byte[1]; //64
                    int size = inputStream.read(buffer);
                    if (size > 0) {
                        nfcData.add(buffer[0]);
                        if (nfcData.size() == 19) {
                            emitter.onNext(toByteArray(nfcData));
                            nfcData.clear();
                        }
                    }
                } catch (IOException e) {
                    emitter.onError(e);
                    e.printStackTrace();
                    return;
                }
            }
        });

    }

    /**
     * Turn off a serial port read/write
     *
     * @param serialPortFileName
     */
    public static void stop(String serialPortFileName) {
        mapSerialPort.get(serialPortFileName).close();
        mapIsListening.remove(serialPortFileName);
        mapSerialPort.remove(serialPortFileName);
    }

    /**
     * Turn off all serial port reads and writes
     */
    public static void stopAll() {
        for (String serialPortFileName : mapSerialPort.keySet()) {
            mapSerialPort.get(serialPortFileName).close();
        }
        mapIsListening.clear();
        mapSerialPort.clear();
    }

    /**
     * Convert ArrayList to Byte Array
     */
    public static byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte[] ret = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

}
