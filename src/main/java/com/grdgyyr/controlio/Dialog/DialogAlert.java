package com.grdgyyr.controlio.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;


public class DialogAlert extends DialogFragment {
    private static final String GESTURE_LENGTH = "length";
    private static final String GESTURE_REPETITION = "repetition";
    private static final String OPEN_MODE = "mode";
    public static final int TO_SHORT_SEQUENCE = 0;
    public static final int TO_SHORT_DURATION = 1;
    public static final int REPEAT = 2;
    private static final int WHITE = R.color.white;
    private static final int RED = R.color.hot_pink;
    private static final int GREEN = R.color.green_primary;

    private TextView title;
    private View line;
    private LinearLayout myLayout;

    private int dataLength;
    private int repetition;
    private int openMode;
    private Activity activity;
    private Resources resources;

    public DialogAlert(){}

    public static DialogAlert newInstance(int openMode, int gestureRepetition, int gestureSize){
        DialogAlert dialog = new DialogAlert();
        Bundle args = new Bundle();
        args.putInt(OPEN_MODE, openMode);
        args.putInt(GESTURE_LENGTH, gestureSize);
        args.putInt(GESTURE_REPETITION, gestureRepetition);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openMode = getArguments().getInt(OPEN_MODE);
        dataLength = getArguments().getInt(GESTURE_LENGTH);
        repetition = getArguments().getInt(GESTURE_REPETITION);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        resources = getResources();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_alert, null);

        title = (TextView) view.findViewById(R.id.tv_dialog_alert_title);
        line = (View) view.findViewById(R.id.v_dialog_alert_title);
        myLayout = (LinearLayout) view.findViewById(R.id.ll_dialog_alert);

        switch (openMode){
            case REPEAT:
                setTitle(GREEN, "REPETITION");
                addRepetitionCounterView(inflater);
                break;
            case TO_SHORT_DURATION:
                setTitle(RED, "TO SHORT DURATION");
                if(dataLength>0){
                    addRepetitionCounterView(inflater);
                }
                View viewDuration = inflater.inflate(R.layout.dialog_alert_to_short_duration, null);
                TextView tvDuration = (TextView) viewDuration.findViewById(R.id.dialog_alert_duration);
                tvDuration.setText(String.valueOf(RecognitionManager.gestureDurationMIN));
                myLayout.addView(viewDuration);
                break;
            case TO_SHORT_SEQUENCE:
                setTitle(RED, "TO SHORT SEQUENCE");
                if(dataLength>0){
                    addRepetitionCounterView(inflater);
                }
                View viewSequence = inflater.inflate(R.layout.dialog_alert_to_short_sequence, null);
                TextView tvRequired = (TextView) viewSequence.findViewById(R.id.tv_dialog_alert_required);
                TextView tvGiven = (TextView) viewSequence.findViewById(R.id.tv_dialog_alert_given);
                tvRequired.setText(String.valueOf(RecognitionManager.MIN_ACC_LENGTH));
                tvGiven.setText(String.valueOf(dataLength));
                myLayout.addView(viewSequence);
                break;
            default:
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // nic
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    private void setTitle(int color, String titleName){
        title.setText(titleName);
        title.setTextColor(resources.getColor(color));
        line.setBackgroundColor(resources.getColor(color));
    }
    private void addRepetitionCounterView(LayoutInflater layoutInflater){
        View header = layoutInflater.inflate(R.layout.dialog_alert_repetition, null);
        TextView tvCurRep = (TextView) header.findViewById(R.id.tv_dialog_alert_current_repetition);
        TextView tvMaxRep = (TextView) header.findViewById(R.id.tv_dialog_alert_max_repetition);
        tvCurRep.setText(String.valueOf(repetition));
        tvMaxRep.setText(String.valueOf(RecognitionManager.getInstance().getRepeatGesture() + 1));
        myLayout.addView(header);
    }

    private TextView createDefaultTextView(int size, int color, String content){
        TextView tv = new TextView(activity);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tv.setTextColor(resources.getColor(color));
        tv.setText(content);
        tv.setTextSize(size);
        return tv;
    }
}
