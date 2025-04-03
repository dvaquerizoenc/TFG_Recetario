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

import java.util.ArrayList;
import java.util.List;

public class AdaptadorFavoritos extends RecyclerView.Adapter<AdaptadorFavoritos.ViewHolder>{

    private List<Receta> listaRecetas;

    public AdaptadorFavoritos(List<Receta> listaRecetas) {
        this.listaRecetas = listaRecetas;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView title;
        TextView area;

        public ViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.img_receta_favoritos);
            title = v.findViewById(R.id.title_receta_favoritos);
            area = v.findViewById(R.id.area_receta_favoritos);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_favoritos, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorFavoritos.ViewHolder holder, int position) {
        Receta receta = listaRecetas.get(position);

        Glide.with(holder.img.getContext())
                .load(receta.getImagen())
                .into(holder.img);

        holder.title.setText(receta.getNombre());
        holder.area.setText(receta.getArea());

        receta.cargarIngredientesYMedidas();

        holder.itemView.setOnClickListener(v -> {
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
