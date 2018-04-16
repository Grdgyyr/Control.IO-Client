package com.grdgyyr.controlio.RecognitionTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by pepegeo on 2015-11-09.
 */
public class Filters {
    private boolean useRemoveFirst; // remove of first samples
    private boolean usePassFilter;
    private boolean useAverageSamples;
    private boolean useSectionedSequence;

    private int filterType;
    private int averageOfNeighbours;
    private int sectionsNumber;
    // filters
    public static final int LOW_PASS_FILTER = 0;
    public static final int HIGH_PASS_FILTER = 1;
    public static final int KALMAHAN_FILTER = 2;
    // average samples
    public static final int ONE_NEIGHBOUR = 0;
    public static final int TWO_NEIGHBOURS = 1;
    public static final int THREE_NEIGHBOURS = 2;
    public static final int FOUR_NEIGHBOURS = 3;
    public static final int FIVE_NEIGHBOURS = 4;
    public static final int SIX_NEIGHBOURS = 5;
    // sections
    public static final int SECTION_25 = 0;
    public static final int SECTION_31 = 1;
    public static final int SECTION_37 = 2;
    private final int MAX_RANGE = 16; // m/s^2
    private final int MIN_RANGE = -16; // m/s^2

    // temporary data use during computations
    // used in removeFirstSamples()
    private final int noSamplesToRemove = 15;
    private int dataOriginSize;
    private int dataSize;

    public Filters(){
        useRemoveFirst = false;
        usePassFilter = true; // use pass filter as default in SensorDataHandler>SensorFilter
        useAverageSamples = true;
        useSectionedSequence = true;
        // data below in range from [0-2] ONLY
        filterType = LOW_PASS_FILTER;
        averageOfNeighbours = TWO_NEIGHBOURS;
        sectionsNumber = SECTION_31;
    }
    public Filters(boolean setAllTrue){
        useRemoveFirst = setAllTrue;
        usePassFilter = setAllTrue;
        useAverageSamples = setAllTrue;
        useSectionedSequence = setAllTrue;
        // data below in range from [0-2] ONLY
        filterType = LOW_PASS_FILTER;
        averageOfNeighbours = FIVE_NEIGHBOURS;
        sectionsNumber = SECTION_31;
    }

    /**Setters**/
    public void setUsePassFilter(boolean usePassFilter) {this.usePassFilter = usePassFilter;}
    public void setUseAverageSamples(boolean useAverageSamples) {this.useAverageSamples = useAverageSamples;}
    public void setUseSectionedSequence(boolean useSectionedSequence) {this.useSectionedSequence = useSectionedSequence;}

    public void setFilterType(int filterType) { this.filterType = filterType;}
    public void setAverageOfNeighbours(int averageOfNeighbours) {this.averageOfNeighbours = averageOfNeighbours;}
    public void setSectionsNumber(int sectionsNumber) {this.sectionsNumber = sectionsNumber;}
    /**Getter's**/
    public boolean isUsePassFilter() {return usePassFilter;}
    public boolean isUseAverageSamples() {return useAverageSamples;}
    public boolean isUseSectionedSequence() {return useSectionedSequence;}

    public int getFilterType() {
        return filterType;
    }
    public int getAverageOfNeighbours() {
        return averageOfNeighbours;
    }
    public int getSectionsNumber() { return sectionsNumber;}

    /**Main Algorithm**/
    public HashMap<Integer,List<Float>> filter(HashMap<Integer,List<Float>> data){
        HashMap<Integer,List<Float>> dataCopy = new HashMap<>();
        for(Map.Entry<Integer,List<Float>> axis : data.entrySet()) {
            dataCopy.put(axis.getKey(), filter(axis.getValue()));
        }
        return dataCopy;
    }

    public List<Float> filter(List<Float> data){
        // first samples contain noise (skok przy wlaczeniu sensora)
        if(useRemoveFirst)
            data = removeFirstSamples(data);
        if(usePassFilter)
            data = passFilter(data);
        if(useAverageSamples)
            data = averageSamples(data);
        if(useSectionedSequence)
            data = sectionedSequence(data);

        return data;
    }

