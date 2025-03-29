package com.dve.tfg_recetario.modelo.negocio;

import android.util.Log;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.modelo.entidad.Categoria;
import com.dve.tfg_recetario.modelo.entidad.ListaCategorias;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;
import com.dve.tfg_recetario.modelo.servicio.RestRecetaApiService;
import com.dve.tfg_recetario.modelo.servicio.responses.RecetaResponse;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GestorReceta {
    private static GestorReceta instance = null;
    private RestRecetaApiService restRecetaApiService = null;

    private GestorReceta() {}

    public static GestorReceta getInstance(){
        if(instance == null) {
            instance = new GestorReceta();
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

    public void getRecetaByNombre(String nombre, ApiCallback callback) {
        Call<RecetaResponse> call = restRecetaApiService.getRecetaByNombre(nombre);
        call.enqueue(new Callback<RecetaResponse>() {
            @Override
            public void onResponse(Call<RecetaResponse> call, Response<RecetaResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getRecetas().isEmpty()) {
                    Receta receta = response.body().getRecetas().get(0);

                    callback.onTaskCompleted(receta);
                }
            }

            @Override
            public void onFailure(Call<RecetaResponse> call, Throwable t) {
                Log.e("API_RESPONSE", "Error: " + t.getMessage());
            }
        });
    }

    public void getRecetaById(String id, ApiCallback callback) {
        Call<RecetaResponse> call = restRecetaApiService.getRecetaById(id);
        call.enqueue(new Callback<RecetaResponse>() {
            @Override
            public void onResponse(Call<RecetaResponse> call, Response<RecetaResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getRecetas().isEmpty()) {
                    Receta receta = response.body().getRecetas().get(0);
                    callback.onTaskCompleted(receta);
                }
            }

            @Override
            public void onFailure(Call<RecetaResponse> call, Throwable t) {
                Log.e("API_RESPONSE", "Error: " + t.getMessage());
            }
        });
    }

    public void getRecetasByCategoria(String categoria, ApiCallback callback) {
        List<Receta> lista = new ArrayList<>();

        Call<RecetaResponse> call = restRecetaApiService.getRecetaByCategoria(categoria);
        call.enqueue(new Callback<RecetaResponse>() {
            @Override
            public void onResponse(Call<RecetaResponse> call, Response<RecetaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for(Receta receta : response.body().getRecetas()) {
                        lista.add(receta);
                    }
                    callback.onTaskCompleted(lista);
                }
            }

            @Override
            public void onFailure(Call<RecetaResponse> call, Throwable t) {
                Log.e("API_RESPONSE", "Error: " + t.getMessage());
            }
        });

    }

    public void getRecetasRandom(ApiCallback callback) {

        try {
            Call<RecetaResponse> call = restRecetaApiService.getRecetaRandom();
            call.enqueue(new Callback<RecetaResponse>() {
                @Override
                public void onResponse(Call<RecetaResponse> call, Response<RecetaResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Receta receta = response.body().getRecetas().get(0);
                        callback.onTaskCompleted(receta);
                    }
                }

                @Override
                public void onFailure(Call<RecetaResponse> call, Throwable t) {
                    Log.e("API_RESPONSE", "Error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("EXCEPTION", ""+ e.getMessage());
        }
    }

}
