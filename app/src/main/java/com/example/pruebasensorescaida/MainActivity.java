package com.example.pruebasensorescaida;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /**
     * Constants for sensors
     */
    private static final float SHAKE_THRESHOLD = 1.1f;
    private static final int SHAKE_WAIT_TIME_MS = 250;
    private static final float ROTATION_THRESHOLD = 2.0f;
    private static final int ROTATION_WAIT_TIME_MS = 100;

    /**
     * The sounds to play when a pattern is detected
     */
    private static MediaPlayer soundAcc;
    private static MediaPlayer soundGyro;

    /**
     * Sensors
     */
    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private Sensor mSensorGyr;
    private long mShakeTime = 0;
    private long mRotationTime = 0;

    /**
     * UI
     */
    private TextView mGyrox;
    private TextView mGyroy;
    private TextView mGyroz;
    private TextView mAccx;
    private TextView mAccy;
    private TextView mAccz;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyr = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //verifico que los dispositivos tenga el sensor
        if (mSensorGyr== null)
        {
            Toast.makeText(getApplicationContext(),
                    "Su dispositivo no posee Giroscopio.Para utilizar esta funcion, debe contar con este sensor.", Toast.LENGTH_LONG).show();
           // finish ();
        }

        if (mSensorAcc== null)
        {
            Toast.makeText(getApplicationContext(),
                    "Su dispositivo no posee Acelerómetro.Para utilizar esta funcion, debe contar con este sensor.", Toast.LENGTH_LONG).show();
           // finish ();
        }

        // Instanciate the sound to use
        soundAcc = MediaPlayer.create(this, R.raw.acc);
        soundGyro = MediaPlayer.create(this, R.raw.gyro);

        mGyrox = (TextView) findViewById(R.id.gyro_x);
        mGyroy = (TextView) findViewById(R.id.gyro_y);
        mGyroz = (TextView) findViewById(R.id.gyro_z);
        mAccx = (TextView) findViewById(R.id.accele_x);
        mAccy = (TextView) findViewById(R.id.accele_y);
        mAccz = (TextView) findViewById(R.id.accele_z);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGyr, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {//no se puede confiar en la exactitud (accuracy del evento)
            // de los valores capturados por el sensor
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {//si es el acelerometro
                mAccx.setText(R.string.act_main_no_acuracy); //por cada coordenada indica que no se
                mAccy.setText(R.string.act_main_no_acuracy);
                mAccz.setText(R.string.act_main_no_acuracy);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                mGyrox.setText(R.string.act_main_no_acuracy);
                mGyroy.setText(R.string.act_main_no_acuracy);
                mGyroz.setText(R.string.act_main_no_acuracy);
            }
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccx.setText("x = " + Float.toString(event.values[0]));
            mAccy.setText("y = " + Float.toString(event.values[1]));
            mAccz.setText("z = " + Float.toString(event.values[2]));
            detectShake(event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGyrox.setText("x = " + Float.toString(event.values[0]));
            mGyroy.setText("y = " + Float.toString(event.values[1]));
            mGyroz.setText("z = " + Float.toString(event.values[2]));
            detectRotation(event);
        }

    }

    /**
     * Detect a shake based on the ACCELEROMETER sensor
     *
     * @param event
     */
    private void detectShake(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement
            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            if (gForce > SHAKE_THRESHOLD) {
                soundAcc.start();
            }
        }
    }
    /**
     * Detect a rotation in on the GYROSCOPE sensor
     *
     * @param event
     */
    private void detectRotation(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            // Change background color if rate of rotation around any
            // axis and in any direction exceeds threshold;
            // otherwise, reset the color
            if (Math.abs(event.values[0]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[1]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[2]) > ROTATION_THRESHOLD) {
                soundGyro.start();
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}