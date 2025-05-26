package com.dve.tfg_recetario.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.MiRecetaActivity;
import com.dve.tfg_recetario.adaptador.AdaptadorEtiquetasReceta;
import com.dve.tfg_recetario.adaptador.AdaptadorIngredientesReceta;
import com.dve.tfg_recetario.adaptador.AdaptadorMisRecetas;
import com.dve.tfg_recetario.modelo.entidad.LoadDialog;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MisRecetasFragment extends Fragment {

    private ImageButton btnAddRecipe;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private RecyclerView rvMisRecetas;
    private List<Receta> listaRecetas;
    private AdaptadorMisRecetas adaptadorRecetas;
    private GestorReceta gestorReceta;
    private AlertDialog progressDialog;

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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        btnAddRecipe = view.findViewById(R.id.btn_add_receta);
        rvMisRecetas = view.findViewById(R.id.rv_my_recipes);
        gestorReceta = GestorReceta.getInstance();

        btnAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MiRecetaActivity.class);
            startActivity(intent);
        });

        loadDialog();
        db.collection("usuarios")
                .document(auth.getUid())
                .collection("recetas")
                .orderBy("fechaCreacion", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firebase", "Error escuchando recetas", error);
                        return;
                    }

                    initRvMisRecetas();

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Receta receta = doc.toObject(Receta.class);
                            adaptadorRecetas.addReceta(receta);
                        }
                    }
                    progressDialog.dismiss();
                });
    }

    /**
     * Inicializador del RecyclerView de Etiquetas
     */
    private void initRvMisRecetas() {
        GridLayoutManager layoutManagerIngredientes = new GridLayoutManager(getContext(), 2); // 2 columnas

        rvMisRecetas.setLayoutManager(layoutManagerIngredientes);

        listaRecetas = new ArrayList<>();

        adaptadorRecetas = new AdaptadorMisRecetas(listaRecetas, getContext());
        rvMisRecetas.setAdapter(adaptadorRecetas);
    }

    /**
     * Función para mostrar dialogo de carga
     */
    public void loadDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_carga, null);

        progressDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        LoadDialog.getInstance().inicializar(progressDialog);

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        progressDialog.show();
    }
}