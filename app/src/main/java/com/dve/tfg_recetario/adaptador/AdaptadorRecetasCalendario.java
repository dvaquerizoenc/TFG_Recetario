package com.dve.tfg_recetario.adaptador;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.RecetaActivity;
import com.dve.tfg_recetario.fragments.InicioFragment;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;

public class AdaptadorRecetasCalendario extends RecyclerView.Adapter<AdaptadorRecetasCalendario.ViewHolder> {
    private List<Receta> listaRecetas;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String fechaFirebase;
    private Context context;
    @Setter
    private AlertDialog dialog;

    public AdaptadorRecetasCalendario(List<Receta> listaRecetas, FirebaseAuth auth, FirebaseFirestore db, String fechaFirebase, Context context) {
        this.listaRecetas = new ArrayList<>(listaRecetas);
        this.auth = auth;
        this.db = db;
        this.fechaFirebase = fechaFirebase;
        this.context = context;
    }

    public void addItem(Receta receta) {
        listaRecetas.add(receta);
        notifyDataSetChanged();
    }

    public void removeItem(Receta receta) {
        listaRecetas.remove(receta);
        Log.d("RECETAAA", String.valueOf(listaRecetas.size()));
        if(listaRecetas.size() == 0){
            dialog.dismiss();
            Log.d("RECETAAA", "siii");
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgReceta;
        private TextView title, tag;
        private ImageButton btnEliminar;

        public ViewHolder(View v) {
            super(v);
            imgReceta = v.findViewById(R.id.img_receta_calendario);
            title = v.findViewById(R.id.titulo_receta_calendario);
            tag = v.findViewById(R.id.tag_receta_calendario);
            btnEliminar = v.findViewById(R.id.btn_eliminar_receta_calendario);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receta_calendario, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receta receta = listaRecetas.get(position);
        Log.d("MI RECETA", String.valueOf(receta.getListaEtiquetas()));

        Glide.with(holder.imgReceta.getContext())
                .load(receta.getImagen())
                .into(holder.imgReceta);

        holder.title.setText(receta.getNombre());
        holder.tag.setText(receta.getListaEtiquetas().get(0));

        holder.btnEliminar.setOnClickListener(v -> {
            removeItem(receta);
            String recetaId = String.valueOf(receta.getId());
            if (recetaId.equals("0")) {
                recetaId = receta.getIdManual();
            }

            GestorReceta.getInstance().eliminarRecetaCalendario(recetaId, fechaFirebase, auth, db, new GestorReceta.EliminarRecetaCallback() {
                @Override
                public void onResultadoEliminacion(boolean success, String errorMessage) {
                    if (!success) {
                        Toast.makeText(context, "An error has occurred, please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        });

        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(holder.itemView.getContext(), RecetaActivity.class);
                receta.setListaEtiquetas(new ArrayList<>());
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
