package com.grdgyyr.controlio.RecognitionTools;

import android.content.Context;
import android.util.Log;

import org.encog.engine.network.activation.ActivationElliott;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.grdgyyr.controlio.SensorDataHandler.SensorData;

/**
 * Created by pepegeo on 2015-11-17.
 */
public class AlgNN extends Algorithms {

    private BasicNetwork network;
    List<Results> resultsList;
    HashMap<Integer,List<Float>> newGesture;
    List<HashMap<Integer,List<Float>>> gestureList;


    private int[] activFun;
    // !!!directly mapping to R.array.activation_function
    private ActivationFunction[] activFunArray = new ActivationFunction[]{
            null,
            new ActivationTANH(),
            new ActivationSigmoid(),
            new ActivationLinear(),
            new ActivationElliott()
    };
    private boolean[] useBias;
    private boolean[] useLayer;
    private int[] neuron;


    public AlgNN(){
        super();
        resultsList = new ArrayList<>();
        newGesture = null;
        gestureList = new ArrayList<>();
        initializeDefaultNeuralNetwork();
    }

    private void initializeDefaultNeuralNetwork(){
        activFun = new int[]{0,1,1,1,3};
        useBias = new boolean[]{false, true, true, true, true};
        neuron = new int[]{-1, 7, 7, 7, 1};
        useLayer = new boolean[]{true, true, true, false, true};
//        activFun1 = 0;  useBias1 = false;
//        activFun2 = 1;  useBias2 = true;   neuron2 = 7;    useLayer2 = true;
//        activFun3 = 1;  useBias3 = true;   neuron3 = 7;    useLayer3 = true;
//        activFun4 = 1;  useBias4 = true;   neuron4 = 7;    useLayer4 = false;
//        activFun5 = 3;  useBias5 = true;   neuron5 = 1;
    }

    public void setUseLayer(int index, boolean isUse){
        useLayer[index] = isUse;
    }

    public void setActivFun(int index, int functionNo){
        activFun[index] = functionNo;
    }

    public void setUseBias(int index, boolean isUse){
        useBias[index] = isUse;
    }

    public void setNeuron(int index, int number){
        neuron[index] = number;
    }

    public void setLayer(int index, boolean isUseLayer, int activFunNo, boolean isUseBias, int neuronNo){
        activFun[index] = activFunNo;
        useBias[index] = isUseBias;
        neuron[index] = neuronNo;
        useLayer[index] = isUseLayer;
//        switch (index){
//            case 1: //input
//                activFun1 = activFunNo;
//                useBias1 = isUseBias;
//                break;
//            case 5: //output
//                activFun5 = activFunNo;
//                useBias5 = isUseBias;
//                neuron5 = neuronNo;
//                break;
//            case 2: // first hidden layer
//                useLayer2 = isUseLayer;
//                activFun2 = activFunNo;
//                useBias2 = isUseBias;
//                neuron2 = neuronNo;
//                break;
//            case 3: // second hidden layer
//                useLayer3 = isUseLayer;
//                activFun3 = activFunNo;
//                useBias3 = isUseBias;
//                neuron3 = neuronNo;
//                break;
//            case 4: // third hidden layer
//                useLayer4 = isUseLayer;
//                activFun4 = activFunNo;
//                useBias4 = isUseBias;
//                neuron4 = neuronNo;
//                break;
//            default:
//                Log.e("Alg NN","Unhandled setLayer index in algorithm NN (NeuralNetwork)");
//                break;
//        }
    }

    public int[] getLayerInfo(int index){
        return new int[]{(useLayer[index] ? 1 : 0), activFun[index], (useBias[index] ? 1 : 0), neuron[index] };
//        switch (index){
//            case 1: //input
//                return new int[]{1, activFun1, ((useBias1) ? 1 : 0), 0};
//            case 5: //output
//                return new int[]{1, activFun5, ((useBias5) ? 1 : 0), neuron5};
//            case 2: // first hidden layer
//                return new int[]{((useLayer2) ? 1 : 0), activFun2, ((useBias2) ? 1 : 0), neuron2};
//            case 3: // second hidden layer
//                return new int[]{((useLayer3) ? 1 : 0), activFun3, ((useBias3) ? 1 : 0), neuron3};
//            case 4: // third hidden layer
//                return new int[]{((useLayer4) ? 1 : 0), activFun4, ((useBias4) ? 1 : 0), neuron4};
//            default:
//                Log.e("Alg NN","Unhandled setLayer index in algorithm NN (NeuralNetwork)");
//                return new int[]{};
//        }
    }

