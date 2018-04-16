package com.grdgyyr.controlio.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.github.nisrulz.sensey.Sensey;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.grdgyyr.controlio.Camera.StreamCameraActivity;
import com.grdgyyr.controlio.Microphone.ActivityMicrophone;
import com.grdgyyr.controlio.SensorDataHandler.SensorData;
import com.grdgyyr.controlio.Utilities.Commands;
import com.grdgyyr.controlio.Utilities.Connection;
import com.grdgyyr.controlio.R;
import com.grdgyyr.controlio.Fragments.Gyromouse.GyroMouseFragment;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentMain.OnFragmentInteractionListener,
        FragmentTouchpad.OnFragmentInteractionListener, GyroMouseFragment.OnFragmentInteractionListener,
        FragmentMotionGesture.OnFragmentInteractionListener, FragmentRating.OnFragmentInteractionListener,
        FragmentPresentation.OnFragmentInteractionListener, FragmentRecognitionTool.OnFragmentInteractionListener,
        FragmentHome.OnFragmentInteractionListener {

    private static final String PATTERN =
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";


    private String ConErrorStr = "";
    private static boolean conState = false;
    public boolean keyboardSend = false;

    private FragmentManager fragmentManager;

    private static int Port;
    private static String Ip;


    SensorData sensorData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, FragmentMain.newInstance("", 0)).commit();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fragmentManager.findFragmentById(R.id.flContent) == null) {
            //fragmentManager.beginTransaction().remove(fragmentManager.findFragmentById(R.id.flContent)).commit();
            fragmentManager.beginTransaction().replace(R.id.flContent, FragmentHome.newInstance()).commit();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fragmentManager.findFragmentById(R.id.flContent) != null) {
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentById(R.id.flContent)).commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Connection.Send("Disconnect");
    }

    public void verifyConnection(String ip, int port) {
        //String ipStr = txtIp.getText().toString();

        boolean isAddressInvalid = false;
        boolean isPortInvalid = false;

        if (!isValidAddress(ip)) {
            ip = null;
            isAddressInvalid = true;
        }

        if (!isPortValid(port)) {
            port = -1;
            isPortInvalid = true;
        }

        if (isAddressInvalid && isPortInvalid) {
            ConErrorStr = "Invalid address and port!";
        } else if (isAddressInvalid || isPortInvalid) {
            ConErrorStr = "Invalid address or port";
        }
        if (!ConErrorStr.equals("")) {
            Toast.makeText(ActivityMain.this, ConErrorStr, Toast.LENGTH_LONG).show();
            ConErrorStr = "";
        } else {
            if (port != -1) {
                Port = port;
            }

            if (ip != null) {
                Ip = ip;
            }
            Log.i("Control.IO.Connection", "ip: " + ip + " port: " + port);
            conState = true;
            connnectBtn(ip, port);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyboardSend) {
            Log.i("key pressed", ((char) event.getUnicodeChar()) + "|" + event.getUnicodeChar());
            if (event.getUnicodeChar() == 0)
                Connection.Send("delete");
            else if (event.getUnicodeChar() == 10)
                Connection.Send("enter");
            else
                Connection.Send("keyboard|" + event.getUnicodeChar());
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            } else {

                String[] connection = result.getContents().split(":");

                String ip = connection[0].toString();
                int port = Integer.parseInt(connection[1]);

                new FragmentMain().setTxtCon(ip, port);

                verifyConnection(connection[0], Integer.parseInt(connection[1]));
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void connnectBtn(String ip, int port) {
        createUdp(ip, port);

        //Toast.makeText(this, "Connection Successful", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Setup onTouchEvent for detecting type of touch gesture
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    public void setCon(boolean con) {
        conState = con;
    }

    private void createUdp(final String ip, final int port) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket udpSocket = new DatagramSocket();
                    udpSocket.connect(new InetSocketAddress(ip, port));

                    if (udpSocket != null) {
                        connectUdp(udpSocket);
                        Log.i("Socket:", "socket created...");
                        Connection.Send(Commands.GetDeviceInfo(port));
                        Connection.Send("GESTUREARRAY" + Commands.getArrayGestures());


                        return;
                    }


                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    protected void connectUdp(DatagramSocket socket) {
        Connection.CreateConnection(socket);
    }


    private boolean isPortValid(int server_port) {
        boolean result = true;
        if (server_port < 0 || server_port > 65535) {
            return false;
        }
        return result;
    }

    private boolean isValidAddress(String ip) {
        boolean result = true;


        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(ip);
        if (!matcher.matches()) {
            return false;
        }

        return result;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {


            return true;
        } else if (id == R.id.action_debug) {
            //Intent intent = new Intent(this, ActivityGestureRecognizer.class);
            //startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentById(R.id.flContent) != null) {
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentById(R.id.flContent)).commit();
        }


        if (conState) {

            switch (id){
                case R.id.nav_gesture:
                    Intent intent = new Intent(this, ActivityGestureRecognizer.class);
                    startActivity(intent);
                    break;
                case R.id.nav_mouse:
                    fragmentManager.beginTransaction().replace(R.id.flContent, GyroMouseFragment.newInstance(Ip, Port)).commit();
                    break;
                case R.id.nav_touchpad:
                    fragmentManager.beginTransaction().replace(R.id.flContent, FragmentTouchpad.newInstance(Ip, Port)).commit();
                    break;
                case R.id.nav_home:
                    fragmentManager.beginTransaction().replace(R.id.flContent, FragmentHome.newInstance()).commit();
                    break;
                case R.id.nav_rating:
                    fragmentManager.beginTransaction().replace(R.id.flContent, FragmentRating.newInstance()).commit();
                    break;
                case R.id.nav_present:
                    fragmentManager.beginTransaction().replace(R.id.flContent, FragmentPresentation.newInstance()).commit();
                    break;
                case R.id.nav_connection:
                    fragmentManager.beginTransaction().replace(R.id.flContent, FragmentMain.newInstance(Ip, Port)).commit();
                    break;
                case R.id.nav_camera:
                    Intent intentCam = new Intent(this, StreamCameraActivity.class);
                    startActivity(intentCam);
                    break;
                case R.id.nav_mic:
                    Intent intentMic = new Intent(this, ActivityMicrophone.class);
                    intentMic.putExtra("CONNECTION_IP", Ip);
                    intentMic.putExtra("CONNECTION_PORT", Port);
                    startActivity(intentMic);
                    break;
            }

        } else
            Toast.makeText(ActivityMain.this, "Connection not established", Toast.LENGTH_LONG).show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }


}
