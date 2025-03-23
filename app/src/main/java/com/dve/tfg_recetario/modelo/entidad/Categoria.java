package com.dve.tfg_recetario.modelo.entidad;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Categoria {
    @SerializedName("idCategory")
    private int id;
    @SerializedName("strCategory")
    private String nombre;
    @SerializedName("strCategoryThumb")
    private String imagen;
    @SerializedName("strCategoryDescription")
    private String descripcion;
}