    @Override
    public void addTwoSequences(HashMap<Integer,List<Float>> base,
                                    HashMap<Integer,List<Float>> compare,
                                    int Sensor, String GestureName) {
        if(newGesture == null){
            newGesture = base;
        }
        gestureList.add(compare);
        resultsList.add(new Results(-1, Sensor, RecognitionManager.NN, GestureName));
    }

    @Override
    public void clearAlgorithm(){
        resultsList.clear();
        newGesture = null;
        gestureList.clear();
        network = null;
    }
    /*
    * REQUIRE that each gesture sequence has the same length
    * */
    @Override
    public List<Results> getResults() {
        if(true)
            return null;

        int listSize = gestureList.size();
        int sequenceSize = gestureList.get(0).get(0).size();
        int dataSetSize = listSize *3;


        double[][] dataSet = new double[listSize][sequenceSize];
        double[][] idealSet = new double[listSize][1];

//        for(HashMap<Integer, List<Float>> gesture: gestureList){ // foreach gesture
//            for(Map.Entry<Integer, List<Float>> axis: gesture.entrySet()){ // foreach axis
//                Iterator<Float> iter = axis.getValue().iterator();
//                int sequenceNo = gestureNo + axis.getKey();
//                for(int i=0; i<sequenceSize; i++){ // foreach value
//                    dataSet[sequenceNo][i] = iter.next();
//                }
//                idealSet[sequenceNo][0] = (double) gestureNo;
//            }
//            gestureNo++;
//        }
        int gestureNo = 0;
        for(HashMap<Integer, List<Float>> gesture: gestureList){ // foreach gesture
            Iterator<Float> xIter = gesture.get(0).iterator();
            Iterator<Float> yIter = gesture.get(1).iterator();
            Iterator<Float> zIter = gesture.get(2).iterator();
            for(int i=0; i<sequenceSize; i++){ // foreach value
                dataSet[gestureNo][i] = Math.sqrt(Math.pow(xIter.next(), 2) +
                        Math.pow(yIter.next(), 2) + Math.pow(zIter.next(),2));
            }
            idealSet[gestureNo][0] = (double) gestureNo;
            gestureNo++;
        }
        NeuralDataSet trainingSet = new BasicNeuralDataSet(dataSet, idealSet);

        network = new BasicNetwork();
        network.addLayer(new BasicLayer(activFunArray[activFun[0]],useBias[0],sequenceSize)); // input
        if (useLayer[1])
            network.addLayer(new BasicLayer(activFunArray[activFun[1]],useBias[1],neuron[1])); // first hidden layer
        if (useLayer[2])
            network.addLayer(new BasicLayer(activFunArray[activFun[2]],useBias[2],neuron[2])); // second hidden layer
        if (useLayer[3])
            network.addLayer(new BasicLayer(activFunArray[activFun[3]],useBias[3],neuron[3])); // third hidden layer
        network.addLayer(new BasicLayer(activFunArray[activFun[4]],useBias[4],neuron[4])); // output
        network.getStructure().finalizeStructure();
        network.reset();

        final Propagation train = new ResilientPropagation(network, trainingSet);
        int j = 0;
        do {
            train.iteration();
            j++;
            if((j%10000) == 1) {
                Log.i("alg NN", "Training error: " + train.getError() + " after " + j + " rounds");
            }
        } while (train.getError() > 0.1);
        train.finishTraining();
        Log.i("alg NN", "Training error: " + train.getError() + " after " + j + " rounds");

        double[][] newGestureSet = new double[1][sequenceSize];
        Iterator<Float> xIter = newGesture.get(0).iterator();
        Iterator<Float> yIter = newGesture.get(1).iterator();
        Iterator<Float> zIter = newGesture.get(2).iterator();
        for(int i=0; i<sequenceSize; i++){
            newGestureSet[0][i] = Math.sqrt(Math.pow(xIter.next(), 2) +
                    Math.pow(yIter.next(), 2) + Math.pow(zIter.next(), 2));
        }

//        double[][] newGestureSet = new double[3][sequenceSize];
//        int m = 0;
//        for(Map.Entry<Integer,List<Float>> axis: newGesture.entrySet()){
//            Iterator<Float> iter = axis.getValue().iterator();
//            for(int i=0; i<sequenceSize; i++){
//                newGestureSet[m][i] = iter.next();
//            }
//            m++;
//        }

        double error = 0.0;
        MLData input = new BasicMLData(newGestureSet[0]);
        MLData output = network.compute(input);
        error += Math.abs(output.getData(0));

        Log.i("alg NN","Final error is: "+error);
//        float fError = (float) (error/3.0);
        float fError = (float) (error);

        for(int k=0; k<listSize; k++){
            resultsList.get(k).setFitRate((float)k-fError);
        }
        return resultsList;
    }