    private List<Float> removeFirstSamples(List<Float> data){
        dataOriginSize = data.size();

        if(dataOriginSize > noSamplesToRemove){
            for (int i = 0; i < noSamplesToRemove; i++)
                data.remove(0);
            dataSize = dataOriginSize - noSamplesToRemove;
        }else{
            dataSize = dataOriginSize;
        }
        return data;
    }

    private List<Float> passFilter(List<Float> data){
        return data;
    }

    public List<Float> averageSamples(List<Float> data){
        final int nearestNeighbours;
        // po wielu rozmyslaniach zastowoanie switcha to najlepszy pomysl, lepiej tutaj ustalic
        // jaka wartosc jest naprawde przypisaywania, niz bawic sie w 10 miejscach i miec
        // metlik w kodzie. pole static final oznacza pozycje na spinnerze wylacznie, exclusevely
        switch (averageOfNeighbours){
            case ONE_NEIGHBOUR:
                nearestNeighbours = 1;
                break;
            case TWO_NEIGHBOURS:
                nearestNeighbours = 2;
                break;
            case THREE_NEIGHBOURS:
                nearestNeighbours = 3;
                break;
            case FOUR_NEIGHBOURS:
                nearestNeighbours = 4;
                break;
            case FIVE_NEIGHBOURS:
                nearestNeighbours = 5;
                break;
            case SIX_NEIGHBOURS:
                nearestNeighbours = 6;
                break;
            default: // out of range
                nearestNeighbours = 0;
                break;
        }
        List<Float> dataCopy = new ArrayList<Float>();
        final int fifoSize = nearestNeighbours*2+1;
        // Wariant gdzie to przechowywania wartosci sasiadow uzyta jest kolejka FIFO.
        // Algorymt iteruje po wszystkich elementach kolejki i zwraca ich wartosc usredniona.
        // Przy nastepnej iteracji nowa wartosc jest dodawana do kolejki a najstarsza usuwana.
        dataSize = data.size();
        if(dataSize > fifoSize) {
            // Fifo queue that hold item and all their neighbours
            LinkedList<Float> fifo = new LinkedList<>();

            ListIterator<Float> dataIter = data.listIterator();
            float unchangedValue;

            // copy one-to-one, those values that haven't enough neighbours to perform this algorithm
            for (int i = 0; i < fifoSize; i++) {
                unchangedValue = dataIter.next();
                fifo.addFirst(unchangedValue);
                if (i < nearestNeighbours)
                    dataCopy.add(unchangedValue);
            }

            // perform algorithm on the samples, those have enough neighbours
            ListIterator<Float> fifoIter;
            float sum;
            float usun;
            for (int i = nearestNeighbours; i < dataSize - nearestNeighbours; i++) {
                sum = 0;
                fifoIter = fifo.listIterator();
                while(fifoIter.hasNext()){
                    sum += fifoIter.next();
                }
                // add averaged value to dataCopy
                sum = sum / fifoSize;
                dataCopy.add(sum);
                // add new item to fifo
                if(dataIter.hasNext()){
                    fifo.addFirst(dataIter.next());
                    fifo.removeLast();
                }
            }

            // copy one-to-one - for last values
            for(int i = dataSize - nearestNeighbours; i < dataSize; i++){
                dataCopy.add(data.get(i));
            }

            return dataCopy;
        }else {
            return data;
        }
    }

    private List<Float> sectionedSequence(List<Float> data) {
        final int sectionNumber;
        switch (getSectionsNumber()){
            case SECTION_25:
                sectionNumber = 25;
                break;
            case SECTION_31:
                sectionNumber = 31;
                break;
            case SECTION_37:
                sectionNumber = 37;
                break;
            default:
                sectionNumber = 0;
                break;
        }

        List<Float> dataCopy = new ArrayList<>();
        float value;
        Iterator<Float> iter = data.iterator();
        while (iter.hasNext()){
            value = iter.next();
            if(-1 <= value && value <= 1)
                dataCopy.add(0f);
            else
                dataCopy.add((float) Math.round(value));
        }
        return dataCopy;
    }
}


