package com.example.covid_contact_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MotionDetection extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager,step_sensorManager;
    private Sensor sensor,step_sensor;
    private float[] gravity,linear_acceleration, hist_accl;
    TextView sensorData,currData,dirData,sensors_avail;
    boolean registered = true;

    String index = SensorManager.SENSOR_STATUS_ACCURACY_HIGH+"\n"+
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM+"\n"+
            SensorManager.SENSOR_STATUS_ACCURACY_LOW+"\n"+
            SensorManager.SENSOR_STATUS_UNRELIABLE;
    //int sample_rate = 3,sampled=1;

    long t1=0,t2=0;
    float v1=0;
    double a1=0,a2=0;
    float xi = (float) 0;

    int sensorDataCount = 0,timerSec = 0;
    int smoothCnt=6;
    int steps = 0,slow=0;

    float initTS = 0.0F,delay = 0.0F, timeStamp = 0.0F,motionMag = 0.0F;
    boolean slowOrNot = true;
    String temp = "";

    /*
    Calculated by first estimating the rate of output of the accelerometer and comparing it with the
    time required for the subject foot to return to initial position
    * */
    //boolean running = false;
    //double sensorRate = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_motion_detection );
        sensorData = findViewById( R.id.display_sensordata );
        currData = findViewById( R.id.current_sensorData );
        dirData = findViewById( R.id.direction_data );
        sensors_avail = findViewById( R.id.sensors_available );
        sensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );
        for(int i = 0; i<sensors.size();i++){
            temp+=sensors.get( i ).getName()+"\n";
        }
        sensors_avail.setText( temp );
        if(sensorManager.getDefaultSensor( Sensor.TYPE_STEP_DETECTOR ) != null){
            step_sensor = sensorManager.getDefaultSensor( Sensor.TYPE_STEP_DETECTOR );
            sensorManager.registerListener(this, step_sensor, SensorManager.SENSOR_DELAY_NORMAL);
            steps = 0;
            initTS = 0.0F;
            Toast.makeText( MotionDetection.this, "Step Sensor Available", Toast.LENGTH_SHORT ).show();
        }else{
            Toast.makeText( MotionDetection.this, "Step Sensor Unavailable", Toast.LENGTH_SHORT ).show();
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorDataCount = 0;
            //timerSec = 0;
            //running = false;
            registered = true;
            gravity = new float[3];
            linear_acceleration = new float[3];
            //hist_accl = new float[smoothCnt];
            //runTimer();
            Log.d("Motion","Launched");

            //dirData.setText( index );
        }else{
            Toast.makeText( MotionDetection.this, "Accelerometer unavailable", Toast.LENGTH_SHORT ).show();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d("Motion","Accuracy Changed");

    }

    protected void onResume() {
        super.onResume();
        registered = true;
        sensorManager.registerListener(this, step_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        //sensorDataCount = 0;
        //hist_accl = new float[smoothCnt];
        //timerSec = 0;
        //running = false;
        //runTimer();
        //sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        registered = false;
        sensorManager.unregisterListener(this);
        Log.d("Motion","Sensor Unregistered");
        //running = false;
    }

    public void sensorStop(View v){
        if(registered) {
            registered = false;
            sensorManager.unregisterListener( this );
            Log.d( "Motion", "Sensor Unregistered" );
            dirData.setText( "Steps: "+ steps+ " Slow: "+slow);
            initTS = 0.0F;
            steps = 0;
            slow = 0;
        }
    }
    public void sensorStart(View v){
        if(!registered) {
            registered = true;
            sensorManager.registerListener( this, sensor, SensorManager.SENSOR_DELAY_NORMAL );
            sensorManager.registerListener( this, step_sensor, SensorManager.SENSOR_DELAY_NORMAL );
            Log.d( "Motion", "Sensor Registered" );
            sensorData.setText( "" );
        }
    }

    public void clearText(View v){
        sensorData.setText("");
    }

    public void getMotionMag(float delay){
        if(delay < 512){
            slowOrNot = false;
        }else if(delay > 512 && delay < 1050){
            slowOrNot = true;
        }
    }

    public void onSensorChanged(SensorEvent event){
        timeStamp = event.timestamp/1000000L;
        if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            if(initTS == 0.0F || (timeStamp - initTS) > 3000){
                initTS = timeStamp;
            }else {
                delay = (timeStamp) - initTS;
                steps++;
                getMotionMag( delay );
                if(delay >= 400 && delay <= 1050){
                    slow++;
                }
                if(slowOrNot){
                    sensorData.setText( timeStamp + " - " + initTS + " = " + delay + " ; SLOW \n" + sensorData.getText() );
                }else{
                    sensorData.setText( timeStamp + " - " + initTS + " = " + delay + " ; FAST\n" + sensorData.getText() );
                }

                currData.setText( "Moving" );
                Log.d( "Movement","Started" );
                initTS = timeStamp;
            }

        }



        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if (event.accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM &&
                    initTS != 0.0F &&
                    (timeStamp - initTS) > 3000) {
                currData.setText( "Stopped" );
                Log.d( "Movement","Stopped" );
            }

        }
    }

     

    /*public void runTimer(){
        running = true;
        Handler handler = new Handler();
        handler.post( new Runnable() {
            @Override
            public void run() {

                sensorData.setText( String.valueOf(timerSec) );
                if (running) {
                    Log.d( "Timer In",String.valueOf( timerSec ) );
                    timerSec = timerSec+1;

                }
                if(sensorDataCount>0 && timerSec>0){
                    sensorRate = sensorDataCount/timerSec;
                }
                Log.d( "Timer Out",sensorDataCount+" / "+timerSec+" : "+sensorRate );
                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        } );
    }*/



    /*public void onSensorChanged(SensorEvent event){
        int acc = event.accuracy;

        if(event.accuracy>=SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            linear_acceleration = preprocessSensorData( event.values );

            /*float cumu_acceleration = calculateAcc( linear_acceleration );
            if (sensorDataCount > (smoothCnt - 1)) {
                double smooth_accl = getSmoothAvg();
                Log.d( "Acceleration First", String.valueOf( smooth_accl ) );
                currData.setText( String.valueOf( smooth_accl ) + " Acc: " + acc );
                sensorData.setText( temp+" -> "+String.valueOf( smooth_accl ) + "\n" + sensorData.getText() );
                sensorDataCount = 0;
                temp = "";
            } else {
                hist_accl[sensorDataCount % smoothCnt] = (float) cumu_acceleration;
                Log.d( "Acceleration Second", String.valueOf( hist_accl ) );
                //currData.setText( String.valueOf( cumu_acceleration ) );
                temp = temp+", "+(float)cumu_acceleration;
                sensorData.setText( String.valueOf( (float) cumu_acceleration ) + "\n" + sensorData.getText() );
                sensorDataCount++;
            }





        }else{
            sensorData.setText( "Low Accuracy"+sensorData.getText() );
        }
    }

     */


    public float[] preprocessSensorData(float[] values){

        final float alpha = (float) 0.8;
        Log.d("Motion","Data Changed");

        /*gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];*/

        linear_acceleration[0] = (float)(Math.round((values[0] )*10.0)/10.0);
        linear_acceleration[1] = (float)(Math.round((values[1] )*10.0)/10.0);
        linear_acceleration[2] = (float)(Math.round((values[2] )*10.0)/10.0);
        return linear_acceleration;
    }

    public float calculateAcc(float[] acc){
        float accl = (float) ((float) Math.round(Math.sqrt( (acc[0] * acc[0]) +
                        (acc[1] * acc[1]) + (acc[2] * acc[2])
                )*10.0)/10.0);
        Log.d( "Acceleration",String.valueOf( accl) );
        return accl;
    }

    public double getSmoothAvg(){   //MAA
        float sum= 0.0F;
        int count = 0;
        for(int i =0 ;i<smoothCnt;i++){
            if(hist_accl[i] != 0.0F) {
                sum += hist_accl[i];
            }else{
                count++;
            }
        }
        //return Math.round(((sum/smoothCnt)/smoothCnt)*10.0)/10.0;
        return Math.round((sum/smoothCnt)*10.0)/10.0;
    }

    /*public boolean stoppedOrNot(float[] accl){
        int count = 0;
        for(int i =0;i<smoothCnt;i++){
            if(accl[i] == 0.0){
                count++;
            }
        }
        if(count >= (0.60*smoothCnt)){
            return true;
        }else{
            return false;
        }
    }*/






}