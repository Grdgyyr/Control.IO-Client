package com.grdgyyr.controlio.RecognitionTools;

import android.content.Context;
import android.hardware.Sensor;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.grdgyyr.controlio.SensorDataHandler.SensorData;

/**
 * Created by pepegeo on 2015-11-11.
 */
public class RecognitionManager {
    // difference of "static final" vs "final static"
    // http://stackoverflow.com/a/34252094/2163045
    public static final int gestureDurationMAX = 3000; // max duration of performacing gesture
    public static final int gestureDurationMIN = 1000; // min duration of performacing gesture
    public static final int MIN_ACC_LENGTH = 100;

    public static final int DTW = 0;
    public static final int NN = 1;
    public static final int SVM = 2;

    public static final int REPEAT_ONCE = 0;
    public static final int REPEAT_TWICE = 1;
    public static final int REPEAT_THRICE = 2;

    private static RecognitionManager recognitionManager = null;
    private Context context;

    // Manage data
    private SensorData rawDataToRecognize;
    private HashMap<String,SensorData> rawListOfGestures;

    // Manage filters, and algorithms
    private boolean usePreviousGestureInIdentifying = true; // cheat program and do not accept new gestures, instead use previous one - for testing purpose
    private int repeatGesture; // number of repetition before gesture will be saved
    private List<Integer> useSensors;
    private Filters filters;
    private AlgDTW algDTW;
    private AlgNN algNN;
    private AlgSVM algSVM;
    private HashMap<Integer, Algorithms> algorithms;

    public static RecognitionManager getInstance(){
        if(recognitionManager == null)
            recognitionManager = new RecognitionManager();
        return recognitionManager;
    }
    private RecognitionManager(){
        repeatGesture = REPEAT_ONCE;
        filters = new Filters();
        setUtilities(true,MIN_ACC_LENGTH);
        algDTW = new AlgDTW();
        algNN = new AlgNN();
        algSVM = new AlgSVM();
        algorithms = new HashMap<Integer, Algorithms>();
        useSensors = new ArrayList<Integer>();
        rawDataToRecognize = null;
        rawListOfGestures = new HashMap<>();
        addInitialValues();
    }

    //put default data into class, it is important to do it.
    private void addInitialValues(){
        algorithms.put(DTW, algDTW);
//        algorithms.put(NN, algNN);
//        algorithms.put(SVM, algSVM);
        useSensors.add(Sensor.TYPE_ACCELEROMETER);
    }

    public void setUtilities(boolean doReduce, int reduceTo){
        if(reduceTo>=MIN_ACC_LENGTH)
            Utilities.getInstance().setReduceDataSizeTo(reduceTo);
        Utilities.getInstance().setUseReduceDataSize(doReduce);
    }

