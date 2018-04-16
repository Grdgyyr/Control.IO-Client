package com.grdgyyr.controlio.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

public class RecognitionToolFragmentAdapter extends FragmentPagerAdapter {

    private final String[] AlgName = {"DTW", "NN", "SVM"};

    public RecognitionToolFragmentAdapter(FragmentManager fm){super(fm);}

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new RecognitionToolDTW();
            case 1:
                return new RecognitionToolNN();
            case 2:
                return new RecognitionToolSVM();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return AlgName[position];
    }

}
