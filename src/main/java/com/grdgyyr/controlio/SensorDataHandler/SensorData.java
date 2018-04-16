package com.grdgyyr.controlio.SensorDataHandler;

import android.content.Context;
import android.hardware.Sensor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.grdgyyr.controlio.Fragments.ActivityGestureRecognizer;
import com.grdgyyr.controlio.RecognitionTools.Filters;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;
import com.grdgyyr.controlio.Utilities.Commands;
import com.grdgyyr.controlio.Utilities.Connection;

/**
 * Created by pepe on 2015-06-01.
 */
public class SensorData {

    /**
     * Sensor types
     * Sensor.TYPE_ACCELEROMETER
     * Sensor.TYPE_GYROSCOPE
    * */
    private HashMap<Integer, HashMap<Integer,List<Float>>> sensorsData;
    // prefix is used to save files on android device with common name
    private static final String PREFIX = "gesture_";
    public static final String FOLDER_NAME = "/Gesture_Recognition";
    public static final String TOEXCEL_FOLDER = FOLDER_NAME + "/aToExcel";
    public static final String COMPARE_FOLDER = FOLDER_NAME + "/aComparision";
    public static final Integer X_axis = 0;
    public static final Integer Y_axis = 1;
    public static final Integer Z_axis = 2;

    public static int[] sensorTypes = new int[] {Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE};

    // Konstruktor który implementuje puste instancje wszystkich podrzednych obiektow sensorsData
    // @param sensorTypes - list of the sensors that will be acquired
    // HashMap{SensorType, HashMap{Axis, List{data}}}
    public SensorData(int[] sensorTypes){
        this.sensorTypes = sensorTypes;
        sensorsData = new HashMap<Integer, HashMap<Integer,List<Float>>>();
        for(int type: sensorTypes) {
            addSensor(type);
        }
    }
    public SensorData(SensorData copy){
        List<Float> aX = new ArrayList<>(copy.getData().get(Sensor.TYPE_ACCELEROMETER).get(X_axis));
        List<Float> aY = new ArrayList<>(copy.getData().get(Sensor.TYPE_ACCELEROMETER).get(Y_axis));
        List<Float> aZ = new ArrayList<>(copy.getData().get(Sensor.TYPE_ACCELEROMETER).get(Z_axis));
        List<Float> gX = new ArrayList<>(copy.getData().get(Sensor.TYPE_GYROSCOPE).get(X_axis));
        List<Float> gY = new ArrayList<>(copy.getData().get(Sensor.TYPE_GYROSCOPE).get(Y_axis));
        List<Float> gZ = new ArrayList<>(copy.getData().get(Sensor.TYPE_GYROSCOPE).get(Z_axis));
        sensorsData = new HashMap<>();

        HashMap<Integer,List<Float>> sensorMap = new HashMap<>();
        sensorMap.put(X_axis,aX);
        sensorMap.put(Y_axis,aY);
        sensorMap.put(Z_axis,aZ);
        sensorsData.put(Sensor.TYPE_ACCELEROMETER,sensorMap);

        HashMap<Integer,List<Float>> sensorMap1 = new HashMap<>();
        sensorMap1.put(X_axis,gX);
        sensorMap1.put(Y_axis,gY);
        sensorMap1.put(Z_axis,gZ);
        sensorsData.put(Sensor.TYPE_GYROSCOPE,sensorMap1);
    }
    // wywolanie pustego konstruktora, powoduje wywylanie tego glownego z jednym parametrem
    public SensorData(){
        this(sensorTypes);
    }

    public void addSensor(int newSensor){
        HashMap<Integer, List<Float>> sensorSet = new HashMap<Integer, List<Float>>();
        sensorSet.put(X_axis, new ArrayList<Float>());
        sensorSet.put(Y_axis, new ArrayList<Float>());
        sensorSet.put(Z_axis, new ArrayList<Float>());
        sensorsData.put(newSensor, sensorSet);
    }

    public void addDataToSensor(int type, float[] newData){
        sensorsData.get(type).get(X_axis).add(newData[0]);
        sensorsData.get(type).get(Y_axis).add(newData[1]);
        sensorsData.get(type).get(Z_axis).add(newData[2]);
    }

