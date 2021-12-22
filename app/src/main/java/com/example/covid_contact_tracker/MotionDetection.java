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

public class MotionDetection extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private float[] gravity,linear_acceleration;
    TextView sensorData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_motion_detection );
        sensorData = findViewById( R.id.display_sensordata );
        sensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        gravity = new float[3];
        linear_acceleration = new float[3];
        Log.d("Motion","Launched");
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        Log.d("Motion","Sensor Unregistered");
    }

    public void sensorStop(View v){
        sensorManager.unregisterListener(this);
        Log.d("Motion","Sensor Unregistered");
    }
    public void sensorStart(View v){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("Motion","Sensor Registered");
    }

    public void onSensorChanged(SensorEvent event){
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = (float) 0.8;
        Log.d("Motion","Data Changed");

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
        String previous = (String) sensorData.getText();
        String data = "X: "+linear_acceleration[0]+" Y: "+linear_acceleration[1]+" Z: "+linear_acceleration[2];
        sensorData.setText( data+"\n"+previous );
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d("Motion","Accuracy Changed");
    }
}