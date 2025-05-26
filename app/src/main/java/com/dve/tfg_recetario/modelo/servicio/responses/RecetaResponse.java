package com.dve.tfg_recetario.modelo.servicio.responses;

import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RecetaResponse {
    @SerializedName("meals")
    private List<Receta> recetas;

    public List<Receta> getRecetas() {
        if(recetas == null) {
            recetas = new ArrayList<>();
        }
        return recetas;
    }
}
