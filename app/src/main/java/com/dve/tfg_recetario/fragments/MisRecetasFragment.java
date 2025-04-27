package com.dve.tfg_recetario.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.MiRecetaActivity;

public class MisRecetasFragment extends Fragment {

    private ImageButton btnAddRecipe;

    public MisRecetasFragment() {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnAddRecipe = view.findViewById(R.id.btn_add_receta);

        btnAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MiRecetaActivity.class);
            startActivity(intent);
        });
    }
}