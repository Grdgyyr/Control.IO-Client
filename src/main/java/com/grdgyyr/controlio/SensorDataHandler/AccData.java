package com.grdgyyr.controlio.SensorDataHandler;

import android.hardware.Sensor;

import java.util.ArrayList;

/**
 * Created by pepegeo on 2015-11-09.
 */
public class AccData extends ConcreteSensorData{

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    public AccData() {
        sensorType = Sensor.TYPE_ACCELEROMETER;
        sensorData.put(X, new ArrayList<Float>());
        sensorData.put(Y, new ArrayList<Float>());
        sensorData.put(Z, new ArrayList<Float>());
    }
}