    public void setUsePreviousGesture(boolean usePrevious){
        usePreviousGestureInIdentifying = usePrevious;
    }
    public boolean isUsePreviousGesture(){
        return this.usePreviousGestureInIdentifying;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void setDataToRecognize(SensorData data){
        if(rawDataToRecognize == null) {
            this.rawDataToRecognize = new SensorData(data);
        }else if(!usePreviousGestureInIdentifying){
            this.rawDataToRecognize = new SensorData(data);
        }
    }
    //public void clearDataToRecognize(){this.rawDataToRecognize = null;}

    public int getRepeatGesture() {
        return repeatGesture;
    }

    public void setRepeatGesture(int repeatGesture) {
        this.repeatGesture = repeatGesture;
    }

    public Filters getFilter(){
        return filters;
    }

    public HashMap<Integer, Algorithms> getAlgorithms(){return algorithms;}

    public AlgDTW getDWTAlgorithm(){
        return algDTW;
    }

    public AlgNN getNNAlgorithm(){return algNN;}

    public AlgSVM getSVMAlgorithm(){return algSVM;}

    public List<Integer> getSensors(){
        return useSensors;
    }
    /*
    * @param isAdd when true try to add alg, when false try to remove it
    * @param algType precise which algorithm should be added/removed
    * */
    public boolean setAlgorithm(boolean isAdd, Integer algType){
        if(isAdd){
            if(!algorithms.containsKey(algType)){
                switch (algType) {
                    case DTW:
                        algorithms.put(DTW,algDTW);
                        break;
                    case NN:
                        algorithms.put(NN, algNN);
                        break;
                    case SVM:
                        algorithms.put(SVM, algSVM);
                }
            }
            return true;
        }else {
            if(algorithms.containsKey(algType))
                if (algorithms.size() > 1) {
                    algorithms.remove(algType);
                    return true;
                }
            return false;
        }
    }
    /*
    * @param sensorTypes one of Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE
    * */
    public boolean setSensors(boolean isAdd, int sensorType){
        if(isAdd){
            if(!useSensors.contains(sensorType))
                useSensors.add(sensorType);
            return true;
        }else{
            if(useSensors.contains(sensorType))
                if(useSensors.size()>1) {
                    useSensors.remove(sensorType);
                    return true;
                }
            return false;
        }
    }

    /*
    * dataToRecognize - gesture that we try to identify
    * dataToComapre - one from the saved gestures
    * */
    public List<Results> recognize() {
        List<Results> lol = new ArrayList<>();
        lol.addAll(dolol());
       // lol.addAll(dolol());
        return lol;
    }
    public List<Results> dolol(){
        List<Results> resultList = new ArrayList<>();
        if(rawDataToRecognize != null) {
            //get and save All gestures
            String[] gestureList = rawDataToRecognize.getListOfFiles();
            for (String gestureName : gestureList) {
                SensorData gesture = new SensorData();
                if(gesture.getFileFromExternalStorage(gestureName));
                rawListOfGestures.put(gestureName, gesture);
            }
            for(Integer sensor: useSensors){
                Log.i("Manager.recognize()", "for SENSOR: " + sensor);
                HashMap<Integer,List<Float>> dataToRecognize = new HashMap<>();
                dataToRecognize.putAll(rawDataToRecognize.getDataOfSensor(sensor));
                // Sequence to Recognize
                dataToRecognize = Utilities.getInstance().reduceDataSizeTo(dataToRecognize);
                dataToRecognize = filters.filter(dataToRecognize);

                for(Map.Entry<String,SensorData> rawGesture : rawListOfGestures.entrySet()){
                    Log.i("Manager.recognize()", "for GESTURE: "+ rawGesture.getKey());
                    HashMap<Integer,List<Float>> dataToComapre = rawGesture.getValue().
                            getDataOfSensor(sensor);
                    // Sequence to Compare
                    dataToComapre = Utilities.getInstance().reduceDataSizeTo(dataToComapre);
                    dataToComapre = filters.filter(dataToComapre);

                    for(Map.Entry<Integer, Algorithms> algorithm: algorithms.entrySet()){
                        Log.i("Manager.recognize()", "for ALGORITHM: "+ algorithm.getKey());
                        if(algorithm.getKey() == DTW || algorithm.getKey() == NN){
                            float result = algorithm.getValue().compareTwoSequences(
                                    dataToRecognize,dataToComapre,rawGesture.getKey(),context);
                            resultList.add(new Results(result, sensor, algorithm.getKey(),
                                    rawGesture.getKey()));
                        }else if(algorithm.getKey() == SVM) {
                            algorithm.getValue().addTwoSequences(dataToComapre,dataToComapre,
                                    sensor, rawGesture.getKey());
                        }
                    }
                }
            }
            //rawDataToRecognize = null;
            rawListOfGestures.clear();
            for(Map.Entry<Integer, Algorithms> algorithm: algorithms.entrySet()){
                List<Results> newResultList = algorithm.getValue().getResults();
                if (newResultList != null) {
                    resultList.addAll(newResultList);
                }
                algorithm.getValue().clearAlgorithm();
            }
            return resultList;
        }else {
            Log.e("RecognitionManager", "before using recognice() need to set the gesture to identify"
                    + " by setting it in setDataToRecognize(gesture)");
            //rawDataToRecognize = null;
            rawListOfGestures.clear();
            return new ArrayList<>();
        }
    }
}
