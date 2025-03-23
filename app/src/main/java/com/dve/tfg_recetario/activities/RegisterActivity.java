package com.dve.tfg_recetario.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dve.tfg_recetario.R;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton btnAtras = findViewById(R.id.btn_atras_register);
        Button btnLogin = findViewById(R.id.tvLogin);

        btnAtras.setOnClickListener(view -> {
            finish();
        });

        btnLogin.setOnClickListener(view -> {
            finish();
        });

    }
}