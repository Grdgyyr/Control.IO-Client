package com.grdgyyr.controlio.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;


import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.RecognitionTools.AlgDTW;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;

public class RecognitionToolDTW extends Fragment implements OnItemSelectedListener {

    private AlgDTW algDTW;

    CheckBox cbWindowCon;
    Spinner sWindowCondition;

    public RecognitionToolDTW(){
        algDTW = RecognitionManager.getInstance().getDWTAlgorithm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recognition_tool_dtw, container, false);

        /**CheckBox**/
        cbWindowCon = (CheckBox) rootView.findViewById(R.id.chb_rt_dtw_adjustment_window_condition);
        cbWindowCon.setOnCheckedChangeListener(myCheckboxListener);

        /**Spinner**/
        sWindowCondition = (Spinner) rootView.findViewById(R.id.s_rt_dtw_window_conditions);
        ArrayAdapter<CharSequence> aWindowCondition = ArrayAdapter.createFromResource(
                this.getActivity(),R.array.adjustment_window_condition,R.layout.rt_spinner_item);
        aWindowCondition.setDropDownViewResource(R.layout.rt_spinner_dropdown_item);
        sWindowCondition.setAdapter(aWindowCondition);

        return rootView;
    }

    private void initializeConditions(){
        cbWindowCon.setChecked(algDTW.isUseWindowCondition());
        sWindowCondition.setSelection(algDTW.getWindowType());
    }

    // LISTENER FOR CHECKBOX'es
    private CompoundButton.OnCheckedChangeListener myCheckboxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int Id = buttonView.getId();
            switch (Id) {
                case R.id.s_rt_dtw_window_conditions:
                    algDTW.setUseWindowCondition(isChecked);
                    break;
            }
        }
    };

    // LISTENER FOR SPINNER's
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.s_rt_dtw_window_conditions:
                switch (position){
                    case 0:
                        algDTW.setWindowType(AlgDTW.SAKOE_CHIBA_BAND);
                        break;
                    case 1:
                        algDTW.setWindowType(AlgDTW.ITAKURA_PARALLELOGRAM);
                        break;
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
