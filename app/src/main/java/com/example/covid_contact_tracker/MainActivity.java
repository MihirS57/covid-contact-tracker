package com.example.covid_contact_tracker;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int REQUEST_ENABLE_BT = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if(ba == null){
            Toast.makeText( this, "Bluetooth unsupported on this device", Toast.LENGTH_SHORT ).show();
        }else{
            if(!ba.isEnabled()){
                Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult( enableBt,REQUEST_ENABLE_BT );
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(resultCode == -1){
            Toast.makeText( this, "Bluetooth enabled", Toast.LENGTH_SHORT ).show();
        }else{
            Toast.makeText( this, "Bluetooth disabled", Toast.LENGTH_SHORT ).show();
        }
    }
}