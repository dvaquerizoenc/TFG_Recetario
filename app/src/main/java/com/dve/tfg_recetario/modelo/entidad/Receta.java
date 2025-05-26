package com.dve.tfg_recetario.modelo.entidad;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Receta implements Serializable {
    @SerializedName("idMeal")
    private int id;
    @SerializedName("strMeal")
    private String nombre;
    @SerializedName("strCategory")
    private String categoria;
    @SerializedName("strArea")
    private String area;
    @SerializedName("strInstructions")
    private String instrucciones;
    @SerializedName("strMealThumb")
    private String imagen;
    @SerializedName("strTags")
    private String etiquetas;

    @SerializedName("strIngredient1")
    private String ingrediente1;
    @SerializedName("strIngredient2")
    private String ingrediente2;
    @SerializedName("strIngredient3")
    private String ingrediente3;
    @SerializedName("strIngredient4")
    private String ingrediente4;
    @SerializedName("strIngredient5")
    private String ingrediente5;
    @SerializedName("strIngredient6")
    private String ingrediente6;
    @SerializedName("strIngredient7")
    private String ingrediente7;
    @SerializedName("strIngredient8")
    private String ingrediente8;
    @SerializedName("strIngredient9")
    private String ingrediente9;
    @SerializedName("strIngredient10")
    private String ingrediente10;
    @SerializedName("strIngredient11")
    private String ingrediente11;
    @SerializedName("strIngredient12")
    private String ingrediente12;
    @SerializedName("strIngredient13")
    private String ingrediente13;
    @SerializedName("strIngredient14")
    private String ingrediente14;
    @SerializedName("strIngredient15")
    private String ingrediente15;
    @SerializedName("strIngredient16")
    private String ingrediente16;
    @SerializedName("strIngredient17")
    private String ingrediente17;
    @SerializedName("strIngredient18")
    private String ingrediente18;
    @SerializedName("strIngredient19")
    private String ingrediente19;
    @SerializedName("strIngredient20")
    private String ingrediente20;

    @SerializedName("strMeasure1")
    private String medida1;
    @SerializedName("strMeasure2")
    private String medida2;
    @SerializedName("strMeasure3")
    private String medida3;
    @SerializedName("strMeasure4")
    private String medida4;
    @SerializedName("strMeasure5")
    private String medida5;
    @SerializedName("strMeasure6")
    private String medida6;
    @SerializedName("strMeasure7")
    private String medida7;
    @SerializedName("strMeasure8")
    private String medida8;
    @SerializedName("strMeasure9")
    private String medida9;
    @SerializedName("strMeasure10")
    private String medida10;
    @SerializedName("strMeasure11")
    private String medida11;
    @SerializedName("strMeasure12")
    private String medida12;
    @SerializedName("strMeasure13")
    private String medida13;
    @SerializedName("strMeasure14")
    private String medida14;
    @SerializedName("strMeasure15")
    private String medida15;
    @SerializedName("strMeasure16")
    private String medida16;
    @SerializedName("strMeasure17")
    private String medida17;
    @SerializedName("strMeasure18")
    private String medida18;
    @SerializedName("strMeasure19")
    private String medida19;
    @SerializedName("strMeasure20")
    private String medida20;

    private List<String> ingredientes;
    private List<String> medidas;
    private List<String> listaInstrucciones;
    private List<Ingrediente> listaIngredientes;
    private List<String> listaEtiquetas;
    private Date fechaCreacion;
    private boolean isManual;
    private boolean isEditable;
    private String idManual;

    public Receta() {
        this.idManual = UUID.randomUUID().toString();;
        this.nombre = "";
        this.imagen = "";
        this.listaEtiquetas = new ArrayList<>();
        this.listaIngredientes = new ArrayList<>();
        this.listaInstrucciones = new ArrayList<>();
        this.fechaCreacion = new Date();
        setEditable(false);
    }

    public void addInstrucciones(String step) {
        listaInstrucciones.add(step);
    }

    public void cargarIngredientesYMedidas() {
        ingredientes = new ArrayList<>();
        medidas = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            try {
                String ingrediente = (String) this.getClass().getDeclaredField("ingrediente" + i).get(this);
                String medida = (String) this.getClass().getDeclaredField("medida" + i).get(this);

                if (ingrediente != null && !ingrediente.trim().isEmpty()) {
                    ingredientes.add(ingrediente);
                }

                if (medida != null && !medida.trim().isEmpty()) {
                    medidas.add(medida);
                }
            } catch (Exception e) {
                Log.d("EXCEPTION", "Error al cargar ingredientes y medidas: " + e.getMessage());
            }
        }
    }

    public List<String> getIngredientes() {
        ingredientes = new ArrayList<>();
        boolean flag = false;
        int cont = 1;

        while(cont<=20 && !flag) {
            try {
                String ingrediente = (String) this.getClass().getDeclaredField("ingrediente" + cont).get(this);
                if (ingrediente != null && !ingrediente.trim().isEmpty()) {
                    ingredientes.add(ingrediente);
                } else {
                    flag = true;
                }
                cont++;
            } catch (Exception e) {
                Log.d("EXCEPTION", "Error al cargar ingredientes: " + e.getMessage());
            }
        }

        return ingredientes;
    }

    public List<String> getMedidas() {
        medidas = new ArrayList<>();
        boolean flag = false;
        int cont = 1;

        while(cont<=20 && !flag) {
            try {
                String medida = (String) this.getClass().getDeclaredField("medida" + cont).get(this);
                if (medida != null) {
                    if (!medida.trim().isEmpty()) {
                        medidas.add(medida);
                    } else {
                        String ingrediente = (String) this.getClass().getDeclaredField("ingrediente" + cont).get(this);
                        if(!ingrediente.trim().isEmpty()) {
                            medidas.add("");
                        }
                    }
                } else {
                    flag = true;
                }
                cont++;
            } catch (Exception e) {
                Log.d("EXCEPTION", "Error al cargar medidas: " + e.getMessage());
            }
        }

        return medidas;
    }

    public Map<String, Object> toMapFirebase() {
        Map<String, Object> map = new HashMap<>();
        map.put("idManual", idManual);
        map.put("nombre", nombre);
        map.put("imagen", imagen);
        map.put("listaEtiquetas", listaEtiquetas);
        map.put("listaIngredientes", listaIngredientes);
        map.put("listaInstrucciones", listaInstrucciones);
        map.put("fechaCreacion", fechaCreacion);
        return map;
    }

}
