package com.grdgyyr.controlio.SensorDataHandler;
/**
 * Created by pepe on 2015-05-31.
 */
import android.hardware.SensorEvent;

//import static android.util.FloatMath.cos;
//import static android.util.FloatMath.sin;
//import static android.util.FloatMath.sqrt;


public class SensorFilter {

//    private SensorManager mSensorManager;
//    private ArrayList <Sensor> sensorList = new ArrayList<Sensor>();
//    private int sensorDelay;
//    private iSensorListener listener;
//
//    public SensorOutput(SensorManager sensorManager,iSensorListener newListener,
//                        ArrayList sensorList, int sensorDelay){
//        this.listener = newListener;
//        this.sensorDelay = sensorDelay;
//        this.mSensorManager = sensorManager;
//        this.sensorList = sensorList;
//        switch(typeOfSensor){
//            case ACCELEROMETER_LINEAR:
//                sensors.put(typeOfSensor, Sensor.TYPE_ACCELEROMETER);
//                break;
//            case GYROSCOPE:
//                sensors.put(typeOfSensor,Sensor.TYPE_GYROSCOPE);
//                break;
//            case ACCandGYRO:
//                sensors.put(typeOfSensor,Sensor.TYPE_ACCELEROMETER);
//                sensors.put(typeOfSensor,Sensor.TYPE_GYROSCOPE);
//                break;
//        }



    // gyro part
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private float EPSILON = 0.1f;
    // acc part
    float [] linear_acceleration = new float[3];
    float [] gravity = new float[]{0,0,0};

    public float[] getGyro(SensorEvent event){
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float)Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;

        return deltaRotationVector;
    }

    public float [] getAccLinear(SensorEvent event){
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = 0.8f;
        gravity = new float[]{0,0,0};
        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        return linear_acceleration;
    }
}
