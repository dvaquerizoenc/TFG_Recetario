package com.dve.tfg_recetario.activities;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.dve.tfg_recetario.modelo.entidad.ListaRecetasRandom;
import com.dve.tfg_recetario.modelo.entidad.LoadDialog;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.negocio.GestorListaCategorias;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView = null;
    Typeface fuenteRegular = null;
    Typeface fuenteSeleccionada = null;
    AlertDialog progressDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.menu_bottom);
        fuenteRegular = ResourcesCompat.getFont(this, R.font.epilogue_regular);
        fuenteSeleccionada = ResourcesCompat.getFont(this, R.font.epilogue_bold);



        loadDialog();

        initBottomNV();
        ListaRecetasRandom.getInstance().inicializar();


        if(ListaCategorias.getInstance().getListaCategorias() == null) {
            ListaCategorias.getInstance().inicializar();
        }
        iniciarGestores();

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
                cambiarFragment(new InicioFragment(progressDialog));
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_view, fragment);
        transaction.commit();
    }

    public void loadDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progress_dialog, null);

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int nightModeFlags = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES || nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            recreate(); // ⚠️ Esto hace que se aplique el nuevo tema
        }
    }

}