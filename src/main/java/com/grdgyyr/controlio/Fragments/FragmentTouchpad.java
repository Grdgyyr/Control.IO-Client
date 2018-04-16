package com.grdgyyr.controlio.Fragments;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.grdgyyr.controlio.Utilities.Commands;
import com.grdgyyr.controlio.Utilities.Connection;
import com.grdgyyr.controlio.R;

import java.util.Date;


public class FragmentTouchpad extends Fragment {
    private static final String IP = "com.grdgyyr.controlio.ip";
    private static final String PORT = "com.grdgyyr.controlio.port";

    private String ipStr;
    private int port;

    private OnFragmentInteractionListener mListener;

    View touchboard;
    Button leftClick, rightClick;
    EditText ipTxt;
    EditText portTxt;
    Button keyboardBtn;

    private static final int ClickTime = 300;
    private static final int ClickDragTime = 400;
    int _downx0;
    int _downy0;

    int _downx1;
    int _downy1;

    int _lastx0;
    int _lasty0;

    int _lastx1;
    int _lasty1;

    int _x0;
    int _y0;

    int _x1;
    int _y1;

    boolean _motion0;
    boolean _motion1;
    boolean _isClickAndDragging;
    boolean _isPinching;
    boolean _isWheeling;

    Date _dateLastPrimaryClick;
    Date _dateDown0;
    Date _dateDown1;
    int _pixelTolerance;
    int _pixelToleranceSqr;

    public FragmentTouchpad() {}

    public static FragmentTouchpad newInstance(String Ip, int Port) {
        FragmentTouchpad fragment = new FragmentTouchpad();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);

        int width = size.x;
        int height = size.y;

        _pixelTolerance = (int) ((float) (Math.min(width, height)) * 0.01f);
        _pixelToleranceSqr = (int) Math.pow(_pixelTolerance, 2);
        _dateLastPrimaryClick = new Date(0);

        ipTxt = (EditText) getView().findViewById(R.id.txtIp);
        portTxt = (EditText) getView().findViewById(R.id.txtPort);
        keyboardBtn = (Button)  getView().findViewById(R.id.btnKeyboard);
        keyboardBtn.setOnClickListener(showKeyboard);
        touchboard = (View) getView().findViewById(R.id.touch_board);
        touchboard.setOnTouchListener(OnTouchButtonLeft);
        leftClick = (Button) getView().findViewById(R.id.btn_left_click);
        rightClick = (Button) getView().findViewById(R.id.btn_right_click);
        leftClick.setOnClickListener(ButtonClick);
        rightClick.setOnClickListener(ButtonClick);

    }


    private View.OnClickListener ButtonClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            Button btn = (Button) v;
            if (btn == leftClick)
            {
                Connection.Send(Commands.DownLeft);
                Connection.Send(Commands.UpLeft);
            }
            else
            {
                Connection.Send(Commands.DownRight);
                Connection.Send(Commands.UpRight);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(getResources().getString(R.string.title_touchpad));

        View view = inflater.inflate(R.layout.fragment_touchpad, container, false);

//        Button btnKeyboard = (Button) view.findViewById(R.id.btnKeyboard);
//
//        btnKeyboard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
//                View mView = getLayoutInflater().inflate(R.layout.dialog_keyboard, null);
//                EditText txtKeyboard = (EditText) mView.findViewById(R.id.txtKeyboard);
//                Button btnSend = (Button) mView.findViewById(R.id.btnKeyboard);
//                Log.i("Control.IO.UI", "Toggle");
//                mBuilder.setView(mView);
//                AlertDialog dialog = mBuilder.create();
//                dialog.show();
//            }
//        });

        return view;
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
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private int DistanceSqrt(int x1, int y1, int x2, int y2) {
        return Math.abs((int) Math.pow(x2 - x1, 2) + (int) Math.pow(y2 - y1, 2));
    }

    private int Distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(DistanceSqrt(x1, y1, x2, y2));
    }

    private View.OnTouchListener OnTouchButtonLeft = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int action = MotionEventCompat.getActionMasked(event);

