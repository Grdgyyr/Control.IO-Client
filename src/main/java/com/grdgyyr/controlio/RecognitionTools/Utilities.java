package com.grdgyyr.controlio.RecognitionTools;

import android.hardware.Sensor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.grdgyyr.controlio.SensorDataHandler.SensorData;

/**
 * Created by pepegeo on 2015-12-15.
 */
public class Utilities {
    private static Utilities util = null;
    private boolean useReduceDataSize;
    private int reduceDataSizeTo;

    private Utilities(){
        useReduceDataSize = true;
        reduceDataSizeTo = 100;
    }

    public static Utilities getInstance(){
        if(util == null)
            util = new Utilities();
        return util;
    }

    public void setUseReduceDataSize(boolean isReduce){
        this.useReduceDataSize = isReduce;
    }
    public boolean getUseReduceDataSize(){
        return this.useReduceDataSize;
    }
    public void setReduceDataSizeTo(int length){
        this.reduceDataSizeTo = length;
    }
    public int getReduceDataSizeTo(){
        return this.reduceDataSizeTo;
    }

    public HashMap<Integer,List<Float>> reduceDataSizeTo(HashMap<Integer,List<Float>> originDataSet){
        if(useReduceDataSize){
            return reduceDataSizeTo(originDataSet,reduceDataSizeTo);
        }
        return originDataSet;
    }
    public HashMap<Integer,List<Float>> reduceDataSizeTo(HashMap<Integer,List<Float>> originDataSet,
                                                         int reduceTo){
        int originSize = originDataSet.get(SensorData.X_axis).size();
        if(originSize>reduceTo){
            float samplesToRemove = originSize - reduceTo;
            float everyStepRemove = originSize / samplesToRemove;
            float floatCurrentStep = everyStepRemove; // float and int separated
            int intCurrentStep = Math.round(everyStepRemove); // float and int separated

            Iterator<Float> xIter = originDataSet.get(SensorData.X_axis).iterator();
            Iterator<Float> yIter = originDataSet.get(SensorData.Y_axis).iterator();
            Iterator<Float> zIter = originDataSet.get(SensorData.Z_axis).iterator();
            int i = 0; // step counter
            while(xIter.hasNext()){
                xIter.next();
                yIter.next();
                zIter.next();
                if(i == intCurrentStep) {
                    xIter.remove();
                    yIter.remove();
                    zIter.remove();
                    floatCurrentStep += everyStepRemove;
                    intCurrentStep = Math.round(floatCurrentStep);
                    //Log.i("REDUCTION","i:"+i+" new Step:"+intCurrentStep+ " new floatStep:"+String.valueOf(floatCurrentStep));
                }
                i++;
            }
            Log.i("REDUCTION", " reduceTo:" + reduceTo + " from:" + originSize + " final size:" +
                    originDataSet.get(SensorData.Y_axis).size());
            return originDataSet;

        }else {
            Log.e("REDUCTION", "in reduceDataSize() cannot reduce set of:" + originSize +
                    " to size of:"+reduceTo);
            return originDataSet;
        }
    }

