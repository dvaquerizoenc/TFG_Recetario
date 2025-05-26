package com.dve.tfg_recetario.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.adaptador.AdaptadorFavoritos;
import com.dve.tfg_recetario.adaptador.AdaptadorHistorialRecetas;
import com.dve.tfg_recetario.modelo.entidad.ListaRecetasFavoritas;
import com.dve.tfg_recetario.modelo.entidad.ListaRecetasRandom;
import com.dve.tfg_recetario.modelo.entidad.LoadDialog;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.entidad.Usuario;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritosFragment extends Fragment {

    private RecyclerView rvRecetasFavoritos;
    private AdaptadorFavoritos adaptadorFavoritos;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Receta> lista;
    private ListenerRegistration favoritosListener;
    private AlertDialog progressDialog;

    public FavoritosFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favoritos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        rvRecetasFavoritos = view.findViewById(R.id.rv_recetas_favoritos);

        rvRecetasFavoritos.setLayoutManager(new GridLayoutManager(getContext(), 2));

        lista = ListaRecetasFavoritas.getInstance().getListaRandom();
        adaptadorFavoritos = new AdaptadorFavoritos(lista);
        rvRecetasFavoritos.setAdapter(adaptadorFavoritos);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(favoritosListener == null) {
            favoritosListener = db.collection("usuarios").document(auth.getUid())
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null || documentSnapshot == null || !documentSnapshot.exists()) return;

                        List<String> nuevosIds = (List<String>) documentSnapshot.get("favoritos");
                        if (nuevosIds == null) nuevosIds = new ArrayList<>();

                        List<String> idsActuales = new ArrayList<>();
                        for (Receta r : lista) {
                            idsActuales.add(String.valueOf(r.getId()));
                        }

                        // IDs que faltan por cargar
                        List<String> idsNuevos = new ArrayList<>(nuevosIds);
                        idsNuevos.removeAll(idsActuales);

                        // IDs que se eliminaron
                        List<String> idsEliminados = new ArrayList<>(idsActuales);
                        idsEliminados.removeAll(nuevosIds);

                        if (idsNuevos.isEmpty() && idsEliminados.isEmpty()) return; // Nada que hacer

                        // Eliminar recetas que ya no están
                        if (!idsEliminados.isEmpty()) {
                            lista.removeIf(r -> idsEliminados.contains(String.valueOf(r.getId())));
                            adaptadorFavoritos.notifyDataSetChanged(); // O usar notifyItemRemoved si querés más eficiencia
                        }

                        if (!idsNuevos.isEmpty()) {
                            loadDialog();
                            cargarRecetasSecuencialmente(idsNuevos, 0, lista, adaptadorFavoritos, GestorReceta.getInstance());
                        }

                    });
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (favoritosListener != null) {
            favoritosListener.remove();
            favoritosListener = null;
        }
    }

    private void cargarRecetasSecuencialmente(List<String> favoritos, int index,
                                              List<Receta> listaRecetas,
                                              AdaptadorFavoritos adaptador,
                                              GestorReceta gestorReceta) {

        if (index >= favoritos.size()) {
            cerrarDialogoCarga();
            return;
        }


        String id = favoritos.get(index);
        gestorReceta.getRecetaById(id, new ApiCallback() {
            @Override
            public void onTaskCompleted(Receta receta) {
                ApiCallback.super.onTaskCompleted(receta);

                if (receta != null) {
                    listaRecetas.add(0, receta);
                    adaptador.notifyItemInserted(0);
                }

                Log.d("FRAGMENT", ""+receta.getNombre());
                // Llamada recursiva: cargar el siguiente
                cargarRecetasSecuencialmente(favoritos, index + 1, listaRecetas, adaptador, gestorReceta);
            }
        });
    }

    public void loadDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_carga, null);

        Context context = getContext();
        if (context == null) return;

        progressDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        LoadDialog.getInstance().inicializar(progressDialog);

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        progressDialog.show();
    }

    private void cerrarDialogoCarga() {
        if (progressDialog != null && progressDialog.isShowing()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressDialog.dismiss();
            }, 400);
        }
    }
}