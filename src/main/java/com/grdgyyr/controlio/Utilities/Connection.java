package com.grdgyyr.controlio.Utilities;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Connection {
    private static DatagramSocket _socket = null;
    private static String command;

    public static void CreateConnection(DatagramSocket socket) {

        _socket = socket;
        Log.i("Socket:", "socket connected...");
    }

    public synchronized static void Send(String str) {
        if (_socket != null && _socket.isConnected() && !_socket.isClosed()) {
            try {
                command = str;
                byte[] bytes = str.getBytes("UTF-8");
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                _socket.send(packet);

            } catch (Exception e) {

                e.printStackTrace();
                Log.i("air", e.getMessage());
            }
        }
    }

    public String getCommand(){
        return command;
    }

    public static boolean IsConnected() {
        if (_socket == null)
            return false;
        return _socket.isConnected();
    }

    public static void Disconnect() {
        if (_socket == null)
            return;
//        if (_socket.isConnected()) {
//            Send(Commands.Bye);
//            // _socket.disconnect();
//        }
        _socket.close();

    }
}
