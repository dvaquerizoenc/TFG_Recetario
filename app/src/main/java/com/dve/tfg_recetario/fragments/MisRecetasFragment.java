package com.dve.tfg_recetario.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dve.tfg_recetario.R;

public class MisRecetasFragment extends Fragment {

    public MisRecetasFragment() {
    }


    public static MisRecetasFragment newInstance() {
        MisRecetasFragment fragment = new MisRecetasFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mis_recetas, container, false);
    }
}