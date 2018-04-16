package com.grdgyyr.controlio.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.grdgyyr.controlio.Utilities.Commands;
import com.grdgyyr.controlio.Utilities.Connection;
import com.grdgyyr.controlio.R;



public class FragmentPresentation extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    private Button downArrowButton, upArrowButton, f5Button,
            leftArrowButton, rightArrowButton, btn_space, btnEsc, btn_back;

    public FragmentPresentation() {
        // Required empty public constructor
    }


    public static FragmentPresentation newInstance() {
        FragmentPresentation fragment = new FragmentPresentation();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getResources().getString(R.string.title_presentation));

        View rootView = inflater.inflate(R.layout.fragment_presentation, container, false);
        downArrowButton = (Button) rootView.findViewById(R.id.downArrowButton);
        upArrowButton = (Button) rootView.findViewById(R.id.upArrowButton);
        leftArrowButton = (Button) rootView.findViewById(R.id.leftArrowButton);
        rightArrowButton = (Button) rootView.findViewById(R.id.rightArrowButton);
        f5Button = (Button) rootView.findViewById(R.id.f5Button);
        btn_space = (Button) rootView.findViewById(R.id.btn_space);
        btnEsc = (Button) rootView.findViewById(R.id.btnEsc);
        btn_back = (Button) rootView.findViewById(R.id.btn_back);
        downArrowButton.setOnClickListener(this);
        leftArrowButton.setOnClickListener(this);
        upArrowButton.setOnClickListener(this);
        rightArrowButton.setOnClickListener(this);
        f5Button.setOnClickListener(this);
        btn_space.setOnClickListener(this);
        btnEsc.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        return rootView;

        //return inflater.inflate(R.layout.fragment_presentation, container, false);
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.downArrowButton:
                Connection.Send(Commands.ArrowDown);
                break;
            case R.id.leftArrowButton:
                Connection.Send(Commands.Left);
                break;
            case R.id.upArrowButton:
                Connection.Send(Commands.ArrowUp);
                break;
            case R.id.rightArrowButton:
                Connection.Send(Commands.Right);
                break;
            case R.id.f5Button:
                Connection.Send(Commands.Present);
                break;
            case R.id.btn_space:
                Connection.Send(Commands.Forward);
                break;
            case R.id.btnEsc:
                Connection.Send(Commands.Escape);
                break;
            case R.id.btn_back:
                Connection.Send(Commands.Back);
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
