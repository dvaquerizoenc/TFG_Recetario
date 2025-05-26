package com.dve.tfg_recetario.adaptador;

import android.content.Context;
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
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.RecetaActivity;
import com.dve.tfg_recetario.fragments.InicioFragment;
import com.dve.tfg_recetario.modelo.entidad.Receta;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorMisRecetas extends RecyclerView.Adapter<AdaptadorMisRecetas.ViewHolder>{
    private List<Receta> listaRecetas;
    private Context context;

    public AdaptadorMisRecetas(List<Receta> listaRecetas, Context context) {
        this.listaRecetas = new ArrayList<>(listaRecetas);
        this.context = context;
    }

    public void setListaRecetas(List<Receta> listaRecetas) {
        this.listaRecetas = new ArrayList<>(listaRecetas);
        notifyDataSetChanged();
    }

    public void addReceta(Receta receta) {
        Log.d("RECETA", receta.getNombre());
        listaRecetas.add(receta);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenReceta;
        TextView nombreReceta;
        TextView tagReceta;

        public ViewHolder(View v) {
            super(v);
            imagenReceta = v.findViewById(R.id.img_receta_favoritos);
            nombreReceta = v.findViewById(R.id.title_receta_favoritos);
            tagReceta = v.findViewById(R.id.area_receta_favoritos);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_favoritos, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receta receta = listaRecetas.get(position);

        Glide.with(context).load(receta.getImagen()).apply(new RequestOptions().transform(
                        new MultiTransformation<>(
                                new CenterCrop(),
                                new RoundedCorners(20))))
                .into(holder.imagenReceta);

        holder.nombreReceta.setText(receta.getNombre());
        holder.tagReceta.setText(receta.getListaEtiquetas().get(0));

        receta.setManual(true);

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
