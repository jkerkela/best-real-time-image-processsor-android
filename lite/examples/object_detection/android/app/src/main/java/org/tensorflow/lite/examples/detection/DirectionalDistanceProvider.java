package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

class DirectionalDistanceProvider implements SensorEventListener {

    SensorManager mSensorManager;
    Sensor accSensor;
    Sensor magnetSensor;
    private float[] gravity;
    private float[] geoMagnetic;
    //for portrait mode
    private float pitch;
    private float azimut;
    //for landscape mode
    private float roll;

    DirectionalDistanceProvider(Context context) {
        initializeSensors(context);
        registerSensorListeners();
    }

    private void initializeSensors(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void registerSensorListeners() {
                mSensorManager.registerListener(this, accSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values.clone();
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geoMagnetic = event.values.clone();
        if (gravity != null && geoMagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, gravity, geoMagnetic)) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = 57.29578F * orientation[0];
                pitch = 57.29578F * orientation[1];
                roll = 57.29578F * orientation[2];
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    Float getDistanceToObjectInDirection() {
        return Math.abs((float) (1.4f * Math.tan(pitch * Math.PI / 180)));
    }
}
