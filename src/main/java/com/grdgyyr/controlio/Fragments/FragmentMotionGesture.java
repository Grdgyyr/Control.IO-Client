package com.grdgyyr.controlio.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;
import com.github.nisrulz.sensey.TouchTypeDetector;
import com.github.nisrulz.sensey.WaveDetector;
import com.grdgyyr.controlio.Utilities.Commands;
import com.grdgyyr.controlio.Utilities.Connection;
import com.grdgyyr.controlio.R;


public class FragmentMotionGesture extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ShakeDetector.ShakeListener shakeListener;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentMotionGesture() {
        // Required empty public constructor
    }

    public static FragmentMotionGesture newInstance() {
        FragmentMotionGesture fragment = new FragmentMotionGesture();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Sensey.getInstance().init(getContext());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        shakeListener = new ShakeDetector.ShakeListener() {
            @Override public void onShakeDetected() {
                //Toast.makeText(getActivity(), "SHAKE!!!", Toast.LENGTH_LONG).show();
                Log.i("Control.IO.Gesture", "Shake");
                Connection.Send(Commands.Shake);
            }

            @Override public void onShakeStopped() {
                // Shake stopped, do something
            }

        };

        TouchTypeDetector.TouchTypListener touchTypListener = new TouchTypeDetector.TouchTypListener() {
            @Override public void onTwoFingerSingleTap() {
                Connection.Send(Commands.TwoTap);
                Log.i("Control.IO.Gesture", "TWO FINGER TAP");
            }

            @Override public void onThreeFingerSingleTap() {
                Connection.Send(Commands.ThreeTap);
                Log.i("Control.IO.Gesture", "THREE TAP");
            }


            @Override public void onDoubleTap() {
                Connection.Send(Commands.DoubleTap);
                Log.i("Control.IO.Gesture", "DOUBLE TAP");
            }

            @Override public void onScroll(int scrollDirection) {}


            @Override public void onSingleTap() {
                Connection.Send(Commands.SingleTap);
                Log.i("Control.IO.Gesture", "SINGLE TAP");
            }

            @Override public void onLongPress() {
                Connection.Send(Commands.LongPress);
                Log.i("Control.IO.Gesture", "LONG PRESS");
            }

            @Override public void onSwipe(int swipeDirection) {
                switch (swipeDirection) {
                    case TouchTypeDetector.SWIPE_DIR_UP:
                        Connection.Send(Commands.SwipeUp);
                        Log.i("Control.IO.Gesture", "SWIPE UP");
                        break;
                    case TouchTypeDetector.SWIPE_DIR_DOWN:
                        Connection.Send(Commands.SwipeDown);
                        Log.i("Control.IO.Gesture", "SWIPE DOWN");
                        break;
                    case TouchTypeDetector.SWIPE_DIR_LEFT:
                        Connection.Send(Commands.SwipeLeft);
                        Log.i("Control.IO.Gesture", "SWIPE LEFT");
                        break;
                    case TouchTypeDetector.SWIPE_DIR_RIGHT:
                        Connection.Send(Commands.SwipeRight);
                        Log.i("Control.IO.Gesture", "SWIPE RIGHT");
                        break;
                    default:
                        //do nothing
                        break;
                }
            }


        };

        WaveDetector.WaveListener waveListener = new WaveDetector.WaveListener() {
            @Override public void onWave() {
                Log.i("Control.IO.Gesture", "wave");
                Connection.Send(Commands.Wave);
            }
        };

        Sensey.getInstance().startWaveDetection(waveListener);
        Sensey.getInstance().startTouchTypeDetection(getContext(),touchTypListener);
        Sensey.getInstance().startShakeDetection(20,10,shakeListener);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getResources().getString(R.string.title_gesture));
        return inflater.inflate(R.layout.fragment_motion_gesture, container, false);



    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Sensey.getInstance().stopTouchTypeDetection();
        Sensey.getInstance().stopShakeDetection(shakeListener);
        Sensey.getInstance().stop();
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
