/*
 * Based on Peepers StreamCameraActivity
 *
 * Copyright 2013 Foxdog Studios Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grdgyyr.controlio.Camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.grdgyyr.controlio.Fragments.SettingsActivity;
import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.Utilities.AppPreferences;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class StreamCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String PREF_CAMERA = "pref_camera";
    private static final String PREF_FLASH_LIGHT = "pref_flash_light";
    private static final String PREF_JPEG_SIZE = "pref_size";
    private static final String PREF_JPEG_QUALITY = "pref_jpeg_quality";
    private static final int PREF_CAMERA_INDEX_DEF = 0;
    private static final boolean PREF_FLASH_LIGHT_DEF = false;
    private static final int PREF_JPEG_QUALITY_DEF = 40;
    private static final int PREF_PREVIEW_SIZE_INDEX_DEF = -1;
    public static final int STREAM_PORT = 8080;

    private boolean mRunning = false;
    private boolean mPreviewDisplayCreated = false;
    private SurfaceHolder mPreviewDisplay = null;
    private CameraStreamer mCameraStreamer = null;

    private SharedPreferences mPrefs;
    private int mCameraIndex = PREF_CAMERA_INDEX_DEF;
    private boolean mUseFlashLight = PREF_FLASH_LIGHT_DEF;
    private int mJpegQuality = PREF_JPEG_QUALITY_DEF;
    private int mPreviewSizeIndex = PREF_PREVIEW_SIZE_INDEX_DEF;

    private AppPreferences preferences;
    private TextView streamStatusText;

    private String mIpAddress = "";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        preferences = new AppPreferences(PreferenceManager.getDefaultSharedPreferences(this));

        mPreviewDisplay = ((SurfaceView) findViewById(R.id.camera)).getHolder();
        mPreviewDisplay.addCallback(this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(StreamCameraActivity.this);
        mPreviewSizeIndex = getPrefInt(PREF_JPEG_SIZE, PREF_PREVIEW_SIZE_INDEX_DEF);
        mJpegQuality = getPrefInt(PREF_JPEG_QUALITY, PREF_JPEG_QUALITY_DEF);


        tryStartCameraStreamer();
        mIpAddress = tryGetIpV4Address();

        // Animate connect icon
        streamStatusText = (TextView) findViewById(R.id.streamStatusText);
        streamStatusText.setText(getString(R.string.stream_connect));

        updateUi();


    }


    @Override
    protected void onResume() {
        super.onResume();
        mRunning = true;
        updateUi();
        tryStartCameraStreamer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunning = false;
        ensureCameraStreamerStopped();
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        // Ignored
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        mPreviewDisplayCreated = true;
        tryStartCameraStreamer();
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        mPreviewDisplayCreated = false;
        ensureCameraStreamerStopped();
    }

    private void tryStartCameraStreamer() {
        if (mRunning && mPreviewDisplayCreated && mPrefs != null) {
            mCameraStreamer = new CameraStreamer(mCameraIndex, mUseFlashLight, STREAM_PORT, mPreviewSizeIndex, mJpegQuality, mPreviewDisplay);
            mCameraStreamer.start();
        }
    }

    private void ensureCameraStreamerStopped() {
        if (mCameraStreamer != null) {
            mCameraStreamer.stop();
            mCameraStreamer = null;
        }
    }

    private int getPrefInt(final String key, final int defValue) {
        // We can't just call getInt because the preference activity
        // saves everything as a string.
        try {
            return Integer.parseInt(mPrefs.getString(key, null));
        } catch (final NullPointerException e) {
            return defValue;
        } catch (final NumberFormatException e) {
            return defValue;
        }
    }

    private void updateUi() {
        mCameraIndex = getPrefInt(PREF_CAMERA, PREF_CAMERA_INDEX_DEF);
        streamStatusText.setText("http://" + mIpAddress + ":" + STREAM_PORT + "/");
        if (hasFlashLight()) {
            if (mPrefs != null) {
                mUseFlashLight = mPrefs.getBoolean(PREF_FLASH_LIGHT, PREF_FLASH_LIGHT_DEF);
            } else {
                mUseFlashLight = PREF_FLASH_LIGHT_DEF;
            }
        } else {
            mUseFlashLight = false;
        }
    }

    private boolean hasFlashLight() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }





    private static String tryGetIpV4Address()
    {
        try
        {
            final Enumeration<NetworkInterface> en =
                    NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements())
            {
                final NetworkInterface intf = en.nextElement();
                final Enumeration<InetAddress> enumIpAddr =
                        intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements())
                {
                    final  InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        final String addr = inetAddress.getHostAddress().toUpperCase();
                        if (InetAddressUtils.isIPv4Address(addr))
                        {
                            return addr;
                        }
                    } // if
                } // while
            } // for
        } // try
        catch (final Exception e)
        {
            // Ignore
        } // catch
        return null;
    } // tryGetIpV4Address()


}
