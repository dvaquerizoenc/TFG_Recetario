package com.dve.tfg_recetario.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.adaptador.AdaptadorEtiquetasReceta;
import com.dve.tfg_recetario.adaptador.AdaptadorIngredientesReceta;
import com.dve.tfg_recetario.fragments.InicioFragment;
import com.dve.tfg_recetario.modelo.entidad.Ingrediente;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.entidad.Usuario;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecetaActivity extends AppCompatActivity {

    private AlertDialog progressDialog = null;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private AppCompatCheckBox cbFavoritos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receta);

        loadDialog(2000);

        cbFavoritos = findViewById(R.id.cbFavoritos);
        Receta receta = (Receta) getIntent().getSerializableExtra(InicioFragment.OBJ_RECETA);
        ImageButton btnAtras = findViewById(R.id.btn_atras);
        ImageView imagen = findViewById(R.id.img_receta);
        TextView tvNombre = findViewById(R.id.tvTituloReceta);
        RecyclerView rvEtiquetas = findViewById(R.id.rvEtiquetasReceta);

        RecyclerView rvIngredientes = findViewById(R.id.rvIngredientesReceta);

        TextView tvInstrucciones = findViewById(R.id.tvInstrucciones);

        btnAtras.setOnClickListener(view -> finish());

        List<String> favoritos = Usuario.getInstance().getFavoritos();
        for (String id : favoritos) {
            if (id.equals(String.valueOf(receta.getId()))) {
                cbFavoritos.setChecked(true);
            }
        }


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        ArrayList<String> recientes = Usuario.getInstance().getRecientes();
        boolean yaExiste = false;
        for (String id : recientes) {
            if (id.equals(String.valueOf(receta.getId()))) {
                yaExiste = true;
            }
        }
        if (!yaExiste) {
            recientes.add(String.valueOf(receta.getId()));
        } else {
            recientes.remove(String.valueOf(receta.getId()));
            recientes.add(String.valueOf(receta.getId()));
        }
        Usuario.getInstance().setRecientes(recientes);
        db.collection("usuarios").document(auth.getUid()).update("recientes", recientes);

        Glide.with(imagen.getContext())
                .load(receta.getImagen())
                .into(imagen);

        tvNombre.setText(receta.getNombre());

        FlexboxLayoutManager layoutManagerEtiquetas = new FlexboxLayoutManager(this);
        layoutManagerEtiquetas.setFlexDirection(FlexDirection.ROW);
        layoutManagerEtiquetas.setFlexWrap(FlexWrap.WRAP);

        rvEtiquetas.setLayoutManager(layoutManagerEtiquetas);

        List<String> items = new ArrayList<>();
        items.add(receta.getCategoria());
        items.add(receta.getArea());
        if (receta.getEtiquetas() != null) {
            items.addAll(Arrays.asList(receta.getEtiquetas().split(",")));
        }

        rvEtiquetas.setAdapter(new AdaptadorEtiquetasReceta(items));

        GridLayoutManager layoutManagerIngredientes = new GridLayoutManager(this, 2); // 2 columnas

        rvIngredientes.setLayoutManager(layoutManagerIngredientes);

        List<Ingrediente> listaIngredientes = new ArrayList<>();
        int minSize = Math.min(receta.getIngredientes().size(), receta.getMedidas().size());

        for(int i = 0; i < minSize; i++) {
            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setImg("https://www.themealdb.com/images/ingredients/"+ receta.getIngredientes().get(i).replace(" ","%20") +".png");
            ingrediente.setIngrediente(receta.getIngredientes().get(i));
            ingrediente.setCantidad(receta.getMedidas().get(i));
            listaIngredientes.add(ingrediente);
        }

        rvIngredientes.setAdapter(new AdaptadorIngredientesReceta(listaIngredientes));

        String instrucciones = receta.getInstrucciones();
        instrucciones = "<strong>·</strong> " + instrucciones;
        instrucciones = instrucciones.replace("\r\n", " ");

        if (instrucciones.endsWith(".")) {
            instrucciones = instrucciones.substring(0, instrucciones.length() - 1)
                    .replace(".", ".<br><br><strong>·</strong> ")
                    + ".";
        } else {
            instrucciones = instrucciones.replace(".", ".<br><br><strong>·</strong> ");
        }

        Spanned textoFormateado = Html.fromHtml(instrucciones, Html.FROM_HTML_MODE_LEGACY);
        tvInstrucciones.setText(textoFormateado);

        cbFavoritos.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if(isChecked) {
                favoritos.add(String.valueOf(receta.getId()));
                db.collection("usuarios").document(auth.getUid()).update("favoritos", favoritos);
            } else {
                favoritos.remove(String.valueOf(receta.getId()));
                db.collection("usuarios").document(auth.getUid()).update("favoritos", favoritos);
            }
        });

    }

    public void loadDialog(int duracion) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);

        progressDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

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
                handler.postDelayed(this, 400);
            }
        };

        handler.post(runnable);

        handler.postDelayed(() -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            handler.removeCallbacks(runnable); // Detener animación
        }, duracion);
    }
}