package com.grdgyyr.controlio.Fragments;


import android.content.Context;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.RecognitionTools.Filters;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;
import com.grdgyyr.controlio.RecognitionTools.Utilities;

import java.util.List;


public class FragmentRecognitionTool extends Fragment implements OnItemSelectedListener {
    RecognitionManager recManager;

    private CheckBox cbReduce;
    private CheckBox cbFilter;
    private CheckBox cbAverage;
    private CheckBox cbSection;

    private CheckBox cbAcc;
    private CheckBox cbGyro;


    private CheckBox cbcheatsGesture;
    private Spinner sGestRepetition;
//    private Spinner sFilter;
    private Spinner sAverage;
    private Spinner sSections;
    private EditText etReduce;
    private EditText etMinLength;
    private EditText etMinDuration;
    private EditText etMaxDuration;

    public  int IdDTW;

    private FragmentRecognitionTool.OnFragmentInteractionListener mListener;

    public FragmentRecognitionTool() {
        recManager = RecognitionManager.getInstance();
        IdDTW = View.generateViewId();
    }

    public static FragmentRecognitionTool newInstance() {
        FragmentRecognitionTool fragment = new FragmentRecognitionTool();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentRating.OnFragmentInteractionListener) {
            mListener = (FragmentRecognitionTool.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**Edit Text**/
        etReduce = (EditText) getView().findViewById(R.id.et_rt_reduction_length);
        etMinLength = (EditText) getView().findViewById(R.id.et_rt_min_length);
        etMinDuration = (EditText) getView().findViewById(R.id.et_rt_min_duration);
        etMaxDuration = (EditText) getView().findViewById(R.id.et_rt_max_duration);

        /**Spinners**/
        sGestRepetition = (Spinner) getView().findViewById(R.id.s_rt_gesture_repetition);
//        sFilter = (Spinner) rootView.findViewById(R.id.s_rt_filter_type);
        sAverage = (Spinner) getView().findViewById(R.id.s_rt_average_neighbours);
        sSections = (Spinner) getView().findViewById(R.id.s_rt_sections_number);
        ArrayAdapter<CharSequence> aGestRep = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.gesture_repetition,R.layout.rt_spinner_item );
//        ArrayAdapter<CharSequence> aFilter = ArrayAdapter.createFromResource(this.getActivity(),
//                R.array.filter_type,R.layout.rt_spinner_item );
        ArrayAdapter<CharSequence> aAverage = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.average_neighbours,R.layout.rt_spinner_item );
        ArrayAdapter<CharSequence> aSections = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.section_type,R.layout.rt_spinner_item );
        aGestRep.setDropDownViewResource(R.layout.rt_spinner_dropdown_item);
//        aFilter.setDropDownViewResource(R.layout.rt_spinner_dropdown_item);
        aAverage.setDropDownViewResource(R.layout.rt_spinner_dropdown_item);
        aSections.setDropDownViewResource(R.layout.rt_spinner_dropdown_item);
        sGestRepetition.setAdapter(aGestRep);
//        sFilter.setAdapter(aFilter);
        sAverage.setAdapter(aAverage);
        sSections.setAdapter(aSections);

        /**CheckBoxes**/
        cbReduce = (CheckBox) getView().findViewById(R.id.chb_rt_reduction);
        cbFilter = (CheckBox) getView().findViewById(R.id.chb_rt_is_filter);
        cbAverage = (CheckBox) getView().findViewById(R.id.chb_rt_is_average);
        cbSection = (CheckBox) getView().findViewById(R.id.chb_rt_is_sectioned);
        cbAcc = (CheckBox) getView().findViewById(R.id.cb_rt_accelerometer);
        cbGyro = (CheckBox) getView().findViewById(R.id.cb_rt_gyroscope);

        cbcheatsGesture = (CheckBox) getView().findViewById(R.id.cb_rt_cheat_previous);

        cbcheatsGesture.setChecked(true);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        return inflater.inflate(R.layout.fragment_recognition_tool, container, false);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        initializeViewsState();
        setUpListeners();
    }
    private void initializeViewsState(){
        sGestRepetition.setSelection(recManager.getRepeatGesture());
        etMinLength.setText(String.valueOf(recManager.MIN_ACC_LENGTH));
        etMinDuration.setText(String.valueOf(recManager.gestureDurationMIN));
        etMaxDuration.setText(String.valueOf(recManager.gestureDurationMAX));
        etMinLength.setEnabled(false);
        etMinDuration.setEnabled(false);
        etMaxDuration.setEnabled(false);

        Filters filters = recManager.getFilter();
        cbReduce.setChecked(Utilities.getInstance().getUseReduceDataSize());
        etReduce.setText(String.valueOf(Utilities.getInstance().getReduceDataSizeTo()));
//        sFilter.setSelection(filters.getFilterType());
        sAverage.setSelection(filters.getAverageOfNeighbours());
        sSections.setSelection(filters.getSectionsNumber());

        cbcheatsGesture.setChecked(true);
        cbFilter.setChecked(filters.isUsePassFilter());
        cbAverage.setChecked(filters.isUseAverageSamples());
        cbSection.setChecked(filters.isUseSectionedSequence());
        List<Integer> sensorList = recManager.getSensors();
        for(Integer sensor: sensorList){
            switch (sensor){
                case Sensor.TYPE_ACCELEROMETER:
                    cbAcc.setChecked(true);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    cbGyro.setChecked(true);
                    break;
            }
        }
    }
    private void setUpListeners(){
        //edittext
        etReduce.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String length = etReduce.getText().toString();
                    if(!length.equals("")) {
                        Utilities.getInstance().setReduceDataSizeTo(Integer.parseInt(length));
                    }
                }
            });

        //spinners
        sGestRepetition.setOnItemSelectedListener(this);
