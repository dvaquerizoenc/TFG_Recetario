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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;

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

    // Boton cerrar activity
    ImageButton btnAtras;
    // RV con las recetas
    RecyclerView rvRecetasHistorial;
    // Adaptador usado para el RV
    AdaptadorHistorialRecetas adaptadorHistorialRecetas;
    // Objeto que conecta con la BBDD
    FirebaseFirestore db;
    // Objeto que conecta con el login de la BBDD
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // DEFINICION / INICIALIZACION DE VARIABLES
        btnAtras = findViewById(R.id.btn_atras_historial);
        rvRecetasHistorial = findViewById(R.id.rv_recetas_historial);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // INICIO
        iniciarListeners();

        rvRecetasHistorial.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        cargarRecetasRecientes(db, Objects.requireNonNull(auth.getCurrentUser()));

    }

    /**
     * Inicializador de todos los listener de la actividad
     */
    private void iniciarListeners(){
        btnAtras.setOnClickListener(view -> {
            finish();
        });
    }

    /**
     * Función que busca, devuelve  y carga las recetas recientes del usuario
     * @param db Objeto de la BBDD
     * @param currentUser Usuario actual logeado
     */
    private void cargarRecetasRecientes(FirebaseFirestore db, FirebaseUser currentUser) {
        db.collection("usuarios").document(currentUser.getUid())
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("HistorialActivity", "Error al escuchar cambios", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Usuario usuario = Usuario.getInstance();
                        // Actualiza la lista de recientes del objeto Usuario
                        List<String> recientesFirestore = (List<String>) documentSnapshot.get("recientes");

                        if (recientesFirestore == null || recientesFirestore.isEmpty()) return;

                        // Actualiza el objeto usuario
                        usuario.setRecientes((ArrayList<String>) recientesFirestore);

                        // Invertir y cargar recetas
                        List<String> recientesInvertido = new ArrayList<>(recientesFirestore);
                        Collections.reverse(recientesInvertido);

                        List<Receta> listaRecetasRecientes = new ArrayList<>();
                        adaptadorHistorialRecetas = new AdaptadorHistorialRecetas(listaRecetasRecientes);
                        rvRecetasHistorial.setAdapter(adaptadorHistorialRecetas);

                        GestorReceta gestorReceta = GestorReceta.getInstance();
                        cargarRecetasSecuencialmente(recientesInvertido, 0, listaRecetasRecientes, adaptadorHistorialRecetas, gestorReceta);
                    }
                });
    }

    /**
     * Función que carga las recetas de forma secuencial para lograr una especie de animación.
     * Se utiliza a modo de blucle gracias a la recursividad
     * @param recientes lista que guarda las recetas recientes
     * @param index
     * @param listaRecetas
     * @param adaptador
     * @param gestorReceta
     */
    private void cargarRecetasSecuencialmente(List<String> recientes, int index,
                                              List<Receta> listaRecetas,
                                              AdaptadorHistorialRecetas adaptador,
                                              GestorReceta gestorReceta) {

        if (index >= recientes.size()) return;

        String id = recientes.get(index);
        gestorReceta.getRecetaById(id, new ApiCallback() {
            @Override
            public void onTaskCompleted(Receta receta) {
                ApiCallback.super.onTaskCompleted(receta);

                if (receta != null) {
                    listaRecetas.add(receta);
                    adaptador.notifyItemInserted(listaRecetas.size() - 1);
                }

                cargarRecetasSecuencialmente(recientes, index + 1, listaRecetas, adaptador, gestorReceta);
            }
        });
    }

}
