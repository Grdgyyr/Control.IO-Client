package com.grdgyyr.controlio.Fragments.Gyromouse;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.grdgyyr.controlio.Utilities.Connection;
import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.Utilities.Settings;
import com.grdgyyr.controlio.Fragments.ActivityMain;

public class GyroMouseFragment extends Fragment {
    private static final String IP = "com.grdgyyr.controlio.ip";
    private static final String PORT = "com.grdgyyr.controlio.port";

    private String ipStr;
    private int port;

    private OnMotionChangedListener OnAcceleromerMotionChanged = new OnAcceleromerMotionChanged();
    private OnCalibrationFinishedListener OnCalibrationFinished = new OnCalibrationFinished();
    private OnMotionChangedListener OnGyroscopeMotionChanged = new OnGyroscopeMotionChanged();
    private View.OnTouchListener OnTouchButtonFocus = new OnTouchButtonFocus();
    private View.OnTouchListener OnTouchButtonLeft = new OnTouchButtonLeft();
    private View.OnTouchListener OnTouchButtonRight = new OnTouchButtonRight();
    private View.OnTouchListener OnTouchWheel = new OnTouchWheel();
    private boolean _anyKeyPressedWhileFocusOn = false;
    Button _buttonFocus;
    Button _keyboard;
    View _buttonLeft;
    View _buttonRight;
    Button _buttonWheel;
    private boolean _focusOn = false;
    private float _lastWheelPixel = -1.0f;
    private boolean _wheelScrolling = false;
    private byte _wheelStep = (byte) 0;
    private OrientationEventListener myOrientationEventListener;

    private OnFragmentInteractionListener mListener;

    public GyroMouseFragment() {}

