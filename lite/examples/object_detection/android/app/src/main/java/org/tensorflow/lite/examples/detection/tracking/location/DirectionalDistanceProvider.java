package org.tensorflow.lite.examples.detection.tracking.location;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

public class DirectionalDistanceProvider implements SensorEventListener {

    private final float OBSERVATION_HEIGHT_IN_METERS = 1.4f;

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

    public DirectionalDistanceProvider(Context context) {
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
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geoMagnetic = event.values;
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

    public Float getDistanceToObjectInDirection(Context context) {
        int resultSanitizer = 2; //the values seem to too big by factor of multiply 2
        double angleToObject = Math.tan(pitch * Math.PI / 180);
        Float distance = (float) Math.abs ((OBSERVATION_HEIGHT_IN_METERS * angleToObject) / resultSanitizer);
        Toast.makeText(context, "Distance: " + distance + " meters", Toast.LENGTH_SHORT).show();
        return distance;

    }
}
