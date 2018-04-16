package com.grdgyyr.controlio.RecognitionTools;

import android.content.Context;
import android.util.Log;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.svm.SVM;
import org.encog.ml.svm.training.SVMSearchTrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by pepe on 19.01.2016.
 */
public class AlgSVM extends Algorithms {

    List<Results> resultsList;
    HashMap<Integer,List<Float>> newGesture;
    List<HashMap<Integer,List<Float>>> gestureList;


    public AlgSVM(){
        super();
        resultsList = new ArrayList<>();
        newGesture = null;
        gestureList = new ArrayList<>();
    }

    @Override
    public void clearAlgorithm(){
        resultsList.clear();
        newGesture = null;
        gestureList.clear();
    }

    @Override
    public void addTwoSequences(HashMap<Integer,List<Float>> base,
                                HashMap<Integer,List<Float>> compare,
                                int Sensor, String GestureName) {
        if(newGesture == null){
            newGesture = base;
        }
        gestureList.add(compare);
        resultsList.add(new Results(-1, Sensor, RecognitionManager.SVM, GestureName));
    }

    private final double MAX_ACC_RANGE = 16.0;

    private double normalize(double value){ // normalize to values (0,1)
        return Math.abs(value/MAX_ACC_RANGE);
    }

    @Override
    public List<Results> getResults() {
        if(true)
            return null;
        int listSize = gestureList.size();
        int sequenceSize = gestureList.get(0).get(0).size();
        int dataSetSize = listSize *3;


        double[][] dataSet = new double[dataSetSize][sequenceSize];
        double[][] idealSet = new double[dataSetSize][1];
        int gestureNo = 0;
        for(HashMap<Integer, List<Float>> gesture: gestureList){ // foreach gesture
            for(Map.Entry<Integer, List<Float>> axis: gesture.entrySet()){ // foreach axis
                Iterator<Float> iter = axis.getValue().iterator();
                int sequenceNo = gestureNo + axis.getKey();
                for(int i=0; i<sequenceSize; i++){ // foreach value
                    dataSet[sequenceNo][i] = normalize(iter.next());
                }
                idealSet[sequenceNo][0] = (double) gestureNo;
            }
            gestureNo++;
        }
//        double[][] dataSet = new double[listSize][sequenceSize];
//        double[][] idealSet = new double[listSize][1];
//        int gestureNo = 0;
//        for(HashMap<Integer, List<Float>> gesture: gestureList){ // foreach gesture
//            Iterator<Float> xIter = gesture.get(0).iterator();
//            Iterator<Float> yIter = gesture.get(1).iterator();
//            Iterator<Float> zIter = gesture.get(2).iterator();
//            for(int i=0; i<sequenceSize; i++){ // foreach value
//                dataSet[gestureNo][i] = normalize(Math.sqrt(Math.pow(xIter.next(), 2) +
//                        Math.pow(yIter.next(), 2) + Math.pow(zIter.next(),2)) );
//            }
//            idealSet[gestureNo][0] = (double) gestureNo;
//            gestureNo++;
//        }
        BasicMLDataSet trainingSet = new BasicMLDataSet(dataSet,idealSet);

        SVM svm = new SVM(sequenceSize,false); // listSize for input, false for classification
        SVMSearchTrain train = new SVMSearchTrain(svm, trainingSet);

        int j = 0;
        boolean dontStop = true;
        do {
            train.iteration();
            j++;
            if((j%10000) == 1) {
                Log.i("alg SVM", "Training error: " + train.getError() + " after " + j + " rounds");
            }
            if(j>100000) {
                dontStop = false;
            }
        } while (train.getError() > 0.01 && dontStop);
        train.finishTraining();
        Log.i("alg SVM", "Training error: " + train.getError() + " after " + j + " rounds");

//        double[][] newGestureSet = new double[1][sequenceSize];
//        Iterator<Float> xIter = newGesture.get(0).iterator();
//        Iterator<Float> yIter = newGesture.get(1).iterator();
//        Iterator<Float> zIter = newGesture.get(2).iterator();
//        for(int i=0; i<sequenceSize; i++){
//            newGestureSet[0][i] = normalize( Math.sqrt(Math.pow(xIter.next(), 2) +
//                    Math.pow(yIter.next(), 2) + Math.pow(zIter.next(), 2)) );
//        }

        double[][] newGestureSet = new double[3][sequenceSize];
        int m = 0;
        for(Map.Entry<Integer,List<Float>> axis: newGesture.entrySet()){
            Iterator<Float> iter = axis.getValue().iterator();
            for(int i=0; i<sequenceSize; i++){
                newGestureSet[m][i] = normalize(iter.next());
            }
            m++;
        }

//        double error = 0.0;
//        MLData input = new BasicMLData(newGestureSet[0]);
//        MLData output = svm.compute(input);
//        error += Math.abs(output.getData(0));

        double error = 0.0;
        for(int n=0; n<3; n++) {
            MLData input = new BasicMLData(newGestureSet[n]);
            MLData output = svm.compute(input);
            error += Math.abs(output.getData(0));
        }

        Log.i("alg NN","Final error is: "+error);
        float fError = (float) (error/3.0);
//        float fError = (float) (error);

        for(int k=0; k<listSize; k++){
            resultsList.get(k).setFitRate((float)k-fError);
        }

        return resultsList;
    }

    @Override
    public float compareTwoSequences(HashMap<Integer, List<Float>> base, HashMap<Integer, List<Float>> compare) {
        return 0;
    }

    @Override
    public float compareTwoSequences(HashMap<Integer, List<Float>> base, HashMap<Integer, List<Float>> compare, String compareName, Context context) {
        return 0;
    }

}
 //   public static double normalize(final int value) {
//        return ((value - INPUT_LOW)
//                / (INPUT_HIGH - INPUT_LOW))
//                * (OUTPUT_HIGH - OUTPUT_LOW) + OUTPUT_LOW;
//    }
//
//    /**
//     * De-normalize the specified value.
//     * @param value The value to denormalize.
//     * @return The denormalized value.
//     */
//    public static double deNormalize(final double data) {
//        double result = ((INPUT_LOW - INPUT_HIGH) * data - OUTPUT_HIGH
//                * INPUT_LOW + INPUT_HIGH * OUTPUT_LOW)
//                / (OUTPUT_LOW - OUTPUT_HIGH);
//        return result;
//    }