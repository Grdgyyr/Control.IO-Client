package com.grdgyyr.controlio.SensorDataHandler;

import android.hardware.Sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pepegeo on 2015-11-09.
 *
 * Nie mozna uzyc kurwa, poniewaz ta klasa nie jest streamowalna/serialzowalna i nie bedzie jej
 * mozna zapisac w postaci poliku xml czy chuj wie jakim na pliku w androidzie
 */
public abstract class ConcreteSensorData {

    protected int sensorType;
    protected HashMap<Integer, List<Float>> sensorData;

    public ConcreteSensorData(int typeOfSensor){
        this.sensorType = typeOfSensor;
    }

    public ConcreteSensorData(){
        this(Sensor.TYPE_ACCELEROMETER);
    }

    // add one row of data. following: x,y,z
    public void addSample(float[] newData){
        for (int i=0; i<3; i++){
            sensorData.get(i).add(newData[i]);
        }
    }

    // return precise one choosen value
    public float getSampleValue(int axis, int position){
        if(axis < sensorData.size())
            if(position < getSize())
                return sensorData.get(axis).get(position);
        throw new NullPointerException("Nie ma wartosci probki dla podanych parametrow");
    }

    // return list of values. following all x , or all y and so on
    public List<Float> getSampleVector(int axis){
        return sensorData.get(axis);
    }
    public void clearData(){
        for (Map.Entry<Integer, List<Float>> set : sensorData.entrySet()){
            set.getValue().clear();
        }
    }
    public int getSize(){
        return sensorData.get(0).size();
    }
    public HashMap<Integer, List<Float>> getData(){
        return sensorData;
    }


}
