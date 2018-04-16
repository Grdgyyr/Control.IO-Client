package com.grdgyyr.controlio.Fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.grdgyyr.controlio.R;


public class ViewPagerAdapter extends FragmentPagerAdapter {

    String[] sensors;
    String gestureName;
    boolean areSensorsEnabled;

    public ViewPagerAdapter(FragmentManager fragmentManager, Context context, String gestureName){
        super(fragmentManager);

        Resources res = context.getResources();
        sensors = res.getStringArray(R.array.sensor_type);
        this.gestureName = gestureName;
        areSensorsEnabled = gestureName != null ? false : true;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        Bundle args = new Bundle();

        if(areSensorsEnabled){
            if(i==0){
                //fragment = new PageGyroscope();
            }else{
                //fragment = new PageAccelerometer();
            }
        }else{
            if(i==0){
                //fragment = new PageGyroscopeDisabled();
            }else{
                //fragment = new PageAccelerometerDisable();
            }
        }
        //args.putString(DataSensorFragment.SENSOR_DESCRIPTION, gestureName);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return sensors.length;
    }

    public CharSequence getPageTitle(int position){
        return sensors[position];
    }

}
