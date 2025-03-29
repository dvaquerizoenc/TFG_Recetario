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
    private int tema;
    private ArrayList<String> recientes;
    private ArrayList<String> favoritos;

    public static Usuario getInstance(){
        if(instance == null) {
            instance = new Usuario();
        }
        return instance;
    }

    public void addReciente(int position, String idReceta) {
        if(recientes.size() <= 12) {
            recientes.add(idReceta);
        } else {
            recientes.remove(recientes.size() - 1);
            recientes.add(position, idReceta);
        }
    }

    public void setRecientes(ArrayList<String> recientes) {
        if(recientes.size() <= 12) {
            this.recientes = recientes;
        } else {
            this.recientes.remove(0);
        }
    }
}
