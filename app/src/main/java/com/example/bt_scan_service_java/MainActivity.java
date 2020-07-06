package com.example.bt_scan_service_java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView label;
    final String LOG_TAG = "BluetoothLogs";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        label = findViewById(R.id.label_connection_status);
        Log.d(LOG_TAG, "MainActivityOnCreate");
    }
    public void onStartService(View view) {
        startService(new Intent(this, BluetoothService.class));
        label.setText("Connect");
        Log.d(LOG_TAG, "MainActivityOnStartCommand");
    }
    public void onStopService(View view) {
        stopService(new Intent(this, BluetoothService.class));
        label.setText("Disconnect");
        Log.d(LOG_TAG, "MainActivityOnStopCommand");
    }
}