    public HashMap<Integer, HashMap<Integer,List<Float>>> getData(){
        return this.sensorsData;
    }
    public void setData(HashMap<Integer, HashMap<Integer,List<Float>>> data){
        this.sensorsData = data;
    }
    public HashMap<Integer,List<Float>> getDataOfSensor(int type){
        if(sensorsData.containsKey(type))
            return sensorsData.get(type);
        else {
            Log.e("WRONG SENSOR", "in getDataOfSensor there is no sensor of type:"+type);
            return null;
        }
    }
    public List<Float> getEuclidenDataOfSensor(int type){
        HashMap<Integer,List<Float>> allAxis = sensorsData.get(type);
        List<Float> xAxis = allAxis.get(X_axis);
        List<Float> yAxis = allAxis.get(Y_axis);
        List<Float> zAxis = allAxis.get(Z_axis);
        Iterator<Float> xIter = xAxis.iterator();
        Iterator<Float> yIter = yAxis.iterator();
        Iterator<Float> zIter = zAxis.iterator();
        List<Float> euclidean = new ArrayList<>();

        while(xIter.hasNext()){
            euclidean.add(returnEuclidean(xIter.next(),yIter.next(),zIter.next()));
        }

        return euclidean;
    }
    private float returnEuclidean(float a, float b, float c){
        return (float) Math.sqrt(a*a+b*b+c*c);
    }

    public Object getData(int type, String setName, int position){
        return sensorsData.get(type).get(setName).get(position);
    }

    public void clearData(){
        for(Map.Entry<Integer, HashMap<Integer,List<Float>>> set : sensorsData.entrySet()){
            set.getValue().get(X_axis).clear();
            set.getValue().get(Y_axis).clear();
            set.getValue().get(Z_axis).clear();
        }
    }

    public int[] sizeOfData(){
        int [] size = new int[sensorsData.size()];
        int i = 0;
        for(Map.Entry<Integer, HashMap<Integer,List<Float>>> set : sensorsData.entrySet()){
            size[i] = set.getValue().get(X_axis).size();
            i++;
        }
        return size;
    }

    public HashMap<Integer, HashMap<Integer,List<Float>>> returnHashMap(){
        return sensorsData;
    }

    //so save to memory files that has after filtering - smooth data
    public void transformToAverageSequence(){
        Filters filter = RecognitionManager.getInstance().getFilter();
        for(Map.Entry<Integer, HashMap<Integer,List<Float>>> set : sensorsData.entrySet()) {
            for(Map.Entry<Integer,List<Float>> axis: set.getValue().entrySet()){
                axis.setValue(filter.averageSamples(axis.getValue()));
            }
        }

    }

