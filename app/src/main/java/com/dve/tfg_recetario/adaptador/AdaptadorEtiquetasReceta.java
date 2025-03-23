package com.dve.tfg_recetario.adaptador;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.RecetaActivity;
import com.dve.tfg_recetario.modelo.entidad.Receta;

import java.util.ArrayList;
import java.util.List;


public class AdaptadorEtiquetasReceta extends RecyclerView.Adapter<AdaptadorEtiquetasReceta.ViewHolder> {
    private List<String> listaRecetas;

    public AdaptadorEtiquetasReceta(List<String> listaRecetas) {
        this.listaRecetas = new ArrayList<>(listaRecetas);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView etiqueta;

        public ViewHolder(View v) {
            super(v);
            etiqueta = v.findViewById(R.id.tvEtiquetasReceta);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_etiquetas, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String txt = listaRecetas.get(position);

        holder.etiqueta.setText(txt);


    }

    @Override
    public int getItemCount() {
        return listaRecetas.size();
    }


}
