package com.dve.tfg_recetario.adaptador;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.RecetaActivity;
import com.dve.tfg_recetario.fragments.InicioFragment;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.entidad.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdaptadorHistorialRecetas extends RecyclerView.Adapter<AdaptadorHistorialRecetas.ViewHolder> {
    private List<Receta> listaRecetas;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public AdaptadorHistorialRecetas(List<Receta> listaRecetas) {
        this.listaRecetas = listaRecetas;
        Log.d("Adaptador", "AdaptadorHistorialRecetas: "+listaRecetas.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre;
        TextView categoria;
        TextView tiempo;

        public ViewHolder(View v) {
            super(v);
            imagen = v.findViewById(R.id.img);
            nombre = v.findViewById(R.id.nombre);
            categoria = v.findViewById(R.id.categoria);
            tiempo = v.findViewById(R.id.tiempo);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receta, parent, false);
        AdaptadorHistorialRecetas.ViewHolder viewHolder = new AdaptadorHistorialRecetas.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receta receta = listaRecetas.get(position);

        Glide.with(holder.imagen.getContext())
                .load(receta.getImagen())
                .into(holder.imagen);

        holder.nombre.setText(receta.getNombre());
        holder.categoria.setText(receta.getCategoria());
        holder.tiempo.setText("30mins · 4 personas");

        receta.cargarIngredientesYMedidas();

        holder.itemView.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(holder.itemView.getContext(), RecetaActivity.class);
                intent.putExtra(InicioFragment.OBJ_RECETA, receta);
                holder.itemView.getContext().startActivity(intent);
            } catch (Exception e) {
                Log.d("EXCEPTION", ""+e.getMessage());
            }
        });

    }

    @Override
    public int getItemCount() {
        return listaRecetas.size();
    }
}
