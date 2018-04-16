package com.grdgyyr.controlio.Utilities;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;
import com.grdgyyr.controlio.RecognitionTools.Results;

/**
 * Created by pepegeo on 2015-11-22.
 */
public class MatchingAdapter extends BaseAdapter {

    private final float APPROVED = 300f;
    private final float MAY_BE_APPROVED = 500f;
    private int approvedColor;
    private int approvedTextColor;
    private int mayBeApprovedColor;
    private int mayBeApprovedTextColor;
    private int normalColor;
    private int normalTextColor;

    String GestureName = "";

    List<Results> resultsList;
    LayoutInflater inflater;

    public MatchingAdapter(Context context, List<Results> resultsList){
        this.inflater = LayoutInflater.from(context);
        this.resultsList = resultsList;

        approvedColor = context.getResources().getColor(R.color.green);
        approvedTextColor = context.getResources().getColor(R.color.black);
        mayBeApprovedColor = context.getResources().getColor(R.color.listview_selected);
        mayBeApprovedTextColor = context.getResources().getColor(R.color.black);
        normalColor = Color.TRANSPARENT;
        normalTextColor = context.getResources().getColor(R.color.rt_text);
    }

    public void updateList(List<Results> newResultList){
        this.resultsList = newResultList;
        notifyDataSetChanged();
    }
    public void addAndUpdate(Results newResult){
        resultsList.add(newResult);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return resultsList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void setHolderColors(ViewHolder holder,int backgroundColor,int textColor){
        holder.llLayout.setBackgroundColor(backgroundColor);
        holder.tvFitRate.setTextColor(textColor);
        holder.tvSensor.setTextColor(textColor);
        holder.tvAlg.setTextColor(textColor);
        holder.Name.setTextColor(textColor);
    }

    private String getAlgText(int alg){
        switch (alg){
            case RecognitionManager.DTW:
                return "DTW";
            case RecognitionManager.NN:
                return "NN";
            case RecognitionManager.SVM:
                return "SVM";
            default:
                return "Unknown";
        }
    }

    private String getSensorText(int sensor){
        switch (sensor){
            case Sensor.TYPE_ACCELEROMETER:
                return "Acc";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyro";
            default:
                return "Unknown";
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.dialog_results_list_item, parent, false);
            holder = new ViewHolder();
            holder.llLayout = (LinearLayout) convertView.findViewById(R.id.ll_list_item_matching);
            holder.tvFitRate = (TextView) convertView.findViewById(R.id.list_item_matching_fit_rate);
            holder.tvSensor = (TextView) convertView.findViewById(R.id.list_item_matching_sensor);
            holder.tvAlg = (TextView) convertView.findViewById(R.id.list_item_matching_algorithm);
            holder.Name = (TextView) convertView.findViewById(R.id.list_item_matching_gesture_name);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Results results = resultsList.get(position);
        float fitRate = results.getFitRate();
        if(position == 0){
            if(fitRate < APPROVED){
                setHolderColors(holder, approvedColor, approvedTextColor);

                GestureName = results.getNameOfGesture();

            }else if(fitRate >= APPROVED && fitRate < MAY_BE_APPROVED){
                setHolderColors(holder, mayBeApprovedColor, mayBeApprovedTextColor);
                //Log.i("GESTURE OUTPUT", "" + results.getNameOfGesture());

                GestureName = results.getNameOfGesture();
            }
        }else {
            setHolderColors(holder, normalColor, normalTextColor);
        }
        holder.tvFitRate.setText(Integer.toString(Math.round(fitRate)));
        holder.tvSensor.setText(getSensorText(results.getSensorType()));
        holder.tvAlg.setText(getAlgText(results.getAlgorithmType()));

        holder.Name.setText(results.getNameOfGesture());


        return convertView;
    }

    public String getGestureName(){
        Iterator<Results> iter = resultsList.iterator();
        Results result;

        result = iter.next();

        return result.getNameOfGesture();
    }

    private class ViewHolder{
        public ViewHolder(){}
        LinearLayout llLayout;
        TextView tvFitRate;
        TextView tvSensor;
        TextView tvAlg;
        TextView Name;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();

        Iterator<Results> iter = resultsList.iterator();
        Results result;
        while (iter.hasNext()) {
            result = iter.next();
            strBuilder.append(String.valueOf(result.getFitRate()) + "\t");
            strBuilder.append(getAlgText(result.getAlgorithmType()) + "\t");
            strBuilder.append(getSensorText(result.getSensorType()) + "\t");
            strBuilder.append(result.getNameOfGesture() + "\n");
        }
        return strBuilder.toString();
    }
}
