package com.dve.tfg_recetario.modelo.servicio;


import com.dve.tfg_recetario.modelo.servicio.responses.CategoriasResponse;
import com.dve.tfg_recetario.modelo.servicio.responses.RecetaResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestRecetaApiService {

    @GET("search.php")
    Call<RecetaResponse> getRecetaByNombre(@Query("s") String nombre);

    @GET("filter.php")
    Call<RecetaResponse> getRecetaByCategoria(@Query("c") String categoria);

    @GET("categories.php")
    Call<CategoriasResponse> getCategorias();

    @GET("random.php")
    Call<RecetaResponse> getRecetaRandom();

}
