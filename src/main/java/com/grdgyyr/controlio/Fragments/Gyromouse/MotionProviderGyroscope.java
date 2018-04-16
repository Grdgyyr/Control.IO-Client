package com.grdgyyr.controlio.Fragments.Gyromouse;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.grdgyyr.controlio.Utilities.Settings;

import java.util.Date;
import java.util.List;

public class MotionProviderGyroscope implements SensorEventListener {
    private static Boolean _calibrating = Boolean.valueOf(false);
    private static float[] _calibratingX;
    private static float[] _calibratingY;
    private static OnCalibrationFinishedListener _calibrationFinishedListener;
    private static int _calibrationSamples = 50;
    private static int _calibrationStep = -1;
    private static long _lastSensorChange;
    private static boolean _loaded = false;
    private static OnMotionChangedListener _motionListener;
    private static MotionProviderGyroscope _self = new MotionProviderGyroscope();

    public static void SetOnCalibrationFinished(OnCalibrationFinishedListener listener) {
        _calibrationFinishedListener = listener;
    }

    public static void ReleaseCalibrationListener() {
        _calibrationFinishedListener = null;
    }

    public static void SetOnMotionChanged(OnMotionChangedListener listener) {
        _motionListener = listener;
    }

    public static void ReleaseMotionListener() {
        _motionListener = null;
    }

    public static void Calibrate() {
        _calibrating = Boolean.valueOf(true);
    }

    public static void RegisterEvents(Context context) {
        if (!_loaded) {
            SensorManager sm = (SensorManager) context.getSystemService("sensor");
            List<Sensor> sensors = sm.getSensorList(4);
            if (sensors.size() > 0) {
                sm.registerListener(_self, (Sensor) sensors.get(0), 0);
                _loaded = true;
            }
        }
    }

    public static void UnregisterEvents(Context context) {
        ((SensorManager) context.getSystemService("sensor")).unregisterListener(_self);
        _loaded = false;
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public void onSensorChanged(SensorEvent event) {
        GyroscopeChanged(event);
    }

    private void GyroscopeChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[2];
        if (_calibrating.booleanValue()) {
            ComputeCalibration(x, y);
            return;
        }
        long now = new Date().getTime();
        if (now - _lastSensorChange > ((long) Settings.getMIN_MILLIS_TOPDATE())) {
            float finalX = x - Settings.getCALIBRATION_DELTAX();
            float finalY = y - Settings.getCALIBRATION_DELTAY();
            if (_motionListener != null) {
                _motionListener.OnMotionChanged(finalX, finalY);
            }
            _lastSensorChange = now;
        }
    }

    private void ComputeCalibration(float currentX, float currentY) {
        int i = 0;
        if (_calibrationStep == -1) {
            _calibratingX = new float[_calibrationSamples];
            _calibratingY = new float[_calibrationSamples];
            _calibrationStep++;
        } else if (_calibrationStep < _calibrationSamples) {
            _calibratingX[_calibrationStep] = currentX;
            _calibratingY[_calibrationStep] = currentY;
            _calibrationStep++;
        } else {
            _calibrating = Boolean.valueOf(false);
            _calibrationStep = -1;
            float totalX = 0.0f;
            for (float currentVal : _calibratingX) {
                totalX += currentVal;
            }
            Settings.setCALIBRATION_DELTAX(totalX / ((float) _calibrationSamples));
            float totalY = 0.0f;
            float[] fArr = _calibratingY;
            while (i < fArr.length) {
                totalY += fArr[i];
                i++;
            }
            Settings.setCALIBRATION_DELTAY(totalY / ((float) _calibrationSamples));
            if (_calibrationFinishedListener != null) {
                _calibrationFinishedListener.OnCalibrationFinished();
            }
        }
    }
}
