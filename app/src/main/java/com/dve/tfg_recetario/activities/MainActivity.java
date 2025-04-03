package com.dve.tfg_recetario.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.fragments.CalendarioFragment;
import com.dve.tfg_recetario.fragments.FavoritosFragment;
import com.dve.tfg_recetario.fragments.InicioFragment;
import com.dve.tfg_recetario.fragments.MisRecetasFragment;
import com.dve.tfg_recetario.fragments.PerfilFragment;
import com.dve.tfg_recetario.modelo.entidad.ListaCategorias;
import com.dve.tfg_recetario.modelo.entidad.ListaRecetasFavoritas;
import com.dve.tfg_recetario.modelo.entidad.ListaRecetasRandom;
import com.dve.tfg_recetario.modelo.entidad.LoadDialog;
import com.dve.tfg_recetario.modelo.entidad.Usuario;
import com.dve.tfg_recetario.modelo.negocio.GestorListaCategorias;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView = null;
    private Typeface fuenteRegular = null;
    private Typeface fuenteSeleccionada = null;
    private AlertDialog progressDialog = null;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String lastFragment;
    private SharedPreferences prefs;
    private Integer tema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean launchedFromScratch = savedInstanceState == null;

        bottomNavigationView = findViewById(R.id.menu_bottom);
        fuenteRegular = ResourcesCompat.getFont(this, R.font.epilogue_regular);
        fuenteSeleccionada = ResourcesCompat.getFont(this, R.font.epilogue_bold);

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (launchedFromScratch) {
            prefs.edit().remove("last_fragment").apply();
        }
        lastFragment = prefs.getString("last_fragment", "Home");

        loadUser();

        if(!lastFragment.equals("Perfil")) {
            loadDialog();
        }

        initBottomNV();
        ListaRecetasRandom.getInstance().inicializar();


        if(ListaCategorias.getInstance().getListaCategorias() == null) {
            ListaCategorias.getInstance().inicializar();
        }
        iniciarGestores();

        ListaRecetasFavoritas.getInstance().inicializar();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_view);

            if(item.getItemId() == R.id.menu_inicio) {
                if (!(currentFragment instanceof InicioFragment)) {
                    cambiarFragment(new InicioFragment());
                    return true;
                }
            } else if (item.getItemId() == R.id.menu_favoritos) {
                if (!(currentFragment instanceof FavoritosFragment)) {
                    cambiarFragment(new FavoritosFragment());
                    return true;
                }
            } else if (item.getItemId() == R.id.menu_calendario) {
                if (!(currentFragment instanceof CalendarioFragment)) {
                    cambiarFragment(new CalendarioFragment());
                    return true;
                }
            } else if (item.getItemId() == R.id.menu_perfil) {
                if (!(currentFragment instanceof PerfilFragment)) {
                    cambiarFragment(new PerfilFragment());
                    return true;
                }
            } else if (item.getItemId() == R.id.menu_mis_recetas) {
                if (!(currentFragment instanceof MisRecetasFragment)) {
                    cambiarFragment(new MisRecetasFragment());
                    return true;
                }
            }

            return false;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void iniciarGestores() {
        GestorReceta.getInstance().inicializar();
        GestorListaCategorias.getInstance().inicializar();
        GestorListaCategorias.getInstance().initListaCategorias(new ApiCallback() {
            @Override
            public void onTaskCompleted(String result) {
                if(lastFragment.equals("Perfil")) {
                    cambiarFragment(new PerfilFragment());
                } else {
                    cambiarFragment(new InicioFragment(progressDialog));
                }
            }
        });
    }

    private void initBottomNV(){
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            View view = bottomNavigationView.findViewById(item.getItemId());
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setTypeface(fuenteRegular);
            }
        }
        bottomNavigationView.setItemIconTintList(null);
    }
    
    public void cambiarFragment(Fragment fragment) {
        if (!isFinishing() && !isDestroyed()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_view, fragment);
            transaction.commit();
        }
    }

    public void loadDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);

        progressDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        LoadDialog.getInstance().inicializar(progressDialog);

        TextView tvProgressText = dialogView.findViewById(R.id.tvProgressText);

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        progressDialog.show();

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int dotCount = 0;
            @Override
            public void run() {
                String dots = new String(new char[dotCount % 4]).replace("\0", ".");
                tvProgressText.setText(getString(R.string.progress_dialog) + dots);
                dotCount++;
                handler.postDelayed(this, 400); // Se repite cada 500ms
            }
        };

        handler.post(runnable);
    }

    private void loadUser() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("usuarios").document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    Usuario user = Usuario.getInstance();
                    user.setImagenPerfil(task.getResult().getString("imagenPerfil"));
                    user.setNombre("@"+task.getResult().getString("nombre"));
                    user.setFechaCreacion(task.getResult().getString("fechaCreacion"));
                    user.setEmail(task.getResult().getString("email"));
                    user.setTema(Integer.parseInt(String.valueOf(task.getResult().getLong("tema"))));
                    user.setFavoritos(ArrayList.class.cast(task.getResult().get("favoritos")));
                    List<String> historial = ArrayList.class.cast(task.getResult().get("recientes"));
                    Collections.reverse(historial);
                    user.setRecientes((ArrayList<String>) historial);

                    if (user.getTema() == 32) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.collection("usuarios").document(currentUser.getUid()).update("recientes", Usuario.getInstance().getRecientes());
    }
}