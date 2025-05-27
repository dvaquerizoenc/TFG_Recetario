package com.dve.tfg_recetario.adaptador;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.RecetaActivity;
import com.dve.tfg_recetario.fragments.InicioFragment;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;


public class AdaptadorRecetasInicio extends RecyclerView.Adapter<AdaptadorRecetasInicio.ViewHolder> {
    private List<Receta> listaRecetas;

    public AdaptadorRecetasInicio(List<Receta> listaRecetas) {
        this.listaRecetas = new ArrayList<>(listaRecetas);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre;
        TextView categoria;
        TextView area;

        public ViewHolder(View v) {
            super(v);
            imagen = v.findViewById(R.id.img);
            nombre = v.findViewById(R.id.nombre);
            categoria = v.findViewById(R.id.categoria);
            area = v.findViewById(R.id.area);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receta, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
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
        holder.area.setText("Area · "+receta.getArea());

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

    public void agregarReceta(Receta receta, AlertDialog progressDialog) {
        listaRecetas.add(receta);
        notifyItemInserted(listaRecetas.size() - 1);
        if(listaRecetas.size() == 10) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        try {
                            progressDialog.dismiss();
                        } catch (IllegalArgumentException e) {
                            Log.d("EXCEPTION", ""+e.getMessage());
                        } catch (Exception e) {
                            Log.d("EXCEPTION", ""+e.getMessage());
                        }
                    }
                }
            }, 400);
        }
    }

    public void vacialLista() {
        listaRecetas.clear();
        notifyDataSetChanged();
    }

    public void setListaRecetas(List<Receta> listaRecetas) {
        this.listaRecetas = new ArrayList<>(listaRecetas);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaRecetas.size();
    }

}
