package com.example.covid_contact_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MotionDetection extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private float[] gravity,linear_acceleration;
    TextView sensorData,currData,dirData;
    boolean registered = true;
    int sample_rate = 3,sampled=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_motion_detection );
        sensorData = findViewById( R.id.display_sensordata );
        currData = findViewById( R.id.current_sensorData );
        dirData = findViewById( R.id.direction_data );
        sensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            registered = true;
            gravity = new float[3];
            linear_acceleration = new float[3];
            Log.d("Motion","Launched");
        }else{
            Toast.makeText( MotionDetection.this, "Accelerometer unavailable", Toast.LENGTH_SHORT ).show();
        }

    }

    protected void onResume() {
        super.onResume();
        registered = true;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        registered = false;
        sensorManager.unregisterListener(this);
        Log.d("Motion","Sensor Unregistered");
    }

    public void sensorStop(View v){
        if(registered) {
            registered = false;
            sensorManager.unregisterListener( this );
            Log.d( "Motion", "Sensor Unregistered" );
        }
    }
    public void sensorStart(View v){
        if(!registered) {
            registered = true;
            sensorManager.registerListener( this, sensor, SensorManager.SENSOR_DELAY_NORMAL );

            Log.d( "Motion", "Sensor Registered" );
        }
    }

    public float[] preprocessSensorData(float[] values){
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.
        final float alpha = (float) 0.8;
        Log.d("Motion","Data Changed");

        gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = values[0] - gravity[0];
        linear_acceleration[1] = values[1] - gravity[1];
        linear_acceleration[2] = values[2] - gravity[2];
        return linear_acceleration;
    }

    public void onSensorChanged(SensorEvent event){

        if(sampled>=sample_rate) {
            sampled = 1;
            linear_acceleration = preprocessSensorData( event.values );

            double cumu_acceleration = Math.sqrt( (linear_acceleration[0] * linear_acceleration[0]) +
                    (linear_acceleration[1] * linear_acceleration[1]) +
                    (linear_acceleration[2] * linear_acceleration[2]) );
            String previous = (String) sensorData.getText();
            String data = "X: " + String.format( "%.1f", linear_acceleration[0] ) + " Y: " + String.format( "%.1f", linear_acceleration[1] ) + " Z: " + String.format( "%.1f", linear_acceleration[2] );
            currData.setText( String.format( "%.1f", cumu_acceleration ) );

            if (cumu_acceleration > 2.5) {
                sensorData.setText( data + "\n" + previous );
                if (cumu_acceleration >= 8) {
                    dirData.setText( "Fast" );
                } else {
                    dirData.setText( "Slow" );
                }
            } /*else {
                sensorData.setText( "------------\n" + previous );
            }*/
        }else{
            sampled++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d("Motion","Accuracy Changed");
    }
}