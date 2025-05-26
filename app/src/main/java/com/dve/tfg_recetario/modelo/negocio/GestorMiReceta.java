package com.dve.tfg_recetario.modelo.negocio;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.modelo.entidad.Ingrediente;

public class GestorMiReceta {
    private static GestorMiReceta instance = null;

    public static GestorMiReceta getInstance() {
        if (instance == null) {
            instance = new GestorMiReceta();
        }
        return instance;
    }

    public void modificarIngrediente(Ingrediente ingrediente, String nuevoDato, String tipo) {
        if (tipo.equals("img")) {
            ingrediente.setImg(nuevoDato);
        } else if (tipo.equals("nombre")) {
            ingrediente.setIngrediente(nuevoDato);
        } else if (tipo.equals("cantidad")) {
            ingrediente.setCantidad(nuevoDato);
        }
    }

    public void cargarImagen(Context context, Uri imgUrl, ImageView lienzo, boolean redondeado) {
        if (redondeado) {
            Glide.with(context).load(imgUrl).apply(new RequestOptions().transform(
                            new MultiTransformation<>(
                                    new CenterCrop(),
                                    new RoundedCorners(20))))
                    .into(lienzo);
        } else {
            Glide.with(context).load(imgUrl).into(lienzo);
        }
    }

}
