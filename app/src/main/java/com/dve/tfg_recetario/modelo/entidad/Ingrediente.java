package com.dve.tfg_recetario.modelo.entidad;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Ingrediente implements Serializable {
    private String img;
    private String ingrediente;
    private String cantidad;

    public Ingrediente(Ingrediente original) {
        this.ingrediente = original.ingrediente;
        this.cantidad = original.cantidad;
        this.img = original.img;
    }

    public Ingrediente(){}
}
