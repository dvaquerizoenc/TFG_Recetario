package com.dve.tfg_recetario.modelo.servicio;

import com.dve.tfg_recetario.modelo.entidad.Receta;

import java.util.ArrayList;
import java.util.List;

public interface ApiCallback {
    default void onTaskCompleted(String result) {};
    default void onTaskCompleted(List<Receta> listaRecetas) {};
    default void onTaskCompleted(Receta receta) {};
}

