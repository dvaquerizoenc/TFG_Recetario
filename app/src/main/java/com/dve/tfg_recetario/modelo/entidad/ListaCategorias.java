package com.dve.tfg_recetario.modelo.entidad;

import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.dve.tfg_recetario.modelo.servicio.RestRecetaApiService;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class ListaCategorias {
    private static ListaCategorias instance = null;
    private List<Categoria> listaCategorias = null;

    public static ListaCategorias getInstance(){
        if(instance == null) {
            instance = new ListaCategorias();
        }
        return instance;
    }

    public void inicializar() {
        listaCategorias = new ArrayList<>();
    }

    public List<Categoria> getListaCategorias() {
        return listaCategorias;
    }

    public void setListaCategorias(List<Categoria> newLista) {
        this.listaCategorias = newLista;
    }

    public void addListaCategorias(Categoria categoria) {
        this.listaCategorias.add(categoria);
    }

}
