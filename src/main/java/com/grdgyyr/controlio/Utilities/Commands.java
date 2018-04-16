package com.grdgyyr.controlio.Utilities;

import com.grdgyyr.controlio.SensorDataHandler.SensorData;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class Commands {
    public static final String Zoom = "zoom";
    public static final String Wheel = "wheel";
    public static final String Volume = "volume";
    public static final String In = "in";
    public static final String Out = "out";
    public static final String Up = "up";
    public static final String Down = "down";
    public static final String Primary = "primary";
    public static final String Secondary = "secondary";
    public static final String Right = "right";
    public static final String Left = "left";
    public static final String Separator = "|";
    public static final String Separator2 = "#";
    public static final String Hello = "hola";
    public static final String Shake = "shake";
    public static final String SwipeUp = "swipeup";
    public static final String SwipeDown = "swipedown";
    public static final String SwipeLeft = "swipeleft";
    public static final String SwipeRight = "swiperight";
    public static final String LongPress = "longpress";
    public static final String SingleTap = "singletap";
    public static final String DoubleTap = "doubletap";
    public static final String TwoTap = "twotap";
    public static final String ThreeTap = "threetap";
    public static final String Wave = "wave";
    public static final String Disconnect = "disconnect";
    public static final String Present = "present";
    public static final String Forward = "forward";
    public static final String Back = "back";
    public static final String ArrowUp = "arrowup";
    public static final String ArrowDown = "arrowdown";
    public static final String Escape = "escape";

    public static final String WheelUp = Wheel + Separator + Up;
    public static final String WheelDown = Wheel + Separator + Down;
    public static final String VolumeUp = Volume + Separator + Up;
    public static final String VolumeDown = Volume + Separator + Down;
    public static final String UpRight = Up + Separator + Right;
    public static final String UpLeft = Up + Separator + Left;
    public static final String DownRight = Down + Separator + Right;
    public static final String DownLeft = Down + Separator + Left;
    public static final String DownPrimary = Down + Separator + Primary;
    public static final String UpPrimary = Up + Separator + Primary;
    public static final String DownSecondary = Down + Separator + Secondary;
    public static final String UpSecondary = Up + Separator + Secondary;

    public static final String GetConnectionString() {
        return Hello + Separator + android.os.Build.MANUFACTURER + Separator2 + android.os.Build.MODEL;
    }

    public static final String GetMouseDeltaString(float x, float y) {
        String xStr = Float.toString(x);
        String yStr = Float.toString(y);
        return xStr + Separator + yStr;
    }

    public static final String GetDeviceInfo(int port) {
        String DNAME = android.os.Build.MODEL;
        String MAC = getMacaddress();
        String IP = tryGetIpV4Address();

        return "info" + Separator + DNAME + Separator + MAC + Separator + IP + Separator + port;
    }



    public static String getMacaddress(){
        try {
            // get all the interfaces
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            //find network interface wlan0
            for (NetworkInterface networkInterface : all) {
                if (!networkInterface.getName().equalsIgnoreCase("wlan0")) continue;
                //get the hardware address (MAC) of the interface
                byte[] macBytes = networkInterface.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }


                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //gets the last byte of b
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }



                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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
    }

    public static String getArrayGestures(){
        String Gestures = "";

        SensorData mData = new SensorData();
        String[] gestureList = mData.getListOfFiles();

        for(int x = 0; x < gestureList.length; x++){

            Gestures = Gestures + "|" + gestureList[x];
        }

        return Gestures;
    }

    public static final String GetWheelDeltaString(float delta) {
        return Wheel + Separator + String.valueOf(delta);
    }

    public static final String GetZoomDeltaString(float delta) {
        return Zoom + Separator + String.valueOf(delta);
    }
}
