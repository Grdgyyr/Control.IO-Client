package com.grdgyyr.controlio.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;


import java.util.ArrayList;
import java.util.List;

import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.RecognitionTools.AlgNN;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;

public class RecognitionToolNN extends Fragment implements AdapterView.OnItemSelectedListener,
        CheckBox.OnCheckedChangeListener{
    private AlgNN algNN;

    private List<CheckBox> chbIsUse;
    private List<Spinner> spinActivFun;
    private List<Switch> switchBias;
    private List<EditText> etNeurons;
    private final int VIEWS_NO = 5;


    public RecognitionToolNN(){
        algNN = RecognitionManager.getInstance().getNNAlgorithm();
        chbIsUse = new ArrayList<>();
        spinActivFun = new ArrayList<>();
        switchBias = new ArrayList<>();
        etNeurons = new ArrayList<>();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recognition_tool_nn, container, false);

        /**CheckBox**/
        chbIsUse.add((CheckBox) rootView.findViewById(R.id.chb_rt_nn_is_1layer));
        chbIsUse.add( (CheckBox) rootView.findViewById(R.id.chb_rt_nn_is_2layer));
        chbIsUse.add( (CheckBox) rootView.findViewById(R.id.chb_rt_nn_is_3layer));
        chbIsUse.add( (CheckBox) rootView.findViewById(R.id.chb_rt_nn_is_4layer));
        chbIsUse.add((CheckBox) rootView.findViewById(R.id.chb_rt_nn_is_5layer));
        for(int i=0; i<VIEWS_NO; i++) {
            chbIsUse.get(i).setOnCheckedChangeListener(this);
        }
        /**Spinners**/
        spinActivFun.add((Spinner) rootView.findViewById(R.id.s_rt_nn_1layer));
        spinActivFun.add((Spinner) rootView.findViewById(R.id.s_rt_nn_2layer));
        spinActivFun.add((Spinner) rootView.findViewById(R.id.s_rt_nn_3layer));
        spinActivFun.add((Spinner) rootView.findViewById(R.id.s_rt_nn_4layer));
        spinActivFun.add((Spinner) rootView.findViewById(R.id.s_rt_nn_5layer));
        ArrayAdapter<CharSequence> adapterActivFun = ArrayAdapter.createFromResource(
                this.getActivity(),R.array.activation_function,R.layout.rt_spinner_item);
        adapterActivFun.setDropDownViewResource(R.layout.rt_spinner_dropdown_item);
        for(int i=0; i<VIEWS_NO; i++) {
            spinActivFun.get(i).setAdapter(adapterActivFun);
            spinActivFun.get(i).setOnItemSelectedListener(this);
        }
        /**Switches**/
        switchBias.add((Switch) rootView.findViewById(R.id.sw_rt_nn_bias1));
        switchBias.add( (Switch) rootView.findViewById(R.id.sw_rt_nn_bias2));
        switchBias.add( (Switch) rootView.findViewById(R.id.sw_rt_nn_bias3));
        switchBias.add( (Switch) rootView.findViewById(R.id.sw_rt_nn_bias4));
        switchBias.add( (Switch) rootView.findViewById(R.id.sw_rt_nn_bias5));
        for(int i=0; i<VIEWS_NO; i++) {
            switchBias.get(i).setOnCheckedChangeListener(this);
        }
        /**EditTexts**/
        etNeurons.add( (EditText) rootView.findViewById(R.id.et_rt_nn_neurons1));
        etNeurons.add( (EditText) rootView.findViewById(R.id.et_rt_nn_neurons2));
        etNeurons.add( (EditText) rootView.findViewById(R.id.et_rt_nn_neurons3));
        etNeurons.add( (EditText) rootView.findViewById(R.id.et_rt_nn_neurons4));
        etNeurons.add( (EditText) rootView.findViewById(R.id.et_rt_nn_neurons5));
        for(int i=0; i<VIEWS_NO; i++) {
            final int j = i;
            etNeurons.get(i).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String neuronNumber = etNeurons.get(j).getText().toString();
                    if(!neuronNumber.equals("")) {
                        algNN.setNeuron(j, Integer.parseInt(neuronNumber));
                        Log.i("View NN", "neurons[" + j + "] = " + neuronNumber);
                    }
                }
            });
        }
        initializeViewsParameters();
        return rootView;
    }

    private void initializeViewsParameters(){
        for(int i=0; i<VIEWS_NO; i++) {
            int[] params = algNN.getLayerInfo(i);
            chbIsUse.get(i).setChecked(params[0] != 0);
            spinActivFun.get(i).setSelection(params[1]);
            switchBias.get(i).setChecked(params[2] != 0);
            etNeurons.get(i).setText(String.valueOf(params[3]));
        }
    }

    /**For CheckBoxes**/
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.chb_rt_nn_is_2layer:
                Log.i("View NN", "useLayer["+1+"] = "+b);
                algNN.setUseLayer(1, b);
                break;
            case R.id.chb_rt_nn_is_3layer:
                Log.i("View NN", "useLayer["+2+"] = "+b);
                algNN.setUseLayer(2, b);
                break;
            case R.id.chb_rt_nn_is_4layer:
                Log.i("View NN", "useLayer["+3+"] = "+b);
                algNN.setUseLayer(3, b);
                break;
            case R.id.sw_rt_nn_bias1:
                algNN.setUseBias(0, b);
                Log.i("View NN", "useBias["+0+"] = "+b);
                break;
            case R.id.sw_rt_nn_bias2:
                algNN.setUseBias(1, b);
                Log.i("View NN", "useBias["+1+"] = "+b);
                break;
            case R.id.sw_rt_nn_bias3:
                algNN.setUseBias(2, b);
                Log.i("View NN", "useBias["+2+"] = "+b);
                break;
            case R.id.sw_rt_nn_bias4:
                algNN.setUseBias(3, b);
                Log.i("View NN", "useBias["+3+"] = "+b);
                break;
            case R.id.sw_rt_nn_bias5:
                algNN.setUseBias(4, b);
                Log.i("View NN", "useBias["+4+"] = "+b);
                break;
            default:
                break;
        }
    }

    /**For Spinners**/
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch(adapterView.getId()){
            case R.id.s_rt_nn_1layer:
                algNN.setActivFun(0, i);
                Log.i("View NN", "activFun[" + 0 + "] = " + i);
                break;
            case R.id.s_rt_nn_2layer:
                algNN.setActivFun(1, i);
                Log.i("View NN", "activFun[" + 1 + "] = " + i);
                break;
            case R.id.s_rt_nn_3layer:
                algNN.setActivFun(2, i);
                Log.i("View NN", "activFun[" + 2 + "] = " + i);
                break;
            case R.id.s_rt_nn_4layer:
                algNN.setActivFun(3, i);
                Log.i("View NN", "activFun[" + 3 + "] = " + i);
                break;
            case R.id.s_rt_nn_5layer:
                algNN.setActivFun(4, i);
                Log.i("View NN", "activFun[" + 4 + "] = " + i);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
