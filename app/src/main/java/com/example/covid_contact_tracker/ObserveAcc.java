package com.example.covid_contact_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObserveAcc extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor sensor;
    List<String[]> data;
    TextView sensorData,currData,dirData;
    boolean registered = false,switchOn = false,keepScreenOn = true;
    Switch gravity_switch;
    float[] gravity = new float[3];
    long TS_i,toSec = 1000000000L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_observe_acc );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        data = new ArrayList<String[]>();
        sensorData = findViewById( R.id.display_accdata );
        currData = findViewById( R.id.current_accData );
        dirData = findViewById( R.id.acc_direction_data );
        gravity_switch = findViewById( R.id.gravity_switch );
        sensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE );
        if(sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER )!= null){
            sensor = sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
            sensorManager.registerListener( this,sensor,SensorManager.SENSOR_DELAY_NORMAL );
            registered = true;
        }else{
            Toast.makeText( ObserveAcc.this, "Accelerometer unavailable", Toast.LENGTH_SHORT ).show();
        }

        gravity_switch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Toast.makeText( ObserveAcc.this, "Gravity On", Toast.LENGTH_SHORT ).show();
                    switchOn = true;
                }else{
                    switchOn = false;
                    Toast.makeText( ObserveAcc.this, "Gravity off", Toast.LENGTH_SHORT ).show();
                }
            }
        } );
    }

    public float[] preprocessSensorData(float[] values){

        final float alpha = (float) 0.8;
        float[] linear_acceleration = new float[3];

        Log.d("Motion","Data Changed");
        if(switchOn) {
            linear_acceleration[0] = (float) (Math.round( (values[0]) * 10.0 ) / 10.0);
            linear_acceleration[1] = (float) (Math.round( (values[1]) * 10.0 ) / 10.0);
            linear_acceleration[2] = (float) (Math.round( (values[2]) * 10.0 ) / 10.0);
        }else{
            gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];

            linear_acceleration[0] = (float) (Math.round( (values[0] - gravity[0]) * 10.0 ) / 10.0);
            linear_acceleration[1] = (float) (Math.round( (values[1] - gravity[1]) * 10.0 ) / 10.0);
            linear_acceleration[2] = (float) (Math.round( (values[2] - gravity[2]) * 10.0 ) / 10.0);
        }
        return linear_acceleration;
    }

    public float calculateAcc(float[] acc){
        float accl = (float) ((float) Math.round(Math.sqrt( (acc[0] * acc[0]) +
                (acc[1] * acc[1]) + (acc[2] * acc[2])
        )*10.0)/10.0);
        Log.d( "Acceleration",String.valueOf( accl) );
        return accl;
    }

    public void onSensorChanged(SensorEvent event) {
        if(getListLength() == 0){
            TS_i = event.timestamp;
        }
        if(event.accuracy>=SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            float[] linear_acceleration = preprocessSensorData( event.values );
            float accVal = calculateAcc( linear_acceleration );
            String ts_c = String.format("%.1f",(float)(event.timestamp - TS_i)/(float)toSec);
            String[] vals = {String.valueOf( linear_acceleration[0] ),
                    String.valueOf( linear_acceleration[1] ),
                    String.valueOf( linear_acceleration[2] ),
                    String.valueOf( accVal ),
                    (ts_c)};
            addToList( vals );
            /*sensorData.setText( linear_acceleration[0]+", "+linear_acceleration[1]+", "
            +linear_acceleration[2]+", "+(accVal) +", "+ (event.timestamp)+", "+ (TS_i)+", "+ (float)((event.timestamp - TS_i)/toSec)+"\n"+sensorData.getText());

             */
            sensorData.setText( ((event.timestamp)+", "+ (TS_i)+", "+ (event.timestamp - TS_i) + ", "+ (ts_c)));

        }else{
            sensorData.setText( "Low Accuracy"+sensorData.getText() );
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void accStart(View view){
        registered = true;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void accStop(View view){
        registered = false;
        sensorManager.unregisterListener(this);
    }

    public void clearTV(View view){
        sensorData.setText( "" );
        clearList();
    }

    public int getListLength(){
        return data.size();
    }

    public void addToList(String[] vals){
        data.add( vals );
    }

    public void clearList(){
        data.clear();
        data.add( new String[]{"X","Y","Z","Acc","Time"} );
    }

    public void createAccCSV(View view){
        if(!registered) {
            CSVWriter writer = null;
            String address = "";
            if(switchOn){
                address = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AccWGDataFile.csv";
            }else {
                address = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AccDataFile.csv";
            }
            try{
                writer = new CSVWriter( new FileWriter( address ) );
                writer.writeAll(data);
                writer.close();
                clearList();
                sensorData.setText( address );
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText( ObserveAcc.this, "Stop Sensor first", Toast.LENGTH_SHORT ).show();
        }
    }

    protected void onResume() {
        super.onResume();
        registered = true;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        if(!keepScreenOn) {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }
        keepScreenOn = true;
    }

    protected void onPause() {
        super.onPause();
        clearList();
        registered = false;
        sensorManager.unregisterListener(this);
        if(keepScreenOn){
            getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }
        keepScreenOn = false;
        Log.d("Motion","Sensor Unregistered");

    }

}