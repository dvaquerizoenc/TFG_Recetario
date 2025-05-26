package com.dve.tfg_recetario.adaptador;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.modelo.entidad.Ingrediente;
import com.dve.tfg_recetario.modelo.entidad.Receta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class AdaptadorIngredientesReceta extends RecyclerView.Adapter<AdaptadorIngredientesReceta.ViewHolder> {
    private List<Ingrediente> listaIngredientes;
    private Context context;
    private AlertDialog addInfoDialog;
    private boolean isManual, isEditable;
    private Receta receta;

    private TextView titleDialog, newInfo;

    private Button cancelBtn, saveBtn;

    public AdaptadorIngredientesReceta(List<Ingrediente> listaIngredientes, Context context) {
        this.listaIngredientes = new ArrayList<>(listaIngredientes);
        this.context = context;
        isManual = false;
        verificarListaVacia();
    }

    public AdaptadorIngredientesReceta(List<Ingrediente> listaIngredientes, Context context, boolean isManual, boolean isEditable, Receta receta) {
        this.listaIngredientes = new ArrayList<>(listaIngredientes);
        this.context = context;
        this.isManual = isManual;
        this.receta = receta;
        this.isEditable = isEditable;
        verificarListaVacia();
    }

    public void addItem(Ingrediente newIngrediente){
        if (listaIngredientes.size() == 1) {
            if(listaIngredientes.get(0).getCantidad().startsWith("E.g:")) {
                listaIngredientes.clear();
            }
        }
        listaIngredientes.add(newIngrediente);
        receta.setListaIngredientes(listaIngredientes);
        notifyDataSetChanged();
    }

    public void deleteItem(String txt) {
        Iterator<Ingrediente> iterator = listaIngredientes.iterator();
        boolean eliminado = false;
        while (iterator.hasNext() && eliminado == false) {
            Ingrediente ingrediente = iterator.next();
            if (ingrediente.getIngrediente().equals(txt)) {
                iterator.remove();
                eliminado = true;
            }
        }
        verificarListaVacia();
        notifyDataSetChanged();
    }

    private void verificarListaVacia() {
        if (listaIngredientes.isEmpty()) {
            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setIngrediente(context.getString(R.string.add_ingredient));
            ingrediente.setCantidad(context.getString(R.string.add_ingredient_amount));
            ingrediente.setImg("");
            listaIngredientes.add(ingrediente);
            notifyItemInserted(0);}
    }

    public void setListaIngredientes(List<Ingrediente> listaIngredientes) {
        this.listaIngredientes = new ArrayList<>(listaIngredientes);
        notifyDataSetChanged();
    }

    public void refrescarLista() {
        notifyDataSetChanged();
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

        if (listaIngredientes.get(position).getImg().isBlank()) {
            Glide.with(holder.imagen.getContext())
                    .load(R.drawable.ic_camera)
                    .into(holder.imagen);
        } else {
            if (isManual) {
                Glide.with(holder.imagen.getContext())
                        .load(listaIngredientes.get(position).getImg())
                        .apply(new RequestOptions().transform(
                                new MultiTransformation<>(
                                        new CenterCrop(),
                                        new RoundedCorners(20))))
                        .into(holder.imagen);
            } else {
                Glide.with(holder.imagen.getContext())
                        .load(listaIngredientes.get(position).getImg())
                        .into(holder.imagen);
            }

        }

        holder.ingrediente.setText(listaIngredientes.get(position).getIngrediente());
        holder.cantidad.setText(listaIngredientes.get(position).getCantidad());

        if (isEditable) {
            holder.itemView.setOnLongClickListener(v -> {

                loadDialog(context.getString(R.string.d_tag_delete), "\"" +listaIngredientes.get(position).getIngrediente()+ "\"", listaIngredientes.get(position).getIngrediente(), position);

                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaIngredientes.size();
    }

    public void loadDialog(String title, String hint, String ingrediente, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_delete_item, null);

        cancelBtn = dialogView.findViewById(R.id.cancel_btn_item);
        saveBtn = dialogView.findViewById(R.id.delete_btn_item);
        newInfo = dialogView.findViewById(R.id.dialog_delete_item);
        titleDialog = dialogView.findViewById(R.id.dialog_delete_title);

        titleDialog.setText(title);
        newInfo.setText(hint);

        addInfoDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (addInfoDialog.getWindow() != null) {
            addInfoDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        cancelBtn.setOnClickListener(v -> {
            addInfoDialog.dismiss();
        });

        saveBtn.setOnClickListener(v -> {
            deleteItem(ingrediente);
            addInfoDialog.dismiss();
        });

        addInfoDialog.show();
    }

}
