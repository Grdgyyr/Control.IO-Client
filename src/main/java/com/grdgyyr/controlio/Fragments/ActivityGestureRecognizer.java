package com.grdgyyr.controlio.Fragments;

import android.animation.ValueAnimator;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.grdgyyr.controlio.Dialog.DialogAlert;
import com.grdgyyr.controlio.Dialog.DialogDelete;
import com.grdgyyr.controlio.Dialog.DialogNewGesture;
import com.grdgyyr.controlio.Dialog.DialogResults;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.RecognitionTools.Filters;
import com.grdgyyr.controlio.RecognitionTools.RecognitionManager;
import com.grdgyyr.controlio.RecognitionTools.Utilities;
import com.grdgyyr.controlio.SensorDataHandler.SensorData;
import com.grdgyyr.controlio.SensorDataHandler.SensorFilter;


public class ActivityGestureRecognizer extends AppCompatActivity
        implements SensorEventListener, DialogNewGesture.NewGestureDialogListener,
        DialogInterface.OnDismissListener, DialogResults.IdentifyDialogListener {


    Button bNewGesture;
    Button bIdentify;

    RecognitionManager recManager;

    private int progressStatus = 0;
    Handler progressHandler = new Handler();
    ProgressBar progressBar;
    CountDownTimer mCountDownTimer;

    RecognitionTool toolFragment;
    RelativeLayout contextLayout;
    //Toolbar mToolbar;
    View statusBar;
    Integer toolbarHeight;
    boolean barFlag;

    static ListView lv;


    private final static int updatePeriod = 50; // update loading bar every 50ms
    private final static int TOUCH_OFFSET = 500; // if user want to show toolbar by click on button
    private final String FRAG_TOOL_TAG = "fragTools"; // tag of RecognitionTool fragment
    private final String DIALOG_NEW_GESTURE = "NewGestureDialog";
    private final String DIALOG_IDENTIFY = "IdentifyDialog";
    private final String DIALOG_ALERT = "AlertDialog";

    private SensorManager mSensorManager;
    private Sensor gyroSensor;
    private Sensor accSensor;
    SensorFilter sensorFilter;
    SensorData sensorData;
    List<SensorData> sensorDataList;

    SensorData mData;
    String[] gestureList;
    List<String> your_array_list;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_recognizer);
        Log.d("Activity Recognizer ", " onCreate ");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        statusBar = getWindow().getDecorView();
        barFlag = true; // enable auto-hidding of bars

        toolFragment = new RecognitionTool();

        contextLayout = (RelativeLayout) findViewById(R.id.activity_gesture_recognizer);
        bNewGesture = (Button) findViewById(R.id.button_create_gesture);

        bIdentify = (Button) findViewById(R.id.button_identify);

        lv = (ListView) findViewById(R.id.lvGesture);


        //listeners
        final GestureDetector clickDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                boolean visible = (statusBar.getSystemUiVisibility()
                        & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;

                return false;
            }
        });

        bNewGesture.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startIncrementing(DIALOG_NEW_GESTURE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stopIncrementing();
                }
                //intentCANCELED();
                return clickDetector.onTouchEvent(event);
            }
        });

        bIdentify.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startIncrementing(DIALOG_IDENTIFY);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stopIncrementing();

                }
