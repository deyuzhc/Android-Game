package com.deyuz.game;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.DecimalFormat;

/**
 * Created by deyuz on 2017/4/4.
 */
public class Sensor {

    private String log = "----->Sensor";
    private SensorManager mSensorManager;
    private android.hardware.Sensor SensorA;//加速度
    private android.hardware.Sensor SensorM;//磁  场

    private float aval[];
    private float mval[];

    //private android.hardware.Sensor sensor;

    // 小球方向
    private Vec2 dir;

    // 锁
    Object lock = new Object();

    Sensor(SensorManager sensorManager) {

        dir = new Vec2();
        aval = new float[3];
        mval = new float[3];

        mSensorManager = sensorManager;

        SensorA = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        SensorM = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);

        //sensor = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE);

    }

    /*// 陀螺仪传感器
    private SensorEventListener Listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Vec3 val = new Vec3(sensorEvent.values);

            float magni = val.magni();

            if (magni < 0.06f) {
                //dir=new Vec2();
                return;
            }

            // 归一化方向向量
            Vec3 _dir = val.scale(1.0f / magni);
            if (Math.abs(_dir.y) > Math.abs(_dir.x)) {
                dir = new Vec2(_dir.y, 0.0f);
            } else {
                dir = new Vec2(0.0f, -_dir.x);
            }

            //dir = new Vec2(_dir.y, -_dir.x);

            //Log.i(log, "x:\t" + _dir.x + " y:\t" + _dir.y + " z:\t" + _dir.z + " m:\t" + magni);
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {

        }
    };*/

    // 加速度监听
    private SensorEventListener Listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case android.hardware.Sensor.TYPE_ACCELEROMETER:
                    aval = event.values;
                    break;
                case android.hardware.Sensor.TYPE_MAGNETIC_FIELD:
                    mval = event.values;
                    break;
                default:
                    break;
            }
            float values[] = new float[3];
            float R[] = new float[9];
            mSensorManager.getRotationMatrix(R, null, aval, mval);
            mSensorManager.getOrientation(R, values);

            //DecimalFormat df = new DecimalFormat("0.00");

            //Log.i(log, "x:\t" + Math.toDegrees(values[0]) + " y:\t" + Math.toDegrees(values[1]) + " z:\t" + Math.toDegrees(values[2]));
            //Log.i(log, "x:" + values[0] + " y:" + values[1] + " z:" + values[2]);
            dir.x = values[2];
            dir.y = values[1];
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {

        }
    };


    void regist() {
        mSensorManager.registerListener(Listener, SensorM, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(Listener, SensorA, SensorManager.SENSOR_DELAY_UI);

        //mSensorManager.registerListener(Listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    void unregist() {
        mSensorManager.unregisterListener(Listener);
        mSensorManager.unregisterListener(Listener);

        //mSensorManager.unregisterListener(Listener);
    }

    Vec2 getDir() {
        //Log.i(log, "方向返回值未定义！");

        return dir.normal();
        //return dir;
    }
}
