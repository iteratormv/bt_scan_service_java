package com.example.bt_scan_service_java;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.device.ScanDevice;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static androidx.core.app.ActivityCompat.startActivityForResult;


public class BluetoothService extends Service {


    private static final int REQUEST_ENABLE_BT = 1;
    private static final String SPP_UUID = "e1ec7041-83ac-4d9d-8ec7-16f7c3bc5470";

    ScanDevice sd;
    BluetoothDevice bluetoothDevice;
    private  boolean isConnected = false;
    private boolean isWorked = false;
    private String tempSend = "";

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    public BluetoothService() {
    }
    final String LOG_TAG = "BluetoothLogs";
    public void onCreate() {
        super.onCreate();
        sd = new ScanDevice();
        Log.d(LOG_TAG, "onCreate");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d(LOG_TAG, "Device doesn't support Bluetooth");
        }else{
            Log.d(LOG_TAG, "Device support Bluetooth");
            Log.d(LOG_TAG, bluetoothAdapter.getName());
            Log.d(LOG_TAG, bluetoothAdapter.getAddress());
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Log.d(LOG_TAG, "Bluetooth isn't enabled");
        }else{
            Log.d(LOG_TAG, "Bluetooth is enabled");
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        Log.d(LOG_TAG, pairedDevices.toString());

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            int i = 1;
            for (BluetoothDevice device : pairedDevices) {
                this.bluetoothDevice = device;
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(LOG_TAG, i + ". " + deviceName + " " + deviceHardwareAddress);
            }
            Log.d(LOG_TAG, "connectThread to device - " + bluetoothDevice.getName());
        }
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        isWorked = false;
        isConnected = false;
        connectThread.cancel();
        connectThread.cancel();
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }
    void someTask() {
        if(!isWorked) {
    connectThread = new ConnectThread(bluetoothDevice);
    connectThread.start();
}
    }
    private class ConnectThread extends  Thread{
        private BluetoothSocket bluetoothSocket = null;
        private boolean succesess = false;
        public ConnectThread(BluetoothDevice device){
            Log.d(LOG_TAG, "ConnectThread constructor");
            BluetoothSocket tmp = null;
            try{
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                Log.d(LOG_TAG, "ConnectThread method " + method.toString());
                tmp = (BluetoothSocket) method.invoke(device, 1);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "ConnectThread don't get socket " + device.getName());
            }
                 bluetoothSocket = tmp;
            Log.d(LOG_TAG, "ConnectThread buetoothsocket - " + bluetoothSocket.toString());
        }
        public void run(){
            Log.d(LOG_TAG, "ConnectThread run");
            try {
                bluetoothSocket.connect();
                isWorked = true;
                succesess = true;
                Log.d(LOG_TAG, "ConnectThread bluetoothSocket connect");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "ConnectThread bluetoothSocket don't connect");
                cancel();
            }
            if(succesess){
                //create class object
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
                Log.d(LOG_TAG,"connectThread success and start");
                for(int i = 0;i<100000000;i++) {
                    if(!isConnected||!isWorked) break;
                    int d = i-1;
                    String dtempSend = "test send" + d + "\r\n";
                    if(dtempSend.equals(tempSend)){
                        Log.d(LOG_TAG,"+++++++++++++++++Enabled++++++++++++++++++++");

                        sd.startScan();
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sd.stopScan();
                    }else {
                        Log.d(LOG_TAG,"+++++++++++++++++Disabled++++++++++++++++++++" + "||"+dtempSend+"||"+tempSend);
                    }
                    connectedThread.write("test send" + i + "\r\n");
                    Log.d(LOG_TAG,"connectThread connectedThread.write(\"test send\" + i + \"\\r\\n\");");
                    try {
                        Thread.sleep(100);
                        Log.d(LOG_TAG,"connectThread sleep200");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG,"connectThread sleepexeption");
                    }
                }
            }
        }
        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isWorked = false;
        }
    }
    private class  ConnectedThread extends Thread{
        private final InputStream inputStream;
        private  final OutputStream outputStream;
        public ConnectedThread(BluetoothSocket bluetoothSocket){
            Log.d(LOG_TAG, "constructor connectedTHread");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            isConnected = true;
        }
        @Override
        public void run() {
//            super.run();
            boolean unTouch = true;
            int gh = 0;
            final StringBuffer buffer = new StringBuffer();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            Log.d(LOG_TAG, "connectedThread  run");
            Log.d(LOG_TAG, "ConnectedThread isConnected - " + isConnected);
            while(isConnected){
                try {
                    int bytes = bis.read();
                    buffer.append((char) bytes);
                    Log.d(LOG_TAG, "ConnectedThread buffer read - " + buffer.toString());
                    int eof = buffer.indexOf("\r\n");
                    Log.d(LOG_TAG, "ConnectedThread eof - " + eof);
                    if(eof>0){
                        tempSend=buffer.toString();
                        buffer.delete(0, buffer.length());
                    }else{
                        Log.d(LOG_TAG, "ConnectedThread dont read ");
                    }
                } catch (IOException e) {
                    Log.d(LOG_TAG,"ConnectedThread cant reed");
                    e.printStackTrace();
                }
            }
        }
        public void write(String command){
            Log.d(LOG_TAG,"connectedThread write start - " + command);
            byte[] bytes = command.getBytes();
            if(outputStream != null){
                try{
                    outputStream.write(bytes);
                    Log.d(LOG_TAG,"connectedThread write bites");
                    outputStream.flush();
                    Log.d(LOG_TAG,"connectedThread flush" + isConnected + isWorked);
                }catch (IOException e){
                    e.printStackTrace();
                    Log.d(LOG_TAG,"connectedThread write exeption");
                }
            }
        }
        public void cancel(){
            isConnected = false;
            try{
                inputStream.close();
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
