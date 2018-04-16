package com.grdgyyr.controlio.Fragments;

import android.app.Fragment;
import android.hardware.Sensor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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


import java.util.HashMap;
import java.util.List;

import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.RecognitionTools.Algorithms;
import com.grdgyyr.controlio.RecognitionTools.Filters;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;
import com.grdgyyr.controlio.RecognitionTools.Utilities;


public class RecognitionTool extends Fragment implements OnItemSelectedListener {
    RecognitionManager recManager;

    private CheckBox cbReduce;
    private CheckBox cbFilter;
    private CheckBox cbAverage;
    private CheckBox cbSection;

    private CheckBox cbAcc;
    private CheckBox cbGyro;

    private CheckBox tabDTW;
    private CheckBox tabNN;
    private CheckBox tabSVM;

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
    public  int IdNN;
    public  int IdSVM;

    public RecognitionTool() {
        recManager = RecognitionManager.getInstance();
        IdDTW = View.generateViewId();
        IdNN = View.generateViewId();
        IdSVM = View.generateViewId();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recognition_tool, container, false);

        /**Edit Text**/
        etReduce = (EditText) rootView.findViewById(R.id.et_rt_reduction_length);
        etMinLength = (EditText) rootView.findViewById(R.id.et_rt_min_length);
        etMinDuration = (EditText) rootView.findViewById(R.id.et_rt_min_duration);
        etMaxDuration = (EditText) rootView.findViewById(R.id.et_rt_max_duration);

        /**Spinners**/
        sGestRepetition = (Spinner) rootView.findViewById(R.id.s_rt_gesture_repetition);
//        sFilter = (Spinner) rootView.findViewById(R.id.s_rt_filter_type);
        sAverage = (Spinner) rootView.findViewById(R.id.s_rt_average_neighbours);
        sSections = (Spinner) rootView.findViewById(R.id.s_rt_sections_number);
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
        cbReduce = (CheckBox) rootView.findViewById(R.id.chb_rt_reduction);
        cbFilter = (CheckBox) rootView.findViewById(R.id.chb_rt_is_filter);
        cbAverage = (CheckBox) rootView.findViewById(R.id.chb_rt_is_average);
        cbSection = (CheckBox) rootView.findViewById(R.id.chb_rt_is_sectioned);
        cbAcc = (CheckBox) rootView.findViewById(R.id.cb_rt_accelerometer);
        cbGyro = (CheckBox) rootView.findViewById(R.id.cb_rt_gyroscope);

        cbcheatsGesture = (CheckBox) rootView.findViewById(R.id.cb_rt_cheat_previous);
        /**ViewPager**/
        ViewPager vpAlgorithm = (ViewPager) rootView.findViewById(R.id.vp_algorithm);
        RecognitionToolFragmentAdapter vpAdapter =
                new RecognitionToolFragmentAdapter(getChildFragmentManager());
        vpAlgorithm.setAdapter(vpAdapter);
        /**Tab ViewPager**/
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tl_rt_algorithm);
        tabLayout.setupWithViewPager(vpAlgorithm);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        setUpTabs(tabLayout);

        return rootView;
    }

    private void setUpTabs(TabLayout tabLayout){
        tabDTW = (CheckBox) LayoutInflater.from(this.getActivity()).inflate(
                R.layout.tab_custom_view, null);
        tabDTW.setText("DTW");
        tabDTW.setId(IdDTW); // create two different id for tabs
        tabLayout.getTabAt(0).setCustomView(tabDTW);

        tabNN = (CheckBox) LayoutInflater.from(this.getActivity()).inflate(
                R.layout.tab_custom_view, null);
        tabNN.setText("NN");
        //tabNN.setEnabled(false); // disable because there is no algorithm to handle action
        tabNN.setId(IdNN);
        tabLayout.getTabAt(1).setCustomView(tabNN);



        tabSVM = (CheckBox) LayoutInflater.from(this.getActivity()).inflate(
                R.layout.tab_custom_view, null);
        tabSVM.setText("SVM");
        tabSVM.setId(IdSVM);
        tabSVM.setEnabled(false);
        tabLayout.getTabAt(2).setCustomView(tabSVM);
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

        cbcheatsGesture.setChecked(false);
        recManager.setUsePreviousGesture(false);
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
        HashMap<Integer, Algorithms> algorithmsMap = recManager.getAlgorithms();
        for(Integer algorithm: algorithmsMap.keySet()){
            switch (algorithm){
                case RecognitionManager.DTW:
                    tabDTW.setChecked(true);
                    break;
                case RecognitionManager.NN:
                    tabNN.setChecked(true);
                    break;
                case RecognitionManager.SVM:
                    tabSVM.setChecked(true);
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
        tabDTW.setOnCheckedChangeListener(myCheckboxListener);
        tabNN.setOnCheckedChangeListener(myCheckboxListener);
        tabSVM.setOnCheckedChangeListener(myCheckboxListener);
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
                    }else if(Id == IdNN){
                        if(!recManager.setAlgorithm(isChecked,RecognitionManager.NN)){
                            buttonView.setChecked(true);
                            Toast.makeText(getActivity().getBaseContext(),
                                    "At least one Algorithm must be selected", Toast.LENGTH_SHORT).show();
                        }
                        Log.i("RecTool CheckBox", "selected NN as="+isChecked);
                    }else if(Id == IdSVM){
                        if(!recManager.setAlgorithm(isChecked,RecognitionManager.SVM)){
                            buttonView.setChecked(true);
                            Toast.makeText(getActivity().getBaseContext(),
                                    "At least one Algorithm must be selected", Toast.LENGTH_SHORT).show();
                        }
                        Log.i("RecTool CheckBox", "selected SVM as="+isChecked);
                    }
                    break;
            }
        }
    };

//    Toast.makeText(getActivity().getBaseContext(),"Average: "+recognitionManager.getFilter().isUseAverageSamples(),Toast.LENGTH_SHORT).show();
}
