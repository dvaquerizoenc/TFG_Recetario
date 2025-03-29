package com.dve.tfg_recetario.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.HistorialActivity;
import com.dve.tfg_recetario.adaptador.AdaptadorRecetasInicio;
import com.dve.tfg_recetario.modelo.entidad.Categoria;
import com.dve.tfg_recetario.modelo.entidad.ListaCategorias;
import com.dve.tfg_recetario.modelo.entidad.ListaRecetasRandom;
import com.dve.tfg_recetario.modelo.entidad.LoadDialog;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {
    public final static String OBJ_RECETA = "receta";

    private List<Categoria> listaCategorias;
    private SearchView searchView;
    private ImageButton btnHistorial;

    private RecyclerView rvRecetasMain;
    private AdaptadorRecetasInicio adaptadorRecetasInicio;

    private GestorReceta gr;

    AlertDialog progressDialog = null;

    public InicioFragment() {
        // Required empty public constructor
    }

    public InicioFragment(AlertDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public static InicioFragment newInstance() {
        InicioFragment fragment = new InicioFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (progressDialog == null) {
            this.progressDialog = LoadDialog.getInstance().getLoadDialog();
        }
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchView = view.findViewById(R.id.search_view);
        listaCategorias = ListaCategorias.getInstance().getListaCategorias();
        btnHistorial = view.findViewById(R.id.btn_historial);

        rvRecetasMain = view.findViewById(R.id.rv_recetas_main);

        gr = GestorReceta.getInstance();

        // Iniciarlizar configuración del SearchView
        initConfigSearchView();

        rvRecetasMain.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        List<Receta> lista = ListaRecetasRandom.getInstance().getListaRandom();
        adaptadorRecetasInicio = new AdaptadorRecetasInicio(lista);
        rvRecetasMain.setAdapter(adaptadorRecetasInicio);

        if (lista.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                gr.getRecetasRandom(new ApiCallback() {
                    @Override
                    public void onTaskCompleted(Receta receta) {
                        ListaRecetasRandom.getInstance().addListaRandom(receta);
                        adaptadorRecetasInicio.agregarReceta(receta, progressDialog);
                    }
                });
            }
        }

        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), HistorialActivity.class);
            startActivity(intent);
        });


    }

    private void initConfigSearchView() {
        EditText searchEditText;

        searchEditText= searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.texto));
        Typeface customFont = ResourcesCompat.getFont(getContext(), R.font.epilogue_regular);
        searchEditText.setTypeface(customFont);
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    }

}