    public static GyroMouseFragment newInstance(String Ip, int Port) {
        GyroMouseFragment fragment = new GyroMouseFragment();
        Bundle args = new Bundle();
        args.putString(IP, Ip);
        args.putInt(PORT, Port);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ipStr = getArguments().getString(IP);
            port = getArguments().getInt(PORT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getResources().getString(R.string.title_gyromouse));

        return inflater.inflate(R.layout.fragment_gyromouse, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getResources().getString(R.string.title_gyromouse));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Settings.LoadSettings(getActivity().getApplicationContext());

        _buttonLeft = (View) getView().findViewById(R.id.btn_left_click);
        _buttonLeft.setOnTouchListener(this.OnTouchButtonLeft);
        _buttonRight = (View)getView().findViewById(R.id.btn_right_click);
        _buttonRight.setOnTouchListener(this.OnTouchButtonRight);
        _buttonWheel = (Button)getView().findViewById(R.id.btnScroll);
        _buttonWheel.setOnTouchListener(this.OnTouchWheel);
        _buttonFocus = (Button)getView().findViewById(R.id.btnSens);
        _buttonFocus.setOnTouchListener(this.OnTouchButtonFocus);
        _keyboard = (Button)getView().findViewById(R.id.btnKeyboard);
        _keyboard.setOnClickListener(showKeyboard);


        getActivity().getWindow().addFlags(128);
        if (Settings.getFISRT_USE()) {
            AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getActivity());
            myAlertDialog.setTitle(R.string.app_name);
            myAlertDialog.setMessage("Calibration sensor needs to be calibrated, do you want to calibrate now? just place your phone over a flat surface and click OK");
            myAlertDialog.setPositiveButton("OK", new Calibrate());
            myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });
            myAlertDialog.show();
        }
    }

    public View.OnClickListener showKeyboard = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            ActivityMain act = (ActivityMain)getActivity();
            act.keyboardSend = true;
        }
    };

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
        ReleaseMotionListeners();
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    class OnTouchWheel implements View.OnTouchListener {
        OnTouchWheel() {
        }

        //        @TargetApi(9)
        public boolean onTouch(View v, MotionEvent event) {
            if (GyroMouseFragment.this._focusOn) {
                GyroMouseFragment.this._anyKeyPressedWhileFocusOn = true;
            }
            if (event.getAction() == 0) {
                //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbuttonclick));
            } else if (event.getAction() == 1) {
                if (GyroMouseFragment.this._wheelScrolling) {
                    GyroMouseFragment.this._lastWheelPixel = -1.0f;
                    GyroMouseFragment.this._wheelScrolling = false;
                    //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbutton));
                } else {
                    Connection.Send("wheel|up");
                    //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbutton));
                }
            } else if (event.getAction() == 2) {
                MotionEvent.PointerCoords coordinates = new MotionEvent.PointerCoords();
                event.getPointerCoords(event.getPointerCount() - 1, coordinates);
                if (GyroMouseFragment.this._lastWheelPixel == -1.0f) {
                    GyroMouseFragment.this._lastWheelPixel = coordinates.y;
                } else if (Math.abs(coordinates.y - GyroMouseFragment.this._lastWheelPixel) > ((float) Settings.getMIN_WHEEL_PIXELS())) {
                    float delta = coordinates.y - GyroMouseFragment.this._lastWheelPixel;
                    Connection.Send("wheel|" + String.valueOf(delta));
                    GyroMouseFragment.this._lastWheelPixel = coordinates.y;
                    GyroMouseFragment.this._wheelScrolling = true;
                    if (GyroMouseFragment.this._wheelStep == (byte) 0) {
                        if (delta < 0.0f) {
                            //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbuttonup1));
                        } else {
                            //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbuttondown1));
                        }
                    }
                    if (GyroMouseFragment.this._wheelStep == (byte) 1) {
                        if (delta < 0.0f) {
                            //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbuttonup2));
                        } else {
                            //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbuttondown2));
                        }
                    }
                    if (GyroMouseFragment.this._wheelStep == (byte) 2) {
                        if (delta < 0.0f) {
                            //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbuttonup3));
                        } else {
                            //GyroMouseFragment.this._buttonWheel.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.midbuttondown3));
                        }
                    }
                    if (GyroMouseFragment.this._wheelStep == (byte) 2) {
                        GyroMouseFragment.this._wheelStep = (byte) 0;
                    } else {
                        GyroMouseFragment mainActivity = GyroMouseFragment.this;
                        mainActivity._wheelStep = (byte) (mainActivity._wheelStep + 1);
                    }
                }
            }
            return true;
        }
    }
    class OnTouchButtonRight implements View.OnTouchListener {
        OnTouchButtonRight() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (GyroMouseFragment.this._focusOn) {
                GyroMouseFragment.this._anyKeyPressedWhileFocusOn = true;
            }
            if (event.getAction() == 0) {
                GyroMouseFragment.this.SendRightDown();
            } else if (event.getAction() == 1) {
                GyroMouseFragment.this.SendRightUp();
            }
            return true;
        }
    }

    class OnTouchButtonLeft implements View.OnTouchListener {
        OnTouchButtonLeft() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (GyroMouseFragment.this._focusOn) {
                GyroMouseFragment.this._anyKeyPressedWhileFocusOn = true;
            }
            if (event.getAction() == 0) {
                GyroMouseFragment.this.SendLeftDown();
            } else if (event.getAction() == 1) {
                GyroMouseFragment.this.SendLeftUp();
            }
            return true;
        }
    }

    class OnTouchButtonFocus implements View.OnTouchListener {
        OnTouchButtonFocus() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 0) {
                //GyroMouseFragment.this._buttonFocus.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.focusclick));
                GyroMouseFragment.this._focusOn = true;
            } else if (event.getAction() == 1) {
                if (GyroMouseFragment.this._anyKeyPressedWhileFocusOn) {
                    GyroMouseFragment.this._anyKeyPressedWhileFocusOn = false;
                } else if (Settings.getSWITCH_BUTTONS()) {
                    Connection.Send("down|right");
                    Connection.Send("up|right");
                } else {
                    Connection.Send("down|left");
                    Connection.Send("up|left");
                }
                //GyroMouseFragment.this._buttonFocus.setImageDrawable(GyroMouseFragment.this.getResources().getDrawable(R.drawable.focus));
                GyroMouseFragment.this._focusOn = false;
            }
            return true;
        }
    }

    class Calibrate implements DialogInterface.OnClickListener {
        Calibrate() {
        }

        public void onClick(DialogInterface arg0, int arg1) {
            MotionProviderGyroscope.Calibrate();
            Settings.setFISRT_USE(false);
        }
    }

    class OnCalibrationFinished implements OnCalibrationFinishedListener {
        OnCalibrationFinished() {
        }

        public void OnCalibrationFinished() {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Calibration OK", Toast.LENGTH_LONG).show();
        }
    }

    class OnAcceleromerMotionChanged implements OnMotionChangedListener {
        OnAcceleromerMotionChanged() {
        }

        public void OnMotionChanged(float x, float y) {
        }
    }

    class OnGyroscopeMotionChanged implements OnMotionChangedListener {
        OnGyroscopeMotionChanged() {
        }

        public void OnMotionChanged(float x, float y) {
            float motionFactor = (float) Settings.getMOTION_FACTOR();
            if (GyroMouseFragment.this._focusOn) {
                motionFactor /= 8.0f;
            }
            float finalX = x * motionFactor;
            float finalY = y * motionFactor;
            if (Math.abs(finalX) > ((float) Settings.getMIN_MOVEMENT()) || Math.abs(finalY) > ((float) Settings.getMIN_MOVEMENT())) {
                String xStr = Float.toString(finalX);
                Connection.Send(new StringBuilder(String.valueOf(xStr)).append("|").append(Float.toString(finalY)).toString());
            }
        }
    }

    public void onResume() {
        super.onResume();
        AssignMotionListeners();
    }

    public void onPause() {
        super.onPause();
        ReleaseMotionListeners();
        boolean _startActivity = false;
        if (!_startActivity) {
            //getActivity().finish();
        }
    }

    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.layout.menu, menu);
