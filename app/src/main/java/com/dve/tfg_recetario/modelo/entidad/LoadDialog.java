package com.dve.tfg_recetario.modelo.entidad;

import androidx.appcompat.app.AlertDialog;

public class LoadDialog {
    private static LoadDialog instance = null;
    private AlertDialog LoadDialog = null;

    public static LoadDialog getInstance(){
        if(instance == null) {
            instance = new LoadDialog();
        }
        return instance;
    }

    public void inicializar(AlertDialog alertDialog) {
        LoadDialog = alertDialog;
    }

    public AlertDialog getLoadDialog() {
        return LoadDialog;
    }
}
