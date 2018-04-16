package com.grdgyyr.controlio.RecognitionTools;

import android.content.Context;

import java.util.HashMap;
import java.util.List;

/**
 * Created by pepegeo on 2015-11-11.
 */
public abstract class Algorithms {

    public Algorithms(){
    }

    public abstract void clearAlgorithm();
    public abstract List<Results> getResults();
    public float compareTwoSequences(List<Float> base, List<Float> compare){ return 0;}
    public abstract float compareTwoSequences(HashMap<Integer,List<Float>> base,
                                              HashMap<Integer,List<Float>> compare);
    public abstract float compareTwoSequences(HashMap<Integer,List<Float>> base,
                                              HashMap<Integer,List<Float>> compare,
                                              String compareName, Context context);
    public void addTwoSequences(HashMap<Integer,List<Float>> base,
                                    HashMap<Integer,List<Float>> compare,
                                    int Sensor, String GestureName){}


}
