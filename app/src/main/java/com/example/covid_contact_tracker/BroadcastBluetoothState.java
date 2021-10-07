package com.example.covid_contact_tracker;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BroadcastBluetoothState extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String str = "";
        if(BluetoothDevice.ACTION_FOUND.equals( action )){
            Log.d("Bluetooth","Bluetooth started");
            Toast.makeText( context, "Started", Toast.LENGTH_SHORT ).show();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            int deviceType = device.getType();
            str+=deviceName+": "+deviceType+"\n";
            Log.d("Bluetooth",str);
        }
    }
}
