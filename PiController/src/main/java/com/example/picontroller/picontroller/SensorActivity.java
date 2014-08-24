package com.example.picontroller.picontroller;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;


public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mGravity;
    private Socket socket;
    private static final int SERVER_PORT = 25640;
    private static final String SERVER_IP = "192.168.0.111";
    private static final double THRESHOLD = 2.0;
    private String instruction = "";

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            List<Sensor> gravitySensors = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
//            Log.e("sensors", "you have " + gravitySensors.size() + " sensors");
            for (int i = 0; i < gravitySensors.size(); i++) {
                if ((gravitySensors.get(i).getVendor().contains("Google Inc.")) &&
                        (gravitySensors.get(i).getVersion() == 3)) {
//                    Log.e("sensors", "you chose number " + i);
                    mGravity = gravitySensors.get(i);
                }
            }
            if (mGravity == null) {
//                Log.e("sensors", "you chose default number 0");
                mGravity = gravitySensors.get(0);
            }
        } else {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
//                Log.e("sensors", "you don't have gravity sensor, use accelerometer");
                mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            } else {
//                Log.e("sensors", "you don't have any accelerometer sensor");
            }
        }

        new Thread(new NetworkThread()).start();
    }

    @Override
    public final void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public final void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
//        Log.e("event", "you got " + event.values.length + " events");
//        for (int i = 0; i < event.values.length; i++) {
//            Log.e("event", "value[" + i + "] is " + event.values[i]);
//        }
        String output;
        double x = event.values[0];
        double y = event.values[1];

        if (y > THRESHOLD) {
            if (x > THRESHOLD) {
                output = "z";
            } else if (x < -THRESHOLD) {
                output = "c";
            } else {
                output = "x";
            }
        } else if (y < -THRESHOLD) {
            if (x > THRESHOLD) {
                output = "q";
            } else if (x < -THRESHOLD) {
                output = "e";
            } else {
                output = "w";
            }
        } else {
            if (x > THRESHOLD) {
                output = "a";
            } else if (x < -THRESHOLD) {
                output = "d";
            } else {
                output = "s";
            }
        }

        if (!instruction.equals(output)) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(output);
                instruction = output;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class NetworkThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddress, SERVER_PORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
