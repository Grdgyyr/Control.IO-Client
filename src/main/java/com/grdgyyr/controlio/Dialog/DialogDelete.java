package com.grdgyyr.controlio.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grdgyyr.controlio.Fragments.ActivityGestureRecognizer;
import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.SensorDataHandler.SensorData;
import com.grdgyyr.controlio.Utilities.Commands;
import com.grdgyyr.controlio.Utilities.Connection;

/**
 * Created by grdgy on 16/03/2018.
 */

public class DialogDelete extends DialogFragment {



    private Activity activity;
    private Resources resources;
    SensorData mData = new SensorData();

    static String GestureName;

    public DialogDelete(){}




    public static DialogDelete newInstance(String name){
        DialogDelete dialog = new DialogDelete();
        Bundle args = new Bundle();
        GestureName = name;
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        resources = getResources();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_alert, null);

        //title = (TextView) view.findViewById(R.id.tv_dialog_alert_title);
        //line = (View) view.findViewById(R.id.v_dialog_alert_title);
        //myLayout = (LinearLayout) view.findViewById(R.id.ll_dialog_alert);



        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view)
                .setPositiveButton("Delete Gesture", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("TEST:", "OK : " + GestureName);
                        mData.deleteFileFromExternalStorage(GestureName);

                        ((ActivityGestureRecognizer)getActivity()).populateSavedGestures();

                        //Connection.Send("GESTUREARRAY"+Commands.getArrayGestures());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });




        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }


}
