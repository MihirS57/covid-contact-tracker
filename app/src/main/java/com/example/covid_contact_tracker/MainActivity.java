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

    int REQUEST_ENABLE_BT = 0;
    TextView bt;
    BluetoothAdapter ba;
    ArrayList<Map<String,String>> devicesList;
    Map<String,String> deviceDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        bt = findViewById( R.id.bluetooth_devices );
        ba = BluetoothAdapter.getDefaultAdapter();
        if(ba == null){
            Toast.makeText( this, "Bluetooth unsupported on this device", Toast.LENGTH_SHORT ).show();
        }else{
            Log.d("Bluetooth","BA not null");
            devicesList = new ArrayList<>();
            deviceDetails = new HashMap<>();
            IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
            ba.startDiscovery();
            /*if(!ba.isEnabled()){
                Log.d("Bluetooth","Bluetooth supported");
                Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult( enableBt,REQUEST_ENABLE_BT );
            }*/

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult( enableBt,REQUEST_ENABLE_BT );*/
        IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        ba.startDiscovery();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(resultCode == -1){
            Log.d("Bluetooth","Bluetooth enabled");
            Toast.makeText( this, "Bluetooth enabled", Toast.LENGTH_SHORT ).show();
            IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
            ba.startDiscovery();

        }else{
            Log.d("Bluetooth","Bluetooth disabled");
            Toast.makeText( this, "Bluetooth disabled", Toast.LENGTH_SHORT ).show();
        }
    }*/

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Bluetooth","Receiving");
            Toast.makeText( MainActivity.this, "Receiving", Toast.LENGTH_SHORT ).show();
            String action = intent.getAction();
            String str = "";
            if(BluetoothDevice.ACTION_FOUND.equals( action )){
                Log.d("Bluetooth","Bluetooth started");
                Toast.makeText( context, "Started", Toast.LENGTH_SHORT ).show();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                int deviceType = device.getType();
                //BluetoothClass bs = device.getBluetoothClass();
                String BclassS = "";
                int Bclass = device.getBluetoothClass().getDeviceClass();
                if(Bclass>=412 && Bclass<=612){
                    BclassS = "Phone";
                }else{
                    BclassS = "Uncategorized";
                }
                String mac = device.getAddress();
                str+=deviceName+": "+deviceType+": "+mac+": "+Bclass+"\n";
                deviceDetails.put( "Device Name: ",deviceName );
                deviceDetails.put("Device Type",Integer.toString( deviceType));
                deviceDetails.put( "Bluetooth Class: ",BclassS);
                deviceDetails.put( "Device MAC: ",mac );
                if(!devicesList.contains( deviceDetails )){
                    devicesList.add( deviceDetails );
                }

                bt.setText( devicesList.toString() );
            }
        }

    };
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver( receiver );
    }
    public void discoverBt(View view){
        Log.d("Bluetooth","Bluetooth restarted");
        devicesList = new ArrayList<>();
        deviceDetails = new HashMap<>();
        unregisterReceiver( receiver );
        IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        ba.startDiscovery();
    }
}