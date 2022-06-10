package com.example.covid_contact_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.os.Vibrator;
public class ObserveAcc extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor sensor,gyro_sensor;
    List<String[]> data;
    TextView sensorData,currData,dirData, motion_pred, pred_hist;
    EditText age_inp;
    Interpreter model_intrepret;
    Vibrator v;
    boolean registered = false,switchOn = false,keepScreenOn = true, rightOn = false, walkOn = false,captureOn = false,stopOn = false;
    Switch gravity_switch,right_switch,walk_switch,stop_switch;
    float[] gravity = new float[3];
    long TS_i,toSec = 1000000000L;
    int input_age,count_acc=0,count_gyro=0;
    float[] lin_acc,event_lin_acc, gyro_rate,event_gyro_rate ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_observe_acc );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        data = new ArrayList<String[]>();
        sensorData = findViewById( R.id.display_accdata );
        currData = findViewById( R.id.current_accData );
        dirData = findViewById( R.id.acc_direction_data );
        motion_pred = findViewById( R.id.motion_prediction );
        age_inp = findViewById( R.id.edit_age );
        pred_hist = findViewById( R.id.pred_history );
        gravity_switch = findViewById( R.id.gravity_switch );
        right_switch = findViewById( R.id.position );
        walk_switch = findViewById( R.id.walkOrRun );
        stop_switch = findViewById( R.id.motionOrStop );
        sensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE );
        if(sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER )!= null){
            sensor = sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
            gyro_sensor = sensorManager.getDefaultSensor( Sensor.TYPE_GYROSCOPE );
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            lin_acc = new float[3];
            gyro_rate = new float[3];
            event_lin_acc = new float[12];
            event_gyro_rate = new float[12];
            input_age = 21;
            //sensorManager.registerListener( this,sensor,SensorManager.SENSOR_DELAY_NORMAL );
            sensorManager.registerListener( this,sensor,500000 );
            sensorManager.registerListener( this,gyro_sensor,500000);
            registered = true;
            try{
                model_intrepret = new Interpreter( loadPredictionModel() );
            }catch (Exception e){
                e.printStackTrace();
            }
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

        right_switch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                    createAccCSV( null );
                    if(b){
                        Toast.makeText( ObserveAcc.this, "Right On", Toast.LENGTH_SHORT ).show();
                        rightOn = true;
                    }else{
                        Toast.makeText( ObserveAcc.this, "Left On", Toast.LENGTH_SHORT ).show();
                        rightOn = false;
                    }
                }

        });

        walk_switch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                    createAccCSV( null );
                    if(b){
                        Toast.makeText( ObserveAcc.this, "Walk On", Toast.LENGTH_SHORT ).show();
                        walkOn = true;
                    }else{
                        Toast.makeText( ObserveAcc.this, "Run On", Toast.LENGTH_SHORT ).show();
                        walkOn = false;
                    }
                }
        });
        stop_switch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                createAccCSV( null );
                if(b){
                    Toast.makeText( ObserveAcc.this, "Walk On", Toast.LENGTH_SHORT ).show();
                    stopOn = true;
                    walkOn = false;
                }else{
                    Toast.makeText( ObserveAcc.this, "Run On", Toast.LENGTH_SHORT ).show();
                    stopOn = false;
                }
            }
        });
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

    public float[] preprocessSensorValues(float[] values){
        float[] vals = new float[3];
        vals[0] = (float) (Math.round( (values[0]) * 10.0 ) / 10.0);
        vals[1] = (float) (Math.round( (values[1]) * 10.0 ) / 10.0);
        vals[2] = (float) (Math.round( (values[2]) * 10.0 ) / 10.0);
        return vals;
    }

    public float calculateAcc(float[] acc){
        float accl = (float) ((float) Math.round(Math.sqrt( (acc[0] * acc[0]) +
                (acc[1] * acc[1]) + (acc[2] * acc[2])
        )*10.0)/10.0);
        Log.d( "Acceleration",String.valueOf( accl) );
        return accl;
    }

    public String getPredictionFrom(int age){
//        if(xg == 0.0 && yg == 0.0 && zg == 0.0){
//            v.cancel();
//            return "Resting";
//        }
        int acc_index = 0,gyro_index=0;
        float[][] inputVals = new float[1][25];
        boolean acc_filled = false;
        inputVals[0][0] = age;
        for(int i=1;i<25;i++){
            if(!acc_filled){
                inputVals[0][i] = event_lin_acc[acc_index];
                acc_index++;
                if(acc_index%3 == 0){
                    acc_filled = true;
                }
            }else{
                inputVals[0][i] = event_gyro_rate[gyro_index];
                gyro_index++;
                if(gyro_index%3 == 0){
                    acc_filled = false;
                }
            }
        }
//        inputVals[0][0] = age;
//        inputVals[0][1] = x;
//        inputVals[0][2] = y;
//        inputVals[0][3] = z;
//        inputVals[0][4] = xg;
//        inputVals[0][5] = yg;
//        inputVals[0][6] = zg;


        float[][] outputVals = new float[1][3];
        model_intrepret.run( inputVals,outputVals );
        if(outputVals[0][0]>outputVals[0][1] && outputVals[0][0]>outputVals[0][2]){
            //pred_hist.setText(pred_hist.getText()+"\nWalking");
            v.cancel();
            return "Resting"+outputVals[0][0];
        }else if(outputVals[0][1]>outputVals[0][0] && outputVals[0][1]>outputVals[0][2]){
            //pred_hist.setText(pred_hist.getText()+"\nWalking");
            v.cancel();
            return "Walking"+outputVals[0][1];
        } else{

            v.vibrate(new long[] {0, 4000,100}, 0);
            return "Running"+outputVals[0][2];
        }
    }

    public void storeInEventBuffer(int mode, int count_event){  // mode 1 for lin acc 2 for gyro
        if(mode == 1){
            switch(count_acc){
                case 0:
                    event_lin_acc[0] = lin_acc[0];
                    event_lin_acc[1] = lin_acc[1];
                    event_lin_acc[2] = lin_acc[2];
                    break;
                case 1:
                    event_lin_acc[3] = lin_acc[0];
                    event_lin_acc[4] = lin_acc[1];
                    event_lin_acc[5] = lin_acc[2];
                    break;
                case 2:
                    event_lin_acc[6] = lin_acc[0];
                    event_lin_acc[7] = lin_acc[1];
                    event_lin_acc[8] = lin_acc[2];
                    break;
                case 3:
                    event_lin_acc[9] = lin_acc[0];
                    event_lin_acc[10] = lin_acc[1];
                    event_lin_acc[11] = lin_acc[2];
                    break;
            }
        }else{
            switch(count_acc){
                case 0:
                    event_gyro_rate[0] = gyro_rate[0];
                    event_gyro_rate[1] = gyro_rate[1];
                    event_gyro_rate[2] = gyro_rate[2];
                    break;
                case 1:
                    event_gyro_rate[3] = gyro_rate[0];
                    event_gyro_rate[4] = gyro_rate[1];
                    event_gyro_rate[5] = gyro_rate[2];
                    break;
                case 2:
                    event_gyro_rate[6] = gyro_rate[0];
                    event_gyro_rate[7] = gyro_rate[1];
                    event_gyro_rate[8] = gyro_rate[2];
                    break;
                case 3:
                    event_gyro_rate[9] = gyro_rate[0];
                    event_gyro_rate[10] = gyro_rate[1];
                    event_gyro_rate[11] = gyro_rate[2];
                    break;
            }
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if(getListLength() == 0){
            TS_i = event.timestamp;
        }
        if(event.accuracy>=SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                lin_acc = preprocessSensorData( event.values );
                if(count_acc<4){
                    storeInEventBuffer(1,count_acc);
                    count_acc++;
                }else{
                    count_acc = 0;
                    storeInEventBuffer(1,count_acc);
                }
            }else{
                gyro_rate = preprocessSensorValues(event.values);
                if(count_gyro<4){
                    storeInEventBuffer(2,count_gyro);
                    count_gyro++;
                }else{
                    count_gyro = 0;
                    storeInEventBuffer(2,count_gyro);
                }
            }
            //float[] linear_acceleration = preprocessSensorData( event.values );
            float accVal = calculateAcc( lin_acc );
            String ts_c = String.format("%.1f",(float)(event.timestamp - TS_i)/(float)toSec);

            if(rightOn && lin_acc[0] != -1000 && gyro_rate[0] != -1000 && count_acc == 4 && count_gyro == 4){
                String prediction = getPredictionFrom( input_age);
                motion_pred.setText( prediction );
            }else if(!rightOn){
                v.cancel();
                motion_pred.setText( "prediction Off" );
            }

            if(captureOn && lin_acc[0] != -1000 && gyro_rate[0] != -1000) {
                String[] vals = {(ts_c),
                        String.valueOf( input_age ),
                        String.valueOf( stopOn ? 1 : 0 ),
                        String.valueOf( !stopOn && walkOn ? 1 : 0 ),
                        String.valueOf( !stopOn && !walkOn ? 1 : 0 ),
                        String.valueOf( lin_acc[0] ),
                        String.valueOf( lin_acc[1] ),
                        String.valueOf( lin_acc[2] ),
                        String.valueOf( gyro_rate[0] ),
                        String.valueOf( gyro_rate[1] ),
                        String.valueOf( gyro_rate[2] ),
                        String.valueOf( accVal ),
                        };
                addToList( vals );
                lin_acc[0] = -1000;
                gyro_rate[0] = -1000;
            }

            sensorData.setText( lin_acc[0]+", "+lin_acc[1]+", "+ lin_acc[2]+", "+ gyro_rate[0]+", "+
                    gyro_rate[1]+", "+ gyro_rate[2]+", "+ (ts_c));

        }else{
            sensorData.setText( "Low Accuracy"+sensorData.getText() );
        }
    }

    private MappedByteBuffer loadPredictionModel() throws IOException{
        //AssetFileDescriptor fileDescriptor = this.getAssets().openFd( "run_walk_model_v1.4_80p.tflite" );
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd( "run_walk_random_model_v1.2.tflite" );

        //AssetFileDescriptor fileDescriptor = this.getAssets().openFd( "run_walk_model_v1.1.tflite" );
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void accStart(View view){
        //v.vibrate( 2000 );
        captureOn = true;
        registered = true;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyro_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        input_age = Integer.valueOf( age_inp.getText().toString());
    }

    public void accStop(View view){
        captureOn = false;
        registered = false;
        sensorManager.unregisterListener(this);
        v.vibrate( 5000 );

    }

    public void clearTV(View view){
        sensorData.setText( "" );
        clearList();
    }

    public int getListLength(){
        return data.size();
    }

    public void addToList(String[] vals){
        if(data.size() == 0){
            data.add( new String[]{"TS","Age","Random","Walk","Run","X_a","Y_a","Z_a","X_g","Y_g","Z_g","Acc"} );
        }
        data.add( vals );
    }

    public void clearList(){
        captureOn = false;
        data.clear();
        data.add( new String[]{"TS","Age","Random","Walk","Run","X_a","Y_a","Z_a","X_g","Y_g","Z_g","Acc"} );
    }

    public void createAccCSV(View view){
        if(!registered) {
            Date currentTime = Calendar.getInstance().getTime();
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String formattedDate = df.format(c.getTime());
            CSVWriter writer = null;
            String address = "";

            String Wmode = stopOn ? "random" : walkOn ? "walk" : "run";
            //String mode = (rightOn?"R":"L")+Wmode;
            if(switchOn){
                address = Environment.getExternalStorageDirectory().getAbsolutePath() +"LR/AccWG.csv";
            }else {
                String name = "Sample_"+Wmode+"_dataset_"+formattedDate+".csv";
                address = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+name;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ){
            accStart( null );
        }
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && registered){
            accStop( null );
            createAccCSV( null );
        }
        else {
            super.onKeyDown(keyCode, event);
        }
        return true;
    }

    protected void onResume() {
        super.onResume();
        registered = true;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyro_sensor, SensorManager.SENSOR_DELAY_NORMAL);
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