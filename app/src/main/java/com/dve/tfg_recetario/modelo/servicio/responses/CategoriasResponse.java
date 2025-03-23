package com.dve.tfg_recetario.modelo.servicio.responses;

import com.dve.tfg_recetario.modelo.entidad.Categoria;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoriasResponse {
    @SerializedName("categories")
    private List<Categoria> categorias;

    public List<Categoria> getCategorias() {
        return categorias;
    }
}