//        return true;
//    }
//
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_calibrate:
//                MotionProviderGyroscope.Calibrate();
//                return true;
//            case R.id.menu_settings:
//                this._startActivity = true;
//                startActivity(new Intent(this, SettingsActivity.class));
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    private void AssignMotionListeners() {
        if (this.myOrientationEventListener != null && this.myOrientationEventListener.canDetectOrientation()) {
            this.myOrientationEventListener.enable();
        }
        MotionProviderGyroscope.SetOnMotionChanged(this.OnGyroscopeMotionChanged);
        MotionProviderGyroscope.SetOnCalibrationFinished(this.OnCalibrationFinished);
        MotionProviderGyroscope.RegisterEvents(getActivity().getApplicationContext());
    }

    private void ReleaseMotionListeners() {
        if (this.myOrientationEventListener != null) {
            this.myOrientationEventListener.disable();
        }
        MotionProviderGyroscope.ReleaseCalibrationListener();
        MotionProviderGyroscope.ReleaseMotionListener();
        MotionProviderGyroscope.UnregisterEvents(getActivity().getApplicationContext());
    }

    private void ShowToast(final String str) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Calibration OK", Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 25) {
            if (Settings.getSWITCH_VOLUME_BUTTONS_RAISE_CLICK()) {
                SendLeftDown();
            } else {
                SendVolumeDown();
            }
        } else if (keyCode != 24) {
            return getActivity().onKeyDown(keyCode, event);

        } else {
            if (Settings.getSWITCH_VOLUME_BUTTONS_RAISE_CLICK()) {
                SendRightDown();
            } else {
                SendVolumeUp();
            }
        }
        if (keyCode == 24 || keyCode == 25) {
            return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 25) {
            if (Settings.getSWITCH_VOLUME_BUTTONS_RAISE_CLICK()) {
                SendLeftUp();
            }
        } else if (keyCode != 24) {
            return getActivity().onKeyUp(keyCode, event);
        } else {
            if (Settings.getSWITCH_VOLUME_BUTTONS_RAISE_CLICK()) {
                SendRightUp();
            }
        }
        if (keyCode == 24 || keyCode == 25) {
            return true;
        }
        return false;
    }

    private void SendVolumeUp() {
        Connection.Send("volume|up");
    }

    private void SendVolumeDown() {
        Connection.Send("volume|down");
    }

    private void SendLeftUp() {
        if (Settings.getSWITCH_BUTTONS()) {
            Connection.Send("up|right");
        } else {
            Connection.Send("up|left");
        }
        //this._buttonLeft.setImageDrawable(getResources().getDrawable(R.drawable.leftbutton));
    }

    private void SendLeftDown() {
        if (Settings.getSWITCH_BUTTONS()) {
            Connection.Send("down|right");
        } else {
            Connection.Send("down|left");
        }
        //this._buttonLeft.setImageDrawable(getResources().getDrawable(R.drawable.leftbuttonclick));
    }

    private void SendRightUp() {
        if (Settings.getSWITCH_BUTTONS()) {
            Connection.Send("up|left");
        } else {
            Connection.Send("up|right");
        }
        //this._buttonRight.setImageDrawable(getResources().getDrawable(R.drawable.rightbutton));
    }

    private void SendRightDown() {
        if (Settings.getSWITCH_BUTTONS()) {
            Connection.Send("down|left");
        } else {
            Connection.Send("down|right");
        }
        //this._buttonRight.setImageDrawable(getResources().getDrawable(R.drawable.rightbuttonclick));
    }
}
