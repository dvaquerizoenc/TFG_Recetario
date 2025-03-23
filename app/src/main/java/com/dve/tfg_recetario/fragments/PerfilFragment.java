package com.dve.tfg_recetario.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilFragment extends Fragment {

    private TextView cerrarSesion;

    public PerfilFragment() {
        // Required empty public constructor
    }

    public static PerfilFragment newInstance() {
        PerfilFragment fragment = new PerfilFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cerrarSesion = view.findViewById(R.id.cerrar_sesion);

        cerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

    }
}