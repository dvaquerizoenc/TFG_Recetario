package com.dve.tfg_recetario.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.adaptador.AdaptadorEtiquetasReceta;
import com.dve.tfg_recetario.adaptador.AdaptadorIngredientesReceta;
import com.dve.tfg_recetario.modelo.entidad.Ingrediente;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MiRecetaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_receta);

        RecyclerView rvEtiquetas = findViewById(R.id.rvEtiquetasRecetaMi);
        RecyclerView rvIngredientes = findViewById(R.id.rvIngredientesRecetaMi);
        ImageButton btnAtras = findViewById(R.id.btn_atras_mi);

        btnAtras.setOnClickListener(v -> {
            finish();
        });

        FlexboxLayoutManager layoutManagerEtiquetas = new FlexboxLayoutManager(this);
        layoutManagerEtiquetas.setFlexDirection(FlexDirection.ROW);
        layoutManagerEtiquetas.setFlexWrap(FlexWrap.WRAP);

        rvEtiquetas.setLayoutManager(layoutManagerEtiquetas);

        List<String> listaEtiquetas = new ArrayList<>();

        listaEtiquetas.add("+");

        rvEtiquetas.setAdapter(new AdaptadorEtiquetasReceta(listaEtiquetas));

        GridLayoutManager layoutManagerIngredientes = new GridLayoutManager(this, 2); // 2 columnas

        rvIngredientes.setLayoutManager(layoutManagerIngredientes);

        List<Ingrediente> listaIngredientes = new ArrayList<>();

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setImg("");
        ingrediente.setIngrediente("Add ingredient");
        ingrediente.setCantidad("");
        listaIngredientes.add(ingrediente);

        rvIngredientes.setAdapter(new AdaptadorIngredientesReceta(listaIngredientes));
    }
}