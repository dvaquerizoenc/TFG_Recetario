package com.dve.tfg_recetario.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.adaptador.AdaptadorHistorialRecetas;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.entidad.Usuario;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HistorialActivity extends AppCompatActivity {

    ImageButton btnAtras;
    RecyclerView rvRecetasHistorial;
    FirebaseFirestore db;
    FirebaseAuth auth;
    AdaptadorHistorialRecetas adaptadorHistorialRecetas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        btnAtras = findViewById(R.id.btn_atras_historial);
        rvRecetasHistorial = findViewById(R.id.rv_recetas_historial);

        btnAtras.setOnClickListener(view -> {
            finish();
        });

        rvRecetasHistorial.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cargarRecetasRecientes(db, Objects.requireNonNull(auth.getCurrentUser()));

    }

    public void cargarRecetasRecientes(FirebaseFirestore db, FirebaseUser currentUser) {
        db.collection("usuarios").document(currentUser.getUid())
                .addSnapshotListener((documentSnapshot, error) -> {

                    List<String> recientes = Usuario.getInstance().getRecientes();
                    if (recientes == null || recientes.isEmpty()) return;

                    // Invertimos la lista de IDs antes de cargar las recetas
                    List<String> recientesInvertido = new ArrayList<>(recientes);
                    Collections.reverse(recientesInvertido);

                    List<Receta> listaRecetasRecientes = new ArrayList<>();
                    adaptadorHistorialRecetas = new AdaptadorHistorialRecetas(listaRecetasRecientes);
                    rvRecetasHistorial.setAdapter(adaptadorHistorialRecetas);

                    GestorReceta gestorReceta = GestorReceta.getInstance();

                    // Cargar en orden invertido
                    cargarRecetasSecuencialmente(recientesInvertido, 0, listaRecetasRecientes, adaptadorHistorialRecetas, gestorReceta);
                });
    }



    private void cargarRecetasSecuencialmente(List<String> recientes, int index,
                                              List<Receta> listaRecetas,
                                              AdaptadorHistorialRecetas adaptador,
                                              GestorReceta gestorReceta) {

        if (index >= recientes.size()) return; // Caso base: terminamos

        String id = recientes.get(index);
        gestorReceta.getRecetaById(id, new ApiCallback() {
            @Override
            public void onTaskCompleted(Receta receta) {
                ApiCallback.super.onTaskCompleted(receta);

                if (receta != null) {
                    listaRecetas.add(receta);
                    adaptador.notifyItemInserted(listaRecetas.size() - 1);
                }

                // Llamada recursiva: cargar el siguiente
                cargarRecetasSecuencialmente(recientes, index + 1, listaRecetas, adaptador, gestorReceta);
            }
        });
    }

}