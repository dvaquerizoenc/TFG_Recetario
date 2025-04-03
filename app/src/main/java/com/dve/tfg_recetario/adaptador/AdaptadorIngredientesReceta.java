package com.dve.tfg_recetario.adaptador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.modelo.entidad.Ingrediente;

import java.util.ArrayList;
import java.util.List;


public class AdaptadorIngredientesReceta extends RecyclerView.Adapter<AdaptadorIngredientesReceta.ViewHolder> {
    private List<Ingrediente> listaRecetas;

    public AdaptadorIngredientesReceta(List<Ingrediente> listaRecetas) {
        this.listaRecetas = new ArrayList<>(listaRecetas);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView ingrediente;
        TextView cantidad;

        public ViewHolder(View v) {
            super(v);
            imagen = v.findViewById(R.id.img_ingrediente_receta);
            ingrediente = v.findViewById(R.id.tvIngredienteReceta);
            cantidad = v.findViewById(R.id.tvCantidadReceta);
        }
    }

    @NonNull
    @Override
    public AdaptadorIngredientesReceta.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_ingredientes, parent, false);
        AdaptadorIngredientesReceta.ViewHolder viewHolder = new AdaptadorIngredientesReceta.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(holder.imagen.getContext())
                .load(listaRecetas.get(position).getImg())
                .into(holder.imagen);

        holder.ingrediente.setText(listaRecetas.get(position).getIngrediente());
        holder.cantidad.setText(listaRecetas.get(position).getCantidad());


    }

    @Override
    public int getItemCount() {
        return listaRecetas.size();
    }


}
