package com.dve.tfg_recetario.modelo.entidad;

import java.util.ArrayList;
import java.util.List;

public class ListaRecetasRandom {
    private static ListaRecetasRandom instance;
    private List<Receta> listaRecetas;

    public static ListaRecetasRandom getInstance(){
        if(instance == null) {
            instance = new ListaRecetasRandom();
        }
        return instance;
    }

    public void inicializar() {
        listaRecetas = new ArrayList<>();
    }

    public List<Receta> getListaRandom() {
        return listaRecetas;
    }

    public void setListaRandom(List<Receta> newLista) {
        this.listaRecetas = newLista;
    }

    public void addListaRandom(Receta categoria) {
        this.listaRecetas.add(categoria);
    }
}