//        sFilter.setOnItemSelectedListener(this);
        sAverage.setOnItemSelectedListener(this);
        sSections.setOnItemSelectedListener(this);
        //checkbox
        cbcheatsGesture.setOnCheckedChangeListener(myCheckboxListener);
        cbReduce.setOnCheckedChangeListener(myCheckboxListener);
        cbFilter.setOnCheckedChangeListener(myCheckboxListener);
        cbAverage.setOnCheckedChangeListener(myCheckboxListener);
        cbSection.setOnCheckedChangeListener(myCheckboxListener);
        cbAcc.setOnCheckedChangeListener(myCheckboxListener);
        cbGyro.setOnCheckedChangeListener(myCheckboxListener);

    }

    // LISTENER FOR SPINNER's
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.s_rt_gesture_repetition:
                recManager.setRepeatGesture(position);
                break;
//            case R.id.s_rt_filter_type:
//                recManager.getFilter().setFilterType(position);
//                break;
            case R.id.s_rt_average_neighbours:
                recManager.getFilter().setAverageOfNeighbours(position);
                break;
            case R.id.s_rt_sections_number:
                recManager.getFilter().setSectionsNumber(position);
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // LISTENER FOR CHECKBOX'es
    private CompoundButton.OnCheckedChangeListener myCheckboxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int Id = buttonView.getId();
            switch (Id){
                case R.id.cb_rt_cheat_previous:
                    recManager.setUsePreviousGesture(isChecked);
                    break;
                case R.id.chb_rt_reduction:
                    Utilities.getInstance().setUseReduceDataSize(isChecked);
                    break;
                case R.id.chb_rt_is_filter:
                    recManager.getFilter().setUsePassFilter(isChecked);
                    break;
                case R.id.chb_rt_is_average:
                    recManager.getFilter().setUseAverageSamples(isChecked);
                    break;
                case R.id.chb_rt_is_sectioned:
                    recManager.getFilter().setUseSectionedSequence(isChecked);
                    break;
                case R.id.cb_rt_accelerometer:
                    if(!recManager.setSensors(isChecked, Sensor.TYPE_ACCELEROMETER)){
                        buttonView.setChecked(true);
                        Toast.makeText(getActivity().getBaseContext(),
                                "At least one Sensor must be selected", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.cb_rt_gyroscope:
                    if(!recManager.setSensors(isChecked, Sensor.TYPE_GYROSCOPE)){
                        buttonView.setChecked(true);
                        Toast.makeText(getActivity().getBaseContext(),
                                "At least one Sensor must be selected", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    if(Id == IdDTW){
                        // if returns false it means that at least one algorithm must be checked
                        if(!recManager.setAlgorithm(isChecked,RecognitionManager.DTW)){
                            buttonView.setChecked(true);
                            Toast.makeText(getActivity().getBaseContext(),
                                    "At least one Algorithm must be selected", Toast.LENGTH_SHORT).show();
                        }
                        Log.i("RecTool CheckBox", "selected DTW as="+isChecked);
                    }
                    break;
            }
        }
    };

//    Toast.makeText(getActivity().getBaseContext(),"Average: "+recognitionManager.getFilter().isUseAverageSamples(),Toast.LENGTH_SHORT).show();
}
