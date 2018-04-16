package com.grdgyyr.controlio.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.grdgyyr.controlio.R;


public class DialogNewGesture extends DialogFragment {
    private static final String SET_SIZE = "dataLength";

    // pass result to activity
    public interface NewGestureDialogListener{
        void onNewGestureDialogPositiveClick(String name);
        boolean onNewGestureDialogTryToSave(String name);
        void onNewGestureDialogNegativeClick();
    }

    NewGestureDialogListener mListener;
    EditText etName;
    TextView tvSize;
    int [] dataLength;

    public DialogNewGesture(){}

    //pass arguments to dialog from activity
    public static DialogNewGesture newInstance(int[] set){
        DialogNewGesture f = new DialogNewGesture();



        Bundle args = new Bundle();
        args.putIntArray(SET_SIZE, set);
        f.setArguments(args);

        return f;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            mListener = (NewGestureDialogListener) activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString()
            + "must implement NewGestureDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataLength = getArguments().getIntArray(SET_SIZE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_newgesture, null);
        builder.setView(view)
                .setMessage(R.string.dialog_newgesture_message)
                .setPositiveButton("Save and show", null)
                .setNegativeButton(R.string.dialog_newgesture_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNewGestureDialogNegativeClick();
                    }
                }).setNeutralButton("Save", null);


        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String gestureName = etName.getText().toString();
                        if (!gestureName.equals("")) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
                            if (mListener.onNewGestureDialogTryToSave(gestureName)) {

                                alertDialog.dismiss();
                            } else {
                                tvSize.setText("Saving failed. Please try once more with different name");
                                tvSize.setTextColor(getResources().getColor(R.color.red));
                            }
                        } else {
                            etName.setHint("Before save enter a name here!");
                        }
                    }
                });
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String gestureName = etName.getText().toString();
                        if (!gestureName.equals("")) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
                            if (mListener.onNewGestureDialogTryToSave(gestureName)) {
                                mListener.onNewGestureDialogPositiveClick(gestureName);
                                alertDialog.dismiss();
                            } else {
                                tvSize.setText("Saving failed. Please try once more with different name");
                                tvSize.setTextColor(getResources().getColor(R.color.red));
                            }
                        } else {
                            etName.setHint("Before save enter a name here!");
                        }
                    }
                });
            }
        });
        alertDialog.show();

        etName = (EditText) view.findViewById(R.id.et_dialog_newgesture);
        tvSize = (TextView) view.findViewById(R.id.tv_dialog_newgesture);
        tvSize.setText("Details ( Gryo size:"+ dataLength[0]+" and Acc size:"+ dataLength[1]+")");

        return alertDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

}
