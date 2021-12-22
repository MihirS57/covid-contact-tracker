package com.example.covid_contact_tracker;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

    }


    public void launchDiscoverBt(View view){
        Intent launchBt = new Intent(MainActivity.this,NormScanActivity.class);
        startActivity( launchBt );
    }

    public void launchBleDiscover(View v){
        Intent launchBle = new Intent(MainActivity.this,BLEScanActivity.class);
        startActivity( launchBle );
    }

    public void launchMtnDetector(View v){
        Intent launchMtn = new Intent(MainActivity.this, MotionDetection.class);
        startActivity( launchMtn );
    }


}