package com.dve.tfg_recetario.modelo.entidad;

import java.util.ArrayList;

import lombok.Data;

@Data
public class Usuario {
    private static Usuario instance;
    private String nombre;
    private String email;
    private String imagenPerfil;
    private String fechaCreacion;

    public static Usuario getInstance(){
        if(instance == null) {
            instance = new Usuario();
        }
        return instance;
    }
}