    public String toString(){
        StringBuilder strBuilder = new StringBuilder();
        // foreach SENSOR
        for(Map.Entry<Integer, HashMap<Integer,List<Float>>> set : sensorsData.entrySet()){
            switch (set.getKey()){
                case Sensor.TYPE_ACCELEROMETER:
                    strBuilder.append("\n Sensor.TYPE_ACCELEROMETER \n");
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    strBuilder.append("\n Sensor.TYPE_GYROSCOPE \n");
                    break;
                default:
                    strBuilder.append("\n Sensor.UNDEFINED \n");
                    break;
            }
            // foreach AXIS
            Iterator xIter = set.getValue().get(X_axis).iterator();
            Iterator yIter = set.getValue().get(Y_axis).iterator();
            Iterator zIter = set.getValue().get(Z_axis).iterator();
            while(xIter.hasNext()){
                strBuilder.append(xIter.next().toString() + "\t");
                strBuilder.append(yIter.next().toString() + "\t");
                strBuilder.append(zIter.next().toString() + "\n");
            }
            strBuilder.append("\n");
        }
        return strBuilder.toString();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    // enable to open only by this application
    public boolean saveFileToExternalStorage(String filename, Context context) {
        Log.d("SAVE FILE Writable:", String.valueOf(isExternalStorageWritable()));
        if( isExternalStorageWritable()) {
            String path = Environment.getExternalStorageDirectory().toString() + FOLDER_NAME;
            File myFile = new File(path);
            // Make sure that directory exist
            myFile.mkdir();
            File file = new File(myFile, PREFIX + filename);


            try {
                if (file.exists()) file.delete();
                //FileOutputStream outputStream = context.openFileOutput(prefix + filename, Context.MODE_PRIVATE);
                FileOutputStream outputStream = new FileOutputStream(file);
                ObjectOutputStream outputObject = new ObjectOutputStream(outputStream);
                outputObject.writeObject(sensorsData);
                outputObject.close();

                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(context,
                        new String[]{file.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
                Connection.Send("GESTUREARRAY"+Commands.getArrayGestures());

                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                Log.w("ExternalStorage ", "Error writing " + myFile, e);
                return false;
            }
        }
        return false;
    }

    // enable to open in excel
    public boolean saveReadableFileToExternalStorage(String filename, Context context) {
        if( isExternalStorageWritable()) {
            // make dataset smooth
            transformToAverageSequence();

            String path = Environment.getExternalStorageDirectory().toString() + TOEXCEL_FOLDER;
            File myFile = new File(path);
            // Make sure that directory exist
            myFile.mkdir();
            File file = new File(myFile, PREFIX + filename);
            try {
                if (file.exists()) file.delete();
                FileOutputStream outputStream = new FileOutputStream(file);
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(this.toString());
                printStream.flush();
                printStream.close();

                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(context,
                        new String[]{file.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });

                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                Log.w("ExternalStorage ", "Error writing " + myFile, e);
                return false;
            }
        }
        return false;
    }

    public boolean getFileFromExternalStorage(String filename){
        Log.d("GET FILE Readable:", String.valueOf(isExternalStorageReadable()));
        if(isExternalStorageReadable()) {
            String path = Environment.getExternalStorageDirectory().toString() + FOLDER_NAME;
            File file = new File(path, PREFIX + filename);
            try {
                //FileInputStream inputStream = context.openFileInput(prefix + filename);
                FileInputStream inputStream = new FileInputStream(file);
                ObjectInputStream inputObject = new ObjectInputStream(inputStream);
                sensorsData = (HashMap<Integer, HashMap<Integer, List<Float>>>) inputObject.readObject();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.w("ExternalStorage", "Error reading "+file,e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void deleteFileFromExternalStorage(String filename){
        // getAbsolutePath() or toString ... given string is the same
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + FOLDER_NAME;
        String pathExcel = Environment.getExternalStorageDirectory().getAbsolutePath() + TOEXCEL_FOLDER;
        File file = new File(path, PREFIX + filename);
        File fileExcel = new File(pathExcel, PREFIX + filename);
        try{
            file.delete();
            Log.d("DELETE FILE ", path);
        }catch(Exception e){
            Log.d("External Storage ", " Error deleting "+file,e);
        }
        try{
            fileExcel.delete();
            Log.d("DELETE FILE ", pathExcel);
        }catch(Exception e){
            Log.d("External Storage ", " Error deleting excel file "+fileExcel,e);
        }
        Connection.Send("GESTUREARRAY"+ Commands.getArrayGestures());

    }

    public String[] getListOfFiles(){
        String path = Environment.getExternalStorageDirectory().toString() + FOLDER_NAME;
        File files = new File(path);
        boolean success = true;
        if(!files.exists()) {
            success = files.mkdir();
        }
        if(success){
            File[] fileList = files.listFiles();
            int length = fileList.length;
            String[] fileNames = new String[length];
            String temp;
            int i = 0;
            for (File file : fileList) {
                temp = file.getName();
                if (temp.contains(PREFIX)) {
                    fileNames[i] = temp.substring(PREFIX.length());
                    i++;
                } else {
                    length--;
                }
            }
            if (length != fileList.length) {
                // match String[] size to actual representation of data
                String[] shorterList = new String[length];
                for (i = 0; i < length; i++) {
                    shorterList[i] = fileNames[i];
                }
                return shorterList;
            } else {
                return fileNames;
            }
        }else {
            return new String[]{};
        }
    }


}
