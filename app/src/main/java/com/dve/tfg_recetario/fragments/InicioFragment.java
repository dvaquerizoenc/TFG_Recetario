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

import android.os.Handler;
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

    private SearchView searchView;
    private List<Receta> listaSearchView, listaAllRecetas;
    private ImageButton btnHistorial, btnReloadRandom;

    private RecyclerView rvRecetasMain;
    private AdaptadorRecetasInicio adaptadorRecetasInicio;
    private TextView titleRandomRecipes;

    private GestorReceta gr;

    AlertDialog progressDialog = null;

    private int limit;
    private final Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;

    public InicioFragment() {
        // Required empty public constructor
    }

    public InicioFragment(AlertDialog progressDialog) {
        this.progressDialog = progressDialog;
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
        btnHistorial = view.findViewById(R.id.btn_historial);
        btnReloadRandom = view.findViewById(R.id.btn_reload_random);
        titleRandomRecipes = view.findViewById(R.id.ten_random_recipes);

        rvRecetasMain = view.findViewById(R.id.rv_recetas_main);

        gr = GestorReceta.getInstance();

        limit = 20;
        listaAllRecetas = new ArrayList<>();

        // Iniciarlizar configuración del SearchView
        initConfigSearchView();

        rvRecetasMain.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        List<Receta> lista = ListaRecetasRandom.getInstance().getListaRandom();
        adaptadorRecetasInicio = new AdaptadorRecetasInicio(lista);
        rvRecetasMain.setAdapter(adaptadorRecetasInicio);

        if (lista.isEmpty()) {
            generateRecipesRandom(lista);
        }

        GestorReceta.getInstance().getAllRecetas(new ApiCallback() {
            @Override
            public void onTaskCompleted(List<Receta> listaRecetas) {
                ApiCallback.super.onTaskCompleted(listaRecetas);
                Log.d("TOTALES", ""+listaRecetas.size());
                listaAllRecetas = new ArrayList<>(listaRecetas);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.isBlank()){
                    titleRandomRecipes.setVisibility(View.GONE);
                    Log.d("BUSQUEDA", ""+newText);
                    Log.d("BUSQUEDA", "todas"+ listaAllRecetas.size());
                    adaptadorRecetasInicio.setListaRecetas(filtrarRecetasPorNombre(newText));
                } else {
                    titleRandomRecipes.setVisibility(View.VISIBLE);
                    adaptadorRecetasInicio.setListaRecetas(lista);
                }

                debounceHandler.postDelayed(debounceRunnable, 300);
                return true;
            }
        });

        btnReloadRandom.setOnClickListener(v -> {
            generateRecipesRandom(lista);
        });

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

    private void generateRecipesRandom(List<Receta> lista) {
        lista.clear();
        adaptadorRecetasInicio.vacialLista();
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

    public List<Receta> filtrarRecetasPorNombre(String textoBuscado) {
        listaSearchView = new ArrayList<>();
        String texto = textoBuscado.toLowerCase();

        int count = 0;
        int offset = 0;

        for (Receta receta : listaAllRecetas) {
            if (receta.getNombre().toLowerCase().contains(texto)) {
                if (offset >= limit - 20 && count < 20) {
                    if (!listaSearchView.contains(receta)) {
                        listaSearchView.add(receta);
                        Log.d("RECETA", ""+receta.getId());
                        count++;
                    }
                }
                offset++;
            }
        }

        return listaSearchView;
    }


}