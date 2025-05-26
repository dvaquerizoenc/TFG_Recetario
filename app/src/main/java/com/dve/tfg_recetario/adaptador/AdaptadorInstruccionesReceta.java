package com.dve.tfg_recetario.adaptador;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.modelo.entidad.Receta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class AdaptadorInstruccionesReceta extends RecyclerView.Adapter<AdaptadorInstruccionesReceta.ViewHolder> {
    private List<String> listaInstrucciones;
    private Context context;
    private AlertDialog addInfoDialog;
    private boolean isManual = false;
    private Receta receta;

    private TextView titleDialog, newInfo;

    private Button cancelBtn, saveBtn;

    public AdaptadorInstruccionesReceta(List<String> listaInstrucciones, Context context) {
        this.listaInstrucciones = new ArrayList<>(listaInstrucciones);
        this.context = context;
        verificarListaVacia();
    }

    public AdaptadorInstruccionesReceta(List<String> listaInstrucciones, Context context, boolean isManual, Receta receta) {
        this.listaInstrucciones = new ArrayList<>(listaInstrucciones);
        this.context = context;
        this.isManual = isManual;
        this.receta = receta;
        verificarListaVacia();
    }

    public void addItem(String newTag){
        if (listaInstrucciones.size() == 1) {
            if(listaInstrucciones.get(0).startsWith("E.g:")) {
                listaInstrucciones.clear();
            }
        }
        int numSteps = listaInstrucciones.size()+1;
        String step = numSteps+". "+newTag;
        listaInstrucciones.add(step);
        receta.setListaInstrucciones(listaInstrucciones);
        notifyDataSetChanged();
    }

    public void deleteItem(String txt) {
        String texto = txt.replace("\"", "");
        int cont = 0;
        while (cont < listaInstrucciones.size() && !Objects.equals(listaInstrucciones.get(cont), texto)) {
            cont++;
        }

        if (cont < listaInstrucciones.size()) {
            Log.d("TRACE", "Eliminando: " + listaInstrucciones.get(cont));
            listaInstrucciones.remove(cont);
            receta.setListaInstrucciones(this.listaInstrucciones);
            renumerarInstrucciones();
            verificarListaVacia();
            notifyDataSetChanged();
        }
    }

    private void renumerarInstrucciones() {
        for (int i = 0; i < listaInstrucciones.size(); i++) {
            String instruccion = listaInstrucciones.get(i);

            String textoSinNumero = instruccion.replaceFirst("^\\d+\\.\\s*", "");

            listaInstrucciones.set(i, (i + 1) + ". " + textoSinNumero);
        }
    }

    private void verificarListaVacia() {
        if (listaInstrucciones.isEmpty()) {
            listaInstrucciones.add(context.getString(R.string.add_tag));
            notifyItemInserted(0);}
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView instrucciones;

        public ViewHolder(View v) {
            super(v);
            instrucciones = v.findViewById(R.id.tvInstrucciones);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_instrucciones, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String txt = listaInstrucciones.get(position);
        holder.instrucciones.setText(txt);

        if (isManual) {
            holder.instrucciones.setOnLongClickListener(v -> {

                loadDialog(context.getString(R.string.d_tag_delete), "\"" +txt+ "\"");

                return true;
            });
        }

    }

    @Override
    public int getItemCount() {
        return listaInstrucciones.size();
    }

    public void loadDialog(String title, String hint) {
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
            Log.d("TRACE", String.valueOf(listaInstrucciones.size()));
            deleteItem(hint);
            Log.d("TRACE", hint+"hint");
            Log.d("TRACE", String.valueOf(listaInstrucciones.size()));
            addInfoDialog.dismiss();
        });

        addInfoDialog.show();
    }

}