//                intentOK();
                return clickDetector.onTouchEvent(event);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);

                //Log.i("CLICKED LISTVIEW", "" + selectedItem);
                showDeleteDialog(selectedItem);
            }
        });


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorFilter = new SensorFilter();
        sensorData = new SensorData();
        sensorDataList = new ArrayList<>();

        recManager = RecognitionManager.getInstance();
        recManager.setUsePreviousGesture(false);

        populateSavedGestures();
    }

    public void populateSavedGestures() {
        mData = new SensorData();
        gestureList = mData.getListOfFiles();
        your_array_list = new ArrayList<String>();
        your_array_list = Arrays.asList(gestureList);

        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                your_array_list);

        lv.setAdapter(arrayAdapter);
        //Log.i("LISTVIEW:", "UPDATED");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bIdentify.setEnabled(true);
        bNewGesture.setEnabled(true);

        // retrieve date of toolbar height in case when user left the application
        /*if(toolbarHeight==0){
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            toolbarHeight = (Integer) sharedPref.getInt("com.example.pp.gesturerecognition.toolbarheight",140);
        }*/
    }


    protected void onStop() {
        // when user leaves application save the height of toolbar if was hidden
        // to easily recover its value

        stopSensor(true);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Fragment prevTool = getFragmentManager().findFragmentByTag(FRAG_TOOL_TAG);
        if (prevTool != null) {
            getFragmentManager().beginTransaction().remove(prevTool).commit();
        } else {
            intentCANCELED();
        }
    }

    private void intentOK(String name) {

        //Intent returnIntent = new Intent(this, DataPresentationActivity.class);
        Bundle args = new Bundle();
        //args.putSerializable("ResultHashMap", sensorsData.returnHashMap());
        args.putString("ResultName", name);
        //returnIntent.putExtras(args);
        //setResult(RESULT_OK, returnIntent);
        sensorData.clearData();
        //Activity will not send result until finish has been called
        finish();


    }

    private void intentCANCELED() {

        //Intent returnIntent = new Intent(this, DataPresentationActivity.class);
        //setResult(RESULT_CANCELED,returnIntent);
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gesture_recognizer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // back button on toolbar
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_tools:
                if (getFragmentManager().findFragmentById(contextLayout.getId()) == null) {
                    getFragmentManager().beginTransaction()
                            .add(contextLayout.getId(), toolFragment, FRAG_TOOL_TAG)

                            .commit();
                }

                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    private void startIncrementing(final String buttonName) {
        final Button btnThis;
        sensorData.clearData();

        if (buttonName == DIALOG_NEW_GESTURE) {
            progressBar = (ProgressBar) findViewById(R.id.progressBar_CreateGesture);
            btnThis = bNewGesture;
            bNewGesture.setAlpha(0.5f);
            bIdentify.setEnabled(false);
        } else if (buttonName == DIALOG_IDENTIFY) {
            progressBar = (ProgressBar) findViewById(R.id.progressBar_Identify);
            btnThis = bIdentify;
            bIdentify.setAlpha(0.5f);
            bNewGesture.setEnabled(false);
        } else {
            btnThis = bNewGesture;
        }

        final DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        progressStatus = 0;
        progressBar.setMax(RecognitionManager.gestureDurationMAX);
        progressBar.setProgress(0);

        mCountDownTimer = new CountDownTimer(RecognitionManager.gestureDurationMAX, updatePeriod) {

            public void onTick(long millisUntilFinished) {
                progressStatus += updatePeriod;
                // Update the progress bar
                progressHandler.post(new Runnable() {
                    public void run() {
                        btnThis.setText(String.valueOf(df.format(progressStatus / 1000.0f)));
                        progressBar.setProgress(progressStatus);
                    }
                });
            }

            public void onFinish() {
                progressBar.setProgress(0);
                //stopSensor(false);
            }
        };
        startSensor();
        mCountDownTimer.start();

    }

    synchronized private void stopIncrementing() {
        mCountDownTimer.cancel();
        stopSensor(false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            sensorData.addDataToSensor(type, sensorFilter.getAccLinear(event));
        } else if (type == Sensor.TYPE_GYROSCOPE) {
            sensorData.addDataToSensor(type, sensorFilter.getGyro(event));
        }
    }

    private void startSensor() {
        mSensorManager.registerListener(this, gyroSensor, mSensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accSensor, mSensorManager.SENSOR_DELAY_GAME);
        // SENSOR_DELAY_FASTEST
    }

    private void stopSensor(boolean emergency) {
        try {
            mSensorManager.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        // open dialog only when sensor activity wasn't finished by closig the application
//        // otworz dialog tylko jak uzytkownik zakonczy wciskanie przycisku, a nie przez nagle
//        // wyjscie z aplikacji (stopSensor wykonuje sie w Activity.onStop() )
        if (!emergency) {
            //        if (bNewGesture.getAlpha() == 1f) { // Open Identify Dialog
            if (bIdentify.isEnabled()) {
                bIdentify.setAlpha(1f);
                bIdentify.setText(R.string.button_identify);
                bNewGesture.setEnabled(true);
                if (progressStatus > TOUCH_OFFSET) {
                    if (progressStatus > RecognitionManager.gestureDurationMIN) {
                        int[] size = sensorData.sizeOfData();
                        if (size[1] > RecognitionManager.MIN_ACC_LENGTH) {
                            // everything OK let's start recognizing
                            showIdentifyDialog();
                        } else {
                            // not enough length of sequence
                            showAlertDialog(DialogAlert.TO_SHORT_SEQUENCE, 1, size[1]);
                        }
                    } else {
                        // user holded button to short
                        showAlertDialog(DialogAlert.TO_SHORT_DURATION, 1, -1);
                    }
                }
                //        } else if (bIdentify.getAlpha() == 1f) { // Open NewGesture
            } else if (bNewGesture.isEnabled()) {
                bNewGesture.setAlpha(1f);
                bNewGesture.setText(R.string.button_new_gesture);
                if (RecognitionManager.getInstance().getRepeatGesture() + 1 != sensorDataList.size() &&
                        sensorDataList.size() != 0) { // if not the first and not the last
                    bIdentify.setEnabled(false);
                } else {
                    bIdentify.setEnabled(true);
                }
                if (progressStatus > TOUCH_OFFSET) {
                    if (progressStatus > RecognitionManager.gestureDurationMIN) {
                        int[] size = sensorData.sizeOfData();
                        if (size[1] > RecognitionManager.MIN_ACC_LENGTH) {
                            sensorDataList.add(sensorData);
                            if (RecognitionManager.getInstance().getRepeatGesture() + 1 == sensorDataList.size()) {
                                // if more than one repetition before saving than return average value of those sequences
                                if (RecognitionManager.getInstance().getRepeatGesture() != RecognitionManager.REPEAT_ONCE) {
                                    sensorData = Utilities.getInstance().returnAverageData(sensorDataList);
                                } else {
                                    sensorData = sensorDataList.get(0);
                                }
                                sensorDataList.clear();
                                showNewGestureDialog(sensorData.sizeOfData());
                                bIdentify.setEnabled(true);
                            } else {
                                showAlertDialog(DialogAlert.REPEAT, sensorDataList.size(), size[1]);
                                bIdentify.setEnabled(false);
                            }

                        } else {
                            // not enough length of sequence
                            showAlertDialog(DialogAlert.TO_SHORT_SEQUENCE, 1, size[1]);
                        }
                    } else {
                        // user holded button to short
                        showAlertDialog(DialogAlert.TO_SHORT_DURATION, 1, -1);
                    }
                }
            }
        }
    }

    private void showIdentifyDialog() {
        RecognitionManager.getInstance().setDataToRecognize(sensorData);
        DialogFragment dialog = new DialogResults();
        dialog.show(getFragmentManager(), DIALOG_IDENTIFY);
    }

    private void showDeleteDialog(String gesture) {
        DialogFragment dialog = DialogDelete.newInstance(gesture);
        dialog.show(getFragmentManager(), "deletedialog");
    }


    private void showNewGestureDialog(int[] size) {
        DialogFragment dialog = DialogNewGesture.newInstance(size);
        dialog.show(getFragmentManager(), DIALOG_NEW_GESTURE);
    }

    private void showAlertDialog(int mode, int repetition, int length) {
        DialogFragment dialog = DialogAlert.newInstance(mode, repetition, length);
        dialog.show(getFragmentManager(), DIALOG_ALERT);
    }

    @Override
    public boolean onNewGestureDialogTryToSave(String name) {
        boolean isSuccess = sensorData.saveFileToExternalStorage(name, getBaseContext());
        isSuccess = sensorData.saveReadableFileToExternalStorage(name, getBaseContext()) && isSuccess;
        if (isSuccess)
            populateSavedGestures();
        sensorData.clearData();
        return isSuccess;
    }

    @Override
    public void onNewGestureDialogPositiveClick(String name) {
        intentOK(name);
    }

    @Override
    public void onNewGestureDialogNegativeClick() {
        // I tak onDissmis zostaje wywolany po tym
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        sensorData.clearData();
    }

    @Override
    public boolean tryToSave() {
        return false;
    }


}
