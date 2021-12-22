package com.example.covid_contact_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class BLEScanActivity extends AppCompatActivity {

    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner bluetoothLeScanner;
    BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private Handler handler;
    TextView discoveryresults;
    Map<String,String> deviceDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_blescan );
        discoveryresults = findViewById( R.id.discoveriesBle );
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            handler = new Handler();
            deviceDetails = new HashMap<>();
            scanLeDevice();
            Log.d("BluetoothLE","BA not null");
        }else {
            Toast.makeText( BLEScanActivity.this, "Unsupported", Toast.LENGTH_SHORT ).show();
        }


    }

    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            Log.d("BluetoothLE","Scanning");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            Toast.makeText( BLEScanActivity.this, "Scanning in progress", Toast.LENGTH_SHORT ).show();
            scanning = true;

            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            Log.d("BluetoothLE","Scanning stopped");
            Toast.makeText( BLEScanActivity.this, "Scanning stopped", Toast.LENGTH_SHORT ).show();
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    Log.d("BluetoothLE","Results are up");
                    int rssi = result.getRssi();
                    String mac = device.getAddress();
                    String name = device.getName();
                    int bClass = device.getBluetoothClass().getDeviceClass();
                    String BclassS="";
                    if(bClass>=412 && bClass<=612){
                        BclassS = "Phone";

                    }else{
                        BclassS = "Other";
                    }
                    String previous = (String) discoveryresults.getText();
                    deviceDetails.put("Name",name );
                    deviceDetails.put("MAC",mac );
                    deviceDetails.put("Class",BclassS );
                    if(rssi<=0 && rssi>=-50){
                        deviceDetails.put("Proximity","CLOSE ("+rssi+")" );
                    }else if(rssi >= -70){
                        deviceDetails.put("Proximity","NEAR ("+rssi+")" );
                    }else{
                        deviceDetails.put("Proximity","FAR ("+rssi+")" );
                    }
                    discoveryresults.setText( deviceDetails.toString()+"\n"+previous );

                }
            };

    public void discoverBt(View v){

        if(scanning){
            Log.d("BluetoothLE","Scanning already in progress");
            Toast.makeText( BLEScanActivity.this, "Scanning already in progress", Toast.LENGTH_SHORT ).show();
        }else{
            Toast.makeText( BLEScanActivity.this, "Restarting discovery", Toast.LENGTH_SHORT ).show();
            discoveryresults.setText( "" );
            scanLeDevice();
        }

    }

}