//            _tvCoord1.setText(String.valueOf(_x0 + " " + String.valueOf(_y0)));
//            _tvCoord2.setText(String.valueOf(_x1 + " " + String.valueOf(_y1)));

            switch (action) {
                case android.view.MotionEvent.ACTION_MOVE:

                    // Get screen coordinates
                    _x0 = (int) MotionEventCompat.getX(event, 0);
                    _y0 = (int) MotionEventCompat.getY(event, 0);
                    if (event.getPointerCount() > 1) {
                        _x1 = (int) MotionEventCompat.getX(event, 1);
                        _y1 = (int) MotionEventCompat.getY(event, 1);
                    }


                    // Process one finger
                    if (event.getPointerCount() == 1) {


                        // Process Click & Drag
                        Date currentDate = new Date();
                        if (currentDate.getTime() - _dateLastPrimaryClick.getTime() < ClickDragTime) {
                            Connection.Send(Commands.DownLeft);
                            _isClickAndDragging = true;
                        }
                        // Process for left click
                        if (!_motion0) {
                            int dist = DistanceSqrt(_x0, _y0, _downx0, _downy0);

                            if (dist > _pixelToleranceSqr) {
                                _motion0 = true;
                            }
                        }

                        boolean swap = false;
                        float diffx = swap ? _y0 - _lasty0 : _x0 - _lastx0;
                        float diffy = swap ? _x0 - _lastx0 : _y0 - _lasty0;

                        diffx *= 1f;
                        diffy *= 1f;
                        Connection.Send(Commands.GetMouseDeltaString(diffy * -1, diffx * -1));




                    }
                    // Mouse wheel & pinch
                    else {
                        // Pinch
                        int distanceOriginalPinch = Distance(_downx0, _downy0, _downx1, _downy1);
                        int distanceCurrentPinch = Distance(_x0, _y0, _x1, _y1);
                        int deltaPinch = distanceCurrentPinch - distanceOriginalPinch;
                        if (!_isWheeling && Math.abs(deltaPinch) > 10) {
                            _isPinching = true;
                            Connection.Send(Commands.GetZoomDeltaString(deltaPinch));
                            _downx0 = _x0;
                            _downx1 = _x1;
                            _downy0 = _y0;
                            _downy1 = _y1;
                        }

                        // Scroll
                        int yOriginalAverage = (_downy0 + _downy1) / 2;
                        int yCurrentAverage = (_y0 + _y1) / 2;
                        int deltaY = yCurrentAverage - yOriginalAverage;
                        if (!_isPinching && Math.abs(deltaY) > 10) {
                            _isWheeling = true;
                            Connection.Send(Commands.GetWheelDeltaString(deltaY));
                            _downx0 = _x0;
                            _downx1 = _x1;
                            _downy0 = _y0;
                            _downy1 = _y1;
                        }

                        // Process for right click
                        if (!_motion1) {
                            int dist = DistanceSqrt(_x1, _y1, _downx1, _downy1);

                            if (dist > _pixelToleranceSqr) {
                                _motion1 = true;
                            }
                        }
                    }

                    // After process
                    _lastx0 = _x0;
                    _lasty0 = _y0;
                    if (event.getPointerCount() > 1) {
                        _lastx1 = _x1;
                        _lasty1 = _y1;
                    }
                    break;

                // Primary button Click down
                case android.view.MotionEvent.ACTION_DOWN:
                    _x0 = (int) MotionEventCompat.getX(event, 0);
                    _y0 = (int) MotionEventCompat.getY(event, 0);
                    _downx0 = _x0;
                    _downy0 = _y0;
                    _lastx0 = _x0;
                    _lasty0 = _y0;
                    _dateDown0 = new Date();
                    break;

                // Secondary button click down <- Enable Pinch & Wheel
                case android.view.MotionEvent.ACTION_POINTER_DOWN:
                    _x1 = (int) MotionEventCompat.getX(event, 1);
                    _y1 = (int) MotionEventCompat.getY(event, 1);
                    _downx1 = _x1;
                    _downy1 = _y1;
                    _lastx1 = _x1;
                    _lasty1 = _y1;
                    _dateDown1 = new Date();
                    break;

                // Primary Click up
                case android.view.MotionEvent.ACTION_UP:
                    if (!_motion0 && !_isPinching && !_isWheeling) {
                        Date currentDatePrimaryUp = new Date();
                        long diff = currentDatePrimaryUp.getTime() - _dateDown0.getTime();

                        if (diff < ClickTime) {
                            Log.i("air","primary click");
                            Connection.Send(Commands.DownLeft);
                            Connection.Send(Commands.UpLeft);
                            _dateLastPrimaryClick = new Date();
//                            _tvInfo.setText("left click");// Send left click
                        }

                    }
                    _motion0 = false;
                    _x0 = -1;
                    _y0 = -1;
                    _lastx0 = -1;
                    _lasty0 = -1;
                    if (_isClickAndDragging) {
                        _isClickAndDragging = false;
                        Connection.Send(Commands.UpLeft);
                    }

                    _isPinching = false;
                    _isWheeling = false;
                    break;

                // Secondary Click up
                case android.view.MotionEvent.ACTION_POINTER_UP:
                    if (!_motion1 && !_isPinching && !_isWheeling) {
                        Date currentDateSecondaryUp = new Date();
                        long diff = currentDateSecondaryUp.getTime() - _dateDown1.getTime();

                        if (diff < ClickTime) {
                            Connection.Send(Commands.DownRight);
                            Connection.Send(Commands.UpRight);
//                            _tvInfo.setText("right click");// Send right click
                        }
                    }

                    _motion1 = false;
                    _x1 = -1;
                    _y1 = -1;
                    _lastx1 = -1;
                    _lasty1 = -1;
                    break;
            }

            return true;
        }
    };

    public static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                return "Down";
            case MotionEvent.ACTION_MOVE:
                return "Move";
            case MotionEvent.ACTION_POINTER_DOWN:
                return "Pointer Down";
            case MotionEvent.ACTION_UP:
                return "Up";
            case MotionEvent.ACTION_POINTER_UP:
                return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE:
                return "Outside";
            case MotionEvent.ACTION_CANCEL:
                return "Cancel";
        }
        return "";
    }
}
