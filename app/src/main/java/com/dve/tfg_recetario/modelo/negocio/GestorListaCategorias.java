package com.dve.tfg_recetario.modelo.negocio;

import android.util.Log;

import com.dve.tfg_recetario.modelo.entidad.Categoria;
import com.dve.tfg_recetario.modelo.entidad.ListaCategorias;
import com.dve.tfg_recetario.modelo.servicio.responses.CategoriasResponse;
import com.dve.tfg_recetario.modelo.servicio.responses.RecetaResponse;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;
import com.dve.tfg_recetario.modelo.servicio.RestRecetaApiService;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GestorListaCategorias {
    private static GestorListaCategorias instance = null;
    private RestRecetaApiService restRecetaApiService = null;
    private ApiCallback apiCallback;

    private GestorListaCategorias() {}

    public static GestorListaCategorias getInstance(){
        if(instance == null) {
            instance = new GestorListaCategorias();
        }
        return instance;
    }

    public void inicializar() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/api/json/v1/1/")
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder().serializeNulls().create()
                )).build();

        restRecetaApiService = retrofit.create(RestRecetaApiService.class);
    }

    public RestRecetaApiService getRestRecetaApiService() {
        return restRecetaApiService;
    }

    public void initListaCategorias(ApiCallback callback) {
        Call<CategoriasResponse> call = restRecetaApiService.getCategorias();
        call.enqueue(new Callback<CategoriasResponse>() {
            @Override
            public void onResponse(Call<CategoriasResponse> call, Response<CategoriasResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for(Categoria categoria : response.body().getCategorias()) {
                        ListaCategorias.getInstance().addListaCategorias(categoria);
                    }
                    callback.onTaskCompleted("Completado");
                }
            }

            @Override
            public void onFailure(Call<CategoriasResponse> call, Throwable t) {
                Log.e("API_RESPONSE", "Error: " + t.getMessage());
            }
        });
    }
}
