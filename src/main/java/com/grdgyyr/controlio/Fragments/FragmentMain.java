package com.grdgyyr.controlio.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.grdgyyr.controlio.Utilities.Commands;
import com.grdgyyr.controlio.Utilities.Connection;
import com.grdgyyr.controlio.R;


public class FragmentMain extends Fragment {

    private static EditText txtIp, txtPort, txtMac, txtDevicename;
    private static TextView txtConStat;

    private static String ipStr;
    private static int port;

    private OnFragmentInteractionListener mListener;

    public FragmentMain() {
        // Required empty public constructor
    }

    public static FragmentMain newInstance(String Ip, int Port) {
        FragmentMain fragment = new FragmentMain();
        ipStr = Ip;
        port = Port;

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.title_connection));

        txtIp = (EditText) getView().findViewById(R.id.txtIp);
        txtPort = (EditText) getView().findViewById(R.id.txtPort);
        txtConStat = (TextView) getView().findViewById(R.id.txtConStat);

        txtMac = (EditText) getView().findViewById(R.id.txtMac);
        txtDevicename = (EditText) getView().findViewById(R.id.txtDevice);

        Button btnConnect = (Button) getView().findViewById(R.id.btnConnect);
        Button btnDisconnect = (Button) getView().findViewById(R.id.btnDisconnect);
        Button btnQr = (Button) getView().findViewById(R.id.btnQr);

        final ActivityMain m = new ActivityMain();

        if(ipStr != "" & port != 0){
            txtIp.setText(ipStr.toString());
            txtPort.setText(Integer.toString(port));
            txtConStat.setText("Connected");

            String[] info = new String[2];

            info = Commands.GetDeviceInfo(port).split("\\|");

            txtMac.setText(info[1]);
            txtDevicename.setText(info[2]);
        }

        btnQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Connection.Send(Commands.Disconnect);
                Connection.Disconnect();
                txtPort.setText("");
                txtIp.setText("");
                txtConStat.setText("None");
                new ActivityMain().setCon(false);
                Toast.makeText(getActivity(), "Disconnect Successful", Toast.LENGTH_LONG).show();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String IP = "";
                int PORT = 0;

                if (checkEditText(txtIp) && checkEditText(txtPort)) {
                    IP = txtIp.getText().toString();
                    PORT = Integer.parseInt(txtPort.getText().toString());
                    m.verifyConnection(IP, PORT);
                    txtConStat.setText("Connected");
                    txtPort.setText(Integer.toString(PORT));
                    txtIp.setText(IP);


                }

            }

        });




    }


    private boolean checkEditText(EditText edit) {
        boolean result = true;
        String editStr = edit.getText().toString();
        if (editStr == null || editStr.equals("")) {
            return false;
        }
        return result;
    }

    public void setTxtCon(String ip, int port){
        txtIp.setText(ip);
        txtPort.setText(Integer.toString(port));
        txtConStat.setText("Connected");
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //ipStr = getArguments().getString(IP);
            //port = getArguments().getInt(PORT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(getResources().getString(R.string.title_connection));
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
