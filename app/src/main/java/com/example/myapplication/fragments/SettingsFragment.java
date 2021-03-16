package com.example.myapplication.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class SettingsFragment extends Fragment {


    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        String centerNBlack = "<div style='text-align:center' ><span style='color:grey' >  REMEDIC  </span></div>";
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Html.fromHtml(centerNBlack));

        return rootView;
    }
}