    public SensorData returnAverageData(List<SensorData> gestureList){
        int listSize = gestureList.size();
        if(listSize == 1){
            return gestureList.get(0);
        }else {
            // first of all reduce all sets to the same size
            for(SensorData data:gestureList){
                for (Map.Entry<Integer, HashMap<Integer, List<Float>>> set : data.getData().entrySet()) {
                    set.setValue(reduceDataSizeTo(set.getValue(), RecognitionManager.MIN_ACC_LENGTH));
                }
            }
            Iterator<Float> xIter;
            Iterator<Float> yIter;
            Iterator<Float> zIter;

            HashMap<Integer, HashMap<Integer, List<List<Float>>>> sumData = new HashMap<>();
            // Get only first SensorData(firstgesture) and use it to initialize new HashMap
            HashMap<Integer, HashMap<Integer, List<Float>>> firstGesture = gestureList.get(0).getData();
            // do the first round just to initialize fields
            for (Map.Entry<Integer, HashMap<Integer, List<Float>>> set : firstGesture.entrySet()) { // foreach sensor
                // initialize fields for each sensor
                HashMap<Integer, List<List<Float>>> sensorSet = new HashMap<Integer, List<List<Float>>>();
                List<List<Float>> sumXAxis = new ArrayList<List<Float>>();
                List<List<Float>> sumYAxis = new ArrayList<List<Float>>();
                List<List<Float>> sumZAxis = new ArrayList<List<Float>>();

                xIter = set.getValue().get(SensorData.X_axis).iterator();
                yIter = set.getValue().get(SensorData.Y_axis).iterator();
                zIter = set.getValue().get(SensorData.Z_axis).iterator();
                while (xIter.hasNext()) {
                    sumXAxis.add(new ArrayList<Float>(Arrays.asList(xIter.next())));
                    sumYAxis.add(new ArrayList<Float>(Arrays.asList(yIter.next())));
                    sumZAxis.add(new ArrayList<Float>(Arrays.asList(zIter.next())));
                }
                sensorSet.put(SensorData.X_axis, sumXAxis);
                sensorSet.put(SensorData.Y_axis, sumYAxis);
                sensorSet.put(SensorData.Z_axis, sumZAxis);
                sumData.put(set.getKey(), sensorSet);
            }
            // do the rest rounds for all
            for (int i = 1; i < listSize; i++) { // for each gesture
                for (Map.Entry<Integer, HashMap<Integer, List<Float>>> set : gestureList.get(i).getData().entrySet()) { // foreach sensor
                    xIter = set.getValue().get(SensorData.X_axis).iterator();
                    yIter = set.getValue().get(SensorData.Y_axis).iterator();
                    zIter = set.getValue().get(SensorData.Z_axis).iterator();
                    int j = 0;
                    while (xIter.hasNext()) {
                        sumData.get(set.getKey()).get(SensorData.X_axis).get(j).add(xIter.next());
                        sumData.get(set.getKey()).get(SensorData.Y_axis).get(j).add(yIter.next());
                        sumData.get(set.getKey()).get(SensorData.Z_axis).get(j).add(zIter.next());
                        j++;
                    }

                }
            }
            // now count average of all gestures
            float xSum;
            float ySum;
            float zSum;
            SensorData sensorData = new SensorData();
            for (Map.Entry<Integer, HashMap<Integer, List<List<Float>>>> set :sumData.entrySet()) { // foreach sensor
                Iterator<List<Float>> sumXIter = set.getValue().get(SensorData.X_axis).iterator();
                Iterator<List<Float>> sumYIter = set.getValue().get(SensorData.Y_axis).iterator();
                Iterator<List<Float>> sumZIter = set.getValue().get(SensorData.Z_axis).iterator();
                while (sumXIter.hasNext()){
                    xSum = 0f;
                    ySum = 0f;
                    zSum = 0f;
                    for(Float value : sumXIter.next()){
                        xSum += value;
                    }
                    for(Float value : sumYIter.next()){
                        ySum += value;
                    }
                    for(Float value : sumZIter.next()){
                        zSum += value;
                    }
                    sensorData.addDataToSensor(set.getKey(), new float[]{xSum/listSize,
                            ySum/listSize, zSum/listSize});
                }
            }
            return sensorData;
        }
    }

    private void testAverageOfSequences(){
        List<SensorData> sensorList = new ArrayList<>();
        SensorData mySensorData = new SensorData();
        mySensorData.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{1,2,3});
        mySensorData.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{1,2,3});
        mySensorData.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{1,2,3});
        mySensorData.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{1,2,3});
        mySensorData.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{3,4,7});
        mySensorData.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{3,4,7});
        mySensorData.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{3,4,7});
        mySensorData.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{3,4,7});
        SensorData mySensorData1 = new SensorData();
        mySensorData1.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{-2,1,3});
        mySensorData1.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{-2,1,3});
        mySensorData1.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{-2,1,3});
        mySensorData1.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{-2,1,3});
        mySensorData1.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{1,4,8});
        mySensorData1.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{1,4,8});
        mySensorData1.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{1,4,8});
        mySensorData1.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{1,4,8});
        SensorData mySensorData2 = new SensorData();
        mySensorData2.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{5,3,1});
        mySensorData2.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{5,3,1});
        mySensorData2.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{5,3,1});
        mySensorData2.addDataToSensor(Sensor.TYPE_ACCELEROMETER,new float[]{5,3,1});
        mySensorData2.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{1,1,1});
        mySensorData2.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{1,1,1});
        mySensorData2.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{1,1,1});
        mySensorData2.addDataToSensor(Sensor.TYPE_GYROSCOPE,new float[]{1,1,1});

        sensorList.add(mySensorData);
        sensorList.add(mySensorData1);
        sensorList.add(mySensorData2);
        SensorData myResultData = returnAverageData(sensorList);
        if(myResultData!=null){
            sensorList.add(mySensorData);
        }else{
            sensorList.add(mySensorData);
        }
        sensorList.add(mySensorData);
    }
}
