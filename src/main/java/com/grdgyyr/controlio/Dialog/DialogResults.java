package com.grdgyyr.controlio.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.RecognitionTools.Filters;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;
import com.grdgyyr.controlio.RecognitionTools.Results;
import com.grdgyyr.controlio.RecognitionTools.Utilities;
import com.grdgyyr.controlio.SensorDataHandler.SensorData;
import com.grdgyyr.controlio.Utilities.Commands;
import com.grdgyyr.controlio.Utilities.Connection;
import com.grdgyyr.controlio.Utilities.MatchingAdapter;
import com.grdgyyr.controlio.RecognitionTools.Results;

/**
 * Created by pepegeo on 2015-11-19.
 */
public class DialogResults extends DialogFragment implements CompoundButton.OnCheckedChangeListener {

    private static final String RESULTS_FOLDER = SensorData.FOLDER_NAME + "/aResults";

    //pass results to activity
    public interface IdentifyDialogListener{
        boolean tryToSave();
    }
    IdentifyDialogListener mListener;
    RecognitionManager recManager;
    MatchingAdapter adapter;
    Context context;
    RecognizeTask recognizeThread;
    long startTime;
    long estimatedTime;
    String totalTime;

    TextView txtGesture;


    public DialogResults(){
        recManager = RecognitionManager.getInstance();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity.getBaseContext();
        try{
            mListener = (IdentifyDialogListener) activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + "must implement IdentifyDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_results, null);
        builder.setView(view)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //mListener.onNewGestureDialogNegativeClick();
                        recognizeThread.cancel(true);
                    }
                });


         txtGesture = (TextView) view.findViewById(R.id.txtGesture);

        ListView lvMatchingList = (ListView) view.findViewById(R.id.lv_identify_dialog);
        adapter = new MatchingAdapter(getActivity().getBaseContext(), new ArrayList<Results>());
        lvMatchingList.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        recognizeThread = new RecognizeTask();
        recognizeThread.execute();





        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        recognizeThread.cancel(true);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        recognizeThread.cancel(true);
    }



    private class RecognizeTask extends AsyncTask<Void, Void, List<Results>> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        @Override
        protected List<Results> doInBackground(Void... params) {
            recManager.setContext(context);
            startTime = System.currentTimeMillis();
            //Log.i("GESTURE OUTPUT", "" + results.getNameOfGesture());
            return recManager.recognize();
        }



        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(List<Results> results) {
            //estimatedTime = System.currentTimeMillis() - startTime;
            //totalTime = String.valueOf(Math.round(estimatedTime)/1000.0);

            estimatedTime = System.currentTimeMillis() - startTime;
            totalTime = String.valueOf(Math.round(estimatedTime)/1000.0);

            Collections.sort(results, Results.Comparators.FITRATE);
            adapter.updateList(results);


            //Gesture is sent to server

            txtGesture.setText(adapter.getGestureName());
            saveResultsToExternalStorage(composeFileName(),context);
            Log.i("GESTURE NAME", "" + adapter.getGestureName());

            Connection.Send("GESTURE|" + adapter.getGestureName());



            Log.i("Dialog Identify", "Results are applied");
        }
    }

    private String composeFileName(){
        StringBuilder builder = new StringBuilder();
        Filters filters = recManager.getFilter();
        builder.append("red:");
        if(Utilities.getInstance().getUseReduceDataSize())
            builder.append(Utilities.getInstance().getReduceDataSizeTo()+ " ");
        else
            builder.append("false ");
        builder.append("av:");
        if(filters.isUseAverageSamples())
            builder.append(filters.getAverageOfNeighbours()+" ");
        else
            builder.append("false ");
        builder.append("sec:");
        if(filters.isUseSectionedSequence())
            builder.append(filters.getSectionsNumber()+" ");
        else
            builder.append("false ");
        builder.append(Calendar.getInstance().getTime().toString());
        return builder.toString();
    }



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    // enable to open in excel
    public boolean saveResultsToExternalStorage(String filename, Context context) {
        if( isExternalStorageWritable()) {

            String path = Environment.getExternalStorageDirectory().toString() + RESULTS_FOLDER;
            File myFile = new File(path);
            // Make sure that directory exist
            myFile.mkdir();
            File file = new File(myFile, filename);
            try {
                if (file.exists()) file.delete();
                FileOutputStream outputStream = new FileOutputStream(file);
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(adapter.toString()+"\n"+totalTime);
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




}
