package com.grdgyyr.controlio.RecognitionTools;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import com.grdgyyr.controlio.SensorDataHandler.SensorData;

/**
 * Created by pepegeo on 2015-11-11.
 */
public class AlgDTW extends Algorithms {

    private static final String PREFIX = "comparision_";
    public static final int SAKOE_CHIBA_BAND = 0;
    public static final int ITAKURA_PARALLELOGRAM = 1;

    Context context;

    private boolean useWindowCondition;
    private int windowType;

    private float[][]DTWmatrix;
    private String[][]track;
    private String compareName;
    private boolean allowSaveComparision;

    public AlgDTW(){
        super();  // very important to use SUPER!
        useWindowCondition = false;
        windowType = SAKOE_CHIBA_BAND;
        allowSaveComparision = false;
    }

    @Override
    public void clearAlgorithm() {

    }

    @Override
    public List<Results> getResults() {
        return null;
    }

    /**Setter's**/
    public void setUseWindowCondition(boolean useWindowCondition) {
        this.useWindowCondition = useWindowCondition;
    }

    public void setWindowType(int windowType) {
        this.windowType = windowType;
    }

    /**Getter's**/
    public boolean isUseWindowCondition() {
        return useWindowCondition;
    }

    public int getWindowType() {
        return windowType;
    }


    @Override
    public float compareTwoSequences(List<Float> base, List<Float> compare) {
        return -1;
    }
    @Override
    public float compareTwoSequences(HashMap<Integer,List<Float>> base,
                                     HashMap<Integer,List<Float>> compare,
                                     String compareName, Context context){
        this.compareName = compareName;
        this.context = context;
        allowSaveComparision = true;
        float result = compareTwoSequences(base, compare);
        allowSaveComparision = false;
        return result;
    }

    public float compareTwoSequences(HashMap<Integer,List<Float>> base,
                                              HashMap<Integer,List<Float>> compare){
        Log.i("DTW alg", "comparing two Sequences");
        List<Float> baseX = base.get(SensorData.X_axis);
        List<Float> baseY = base.get(SensorData.Y_axis);
        List<Float> baseZ = base.get(SensorData.Z_axis);
        List<Float> compareX = compare.get(SensorData.X_axis);
        List<Float> compareY = compare.get(SensorData.Y_axis);
        List<Float> compareZ = compare.get(SensorData.Z_axis);

        int xSize = baseX.size();
        int ySize = compareX.size();
        DTWmatrix = new float[xSize][ySize];
        track = new String[xSize][ySize];
        for(int i = 0; i<xSize; i++){
            for(int j = 0; j<ySize; j++){
                track[i][j]="#";
            }
        }

        // only for first sample
        DTWmatrix[0][0] = euclideanDistance(baseX.get(0),baseY.get(0),baseZ.get(0),
                compareX.get(0),compareY.get(0), compareZ.get(0));
        track[0][0] = "*";
        //initialize first column (horizontal)
        float tempX = compareX.get(0);
        float tempY = compareY.get(0);
        float tempZ = compareZ.get(0);
        for(int i=1; i<xSize; i++){
            DTWmatrix[i][0] = euclideanDistance(baseX.get(i),baseY.get(i),baseZ.get(i),
                    tempX, tempY, tempZ) + DTWmatrix[i-1][0];
            track[i][0] = "|";
        }
        // initialize first row (vertical)
        tempX = baseX.get(0);
        tempY = baseY.get(0);
        tempZ = baseZ.get(0);
        for(int j=1; j<ySize; j++){
            DTWmatrix[0][j] = euclideanDistance(tempX, tempY, tempZ,
                    compareX.get(j),compareY.get(j),compareZ.get(j)) + DTWmatrix[0][j-1];
            track[0][j] = "-";
        }
        // count the rest
        // i - vertical , j - horizontal
        float minValue;
        for(int j=1; j<ySize; j++){
            tempX = compareX.get(j);
            tempY = compareY.get(j);
            tempZ = compareZ.get(j);
            for(int i=1; i<xSize; i++){
//                minValue = DTWmatrix[i][j-1] < DTWmatrix[i-1][j-1] ? DTWmatrix[i][j-1] : DTWmatrix[i-1][j-1];
//                minValue = minValue < DTWmatrix[i-1][j] ? minValue : DTWmatrix[i-1][j];
//                minValue = Math.min(DTWmatrix[i][j-1], DTWmatrix[i-1][j-1]);
//                minValue = Math.min(minValue, DTWmatrix[i-1][j]);
                if(DTWmatrix[i][j-1] < DTWmatrix[i-1][j-1]){
                    minValue = DTWmatrix[i][j-1];
                    track[i][j-1] = "-";
                }else{
                    minValue = DTWmatrix[i-1][j-1];
                    track[i-1][j-1] = "/";
                }
                if(minValue > DTWmatrix[i-1][j]){
                    minValue = DTWmatrix[i-1][j];
                    track[i-1][j] = "|";
                }
                DTWmatrix[i][j] = euclideanDistance(baseX.get(i),baseY.get(i),baseZ.get(i),
                        tempX, tempY, tempZ) + minValue;
            }
        }

        //Create string of processed matrixes
        Log.i("DTW alg", "Result of comparision is: "+ DTWmatrix[xSize-1][ySize-1]);
        StringBuilder trackMatrix = new StringBuilder();
        for(int i=(xSize-1); i>=0; i--) {
            for(int j=0; j<ySize; j++) {
                trackMatrix.append( track[i][j].toString() );
                trackMatrix.append(" ");
            }
            trackMatrix.append("\n");
        }
        // if allowSaveComparision==true then save dtw matrix to external storage
//        if(allowSaveComparision){
//            Calendar cal = Calendar.getInstance();
//            saveFileToExternalStorage(compareName, DTWmatrix, trackMatrix.toString());
//        }
        return DTWmatrix[xSize-1][ySize-1];
    }

    private float euclideanDistance(float x1, float y1, float z1, float x2, float y2, float z2){
        return (float) Math.sqrt(Math.pow(x1-x2,2) + Math.pow(y1-y2,2) + Math.pow(z1-z2,2));
    }

    public boolean saveFileToExternalStorage(String filename, float[][] valueMartix,
                                             String trackMatrix) {
        Log.i("DTW SAVE"," save comparision of "+filename);
        String path = Environment.getExternalStorageDirectory().toString() +
                SensorData.COMPARE_FOLDER;
        File myFile = new File(path);
        // Make sure that directory exist
        myFile.mkdir();
        File file = new File(myFile, PREFIX + filename);
        file.mkdir();

        try {
            if (file.exists()) file.delete();
            //FileOutputStream outputStream = context.openFileOutput(prefix + filename, Context.MODE_PRIVATE);
            FileOutputStream outputStream = new FileOutputStream(file);
//            ObjectOutputStream outputObject = new ObjectOutputStream(outputStream);
//            outputObject.writeChars(trackMatrix);
//            outputObject.flush();
//            outputObject.close();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(trackMatrix);
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
        } catch (IOException e) {
            Log.w("ExternalStorage ", "Error writing " + myFile, e);
        }
        return false;
    }
}