    @Override
    public float compareTwoSequences(List<Float> base, List<Float> compare) {
        return -1;
    }

    public float compareTwoSequences(HashMap<Integer,List<Float>> base,
                                     HashMap<Integer,List<Float>> compare,
                                     String compareName, Context context){
        return compareTwoSequences(base,compare);
    }



    public float compareTwoSequences(HashMap<Integer,List<Float>> base,
                                              HashMap<Integer,List<Float>> compare){
        Log.i("NN alg", "comparing two Sequences");

        List<Float> baseX = base.get(SensorData.X_axis);
        List<Float> baseY = base.get(SensorData.Y_axis);
        List<Float> baseZ = base.get(SensorData.Z_axis);
        List<Float> compareX = compare.get(SensorData.X_axis);
        List<Float> compareY = compare.get(SensorData.Y_axis);
        List<Float> compareZ = compare.get(SensorData.Z_axis);

        int baseSize = baseX.size();
        int compSize = compareX.size();
        int minSize = Math.min(baseSize, compSize);

        double[][] ideal = new double[][]{
                {0.0},
                {0.0},
                {0.0},
                {0.0},
                {0.0},
                {0.0}
        };
        double[][] idealBase = new double[][]{
                {2.0},
                {2.0},
                {2.0}
        };
        double[][] idealCompare = new double[][]{
                {0.0},
                {0.0},
                {0.0}
        };

        double[][] dataSet = new double[6][minSize];
        double[][] baseSet = new double[3][minSize];
        double[][] compareSet = new double[3][minSize];

        Iterator<Float> xIterBase = baseX.iterator();
        Iterator<Float> yIterBase = baseY.iterator();
        Iterator<Float> zIterBase = baseZ.iterator();
        Iterator<Float> xIterComp = compareX.iterator();
        Iterator<Float> yIterComp = compareY.iterator();
        Iterator<Float> zIterComp = compareZ.iterator();
        for(int i = 0; i < minSize; i++){
            baseSet[0][i] = dataSet[0][i] = xIterBase.next();
            baseSet[1][i] = dataSet[1][i] = yIterBase.next();
            baseSet[2][i] = dataSet[2][i] = zIterBase.next();
            compareSet[0][i] = dataSet[3][i] = xIterComp.next();
            compareSet[0][i] = dataSet[4][i] = yIterComp.next();
            compareSet[0][i] = dataSet[5][i] = zIterComp.next();
        }


        NeuralDataSet trainingSet = new BasicNeuralDataSet(baseSet, idealBase);

        network = new BasicNetwork();
        network.addLayer(new BasicLayer(activFunArray[activFun[0]],useBias[0],baseSize)); // input
        if (useLayer[1])
            network.addLayer(new BasicLayer(activFunArray[activFun[1]],useBias[1],neuron[1])); // first hidden layer
        if (useLayer[2])
            network.addLayer(new BasicLayer(activFunArray[activFun[2]],useBias[2],neuron[2])); // second hidden layer
        if (useLayer[3])
            network.addLayer(new BasicLayer(activFunArray[activFun[3]],useBias[3],neuron[3])); // third hidden layer
        network.addLayer(new BasicLayer(activFunArray[activFun[4]],useBias[4],neuron[4])); // output
//            network.addLayer(new BasicLayer(null, false, baseSize));
//            network.addLayer(new BasicLayer(new ActivationTANH(), true, 7));
//            network.addLayer(new BasicLayer(new ActivationTANH(), true, 7));
//            network.addLayer(new BasicLayer(new ActivationLinear(), false, 1));
        network.getStructure().finalizeStructure();
        network.reset();

        final Propagation train = new ResilientPropagation(network, trainingSet);
        int j = 0;
        do {
            train.iteration();
            j++;
        } while (train.getError() > 0.01);

        Log.i("alg NN", "Training error: " + train.getError() + " after " + j + " rounds");
        train.finishTraining();



        int i=0;
        double error = 0.0;
        while(i<3){
            MLData input = new BasicMLData(compareSet[i]);
            MLData output = network.compute(input);
            if(i<3){
                error += Math.abs(output.getData(0));
            }
            Log.i("alg NN","Classification for i:"+i+" "+output.getData(0)+ " ideal "+ideal[i][0]);
            i++;
        }

        error = error/3.0*100.0;
        Log.i("alg NN","Final error is: "+error);
        return (float)(error);

    }

}
