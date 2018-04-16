package com.grdgyyr.controlio.RecognitionTools;

import java.util.Comparator;

/**
 * Created by pepegeo on 2015-11-22.
 */
public class Results implements Comparable<Results> {
    private float fitRate;
    private int sensorType;
    private int algorithmType;
    private String nameOfGesture;

    public Results(float fitRate, int sensorType, int algorithmType, String nameOfGesture){
        this.fitRate = fitRate;
        this.sensorType = sensorType;
        this.algorithmType = algorithmType;
        this.nameOfGesture = nameOfGesture;
    }

    public void setFitRate(float fitRate){this.fitRate = fitRate;}
    public Results getResults(){
        return this;
    }
    public float getFitRate() {
        return fitRate;
    }

    public int getSensorType() {
        return sensorType;
    }

    public int getAlgorithmType(){
        return algorithmType;
    }

    public String getNameOfGesture() {
        return nameOfGesture;
    }

    @Override
    public int compareTo(Results another) {
        return Comparators.FITRATE.compare(this, another);
    }

    public static class Comparators{
        public static Comparator<Results> FITRATE = new Comparator<Results>() {
            @Override
            public int compare(Results lhs, Results rhs) {
                return Float.compare(Math.abs(lhs.getFitRate()), Math.abs(rhs.getFitRate()));
            }
        };
    }
}
