package com.grdgyyr.controlio.SensorDataHandler;

import android.hardware.Sensor;

import java.util.ArrayList;

/**
 * Created by pepegeo on 2015-11-09.
 */
public class GyroData extends ConcreteSensorData {
    public static final int PINCH = 0;
    public static final int YAW = 1;
    public static final int ROLL = 2;

    public GyroData(){
        sensorType = Sensor.TYPE_GYROSCOPE;
        sensorData.put(PINCH, new ArrayList<Float>());
        sensorData.put(YAW, new ArrayList<Float>());
        sensorData.put(ROLL, new ArrayList<Float>());
    }
}
