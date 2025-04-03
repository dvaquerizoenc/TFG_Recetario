package com.dve.tfg_recetario.modelo.entidad;

import java.util.ArrayList;
import java.util.List;

public class ListaRecetasFavoritas {
    private static ListaRecetasFavoritas instance;
    private List<Receta> listaRecetas;

    public static ListaRecetasFavoritas getInstance(){
        if(instance == null) {
            instance = new ListaRecetasFavoritas();
        }
        return instance;
    }

    public void inicializar() {
        listaRecetas = new ArrayList<>();
    }

    public List<Receta> getListaRandom() {
        return listaRecetas;
    }

    public void setListaFavoritas(List<Receta> newLista) {
        this.listaRecetas = new ArrayList<>(newLista);
    }

    public void addListaFavoritas(Receta categoria) {
        this.listaRecetas.add(categoria);
    }
}
