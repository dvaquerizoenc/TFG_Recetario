package com.dve.tfg_recetario.modelo.negocio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dve.tfg_recetario.activities.MiRecetaActivity;
import com.dve.tfg_recetario.modelo.entidad.Ingrediente;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.persistencia.DaoReceta;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;
import com.dve.tfg_recetario.modelo.servicio.RestRecetaApiService;
import com.dve.tfg_recetario.modelo.servicio.responses.RecetaResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GestorReceta {
    private static GestorReceta instance = null;
    private RestRecetaApiService restRecetaApiService = null;
    private DaoReceta daoReceta = null;
    private Context context;

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

        daoReceta = DaoReceta.getInstance();

        restRecetaApiService = retrofit.create(RestRecetaApiService.class);
    }

    public void setContext(Context context) {
        this.context = context;
        daoReceta.setContext(context);
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

    /**
     *
     * @param receta Receta que se subira a la BBDD
     * @return 0 si se subio correctamente
     */
    public void uploadReceta(Receta receta, FirebaseAuth auth, FirebaseFirestore db, FirebaseStorage storage, MiRecetaActivity.UploadRecetaCallback callback) {
        int respuesta;

        if (receta.getImagen().isBlank()) {
            respuesta = 1;
        } else if (receta.getNombre().isBlank()) {
            respuesta = 2;
        } else if (receta.getListaEtiquetas().isEmpty()) {
            respuesta = 3;
        } else if (receta.getListaIngredientes().isEmpty()) {
            respuesta = 4;
        } else if (receta.getListaInstrucciones().isEmpty()) {
            respuesta = 5;
        } else {

            daoReceta.uploadReceta(receta, auth, db, storage, success -> {
                if (success) {
                    callback.onResult(0);
                } else {
                    callback.onResult(6);
                }
            });
            return;
        }

        callback.onResult(respuesta);
    }

    public void montarReceta(Receta receta) {
        List<String> listaEtiquetas = new ArrayList<>();
        if (receta.getCategoria() != null) {
            listaEtiquetas.add(receta.getCategoria());
        }
        if (receta.getArea() != null) {
            listaEtiquetas.add(receta.getArea());
        }
        if(receta.getEtiquetas() != null) {
            listaEtiquetas.addAll(Arrays.asList(receta.getEtiquetas().split(",")));
        }
        if (receta.getListaEtiquetas() != null && !receta.getListaEtiquetas().isEmpty()) {
            listaEtiquetas.addAll(receta.getListaEtiquetas());
        }
        receta.setListaEtiquetas(listaEtiquetas);

        List<Ingrediente> listaIngredientes = getIngredientes(receta);

        receta.setListaIngredientes(listaIngredientes);

    }

    private static List<Ingrediente> getIngredientes(Receta receta) {
        List<Ingrediente> listaIngredientes = new ArrayList<>();
        List<String> listaIngrediente = new ArrayList<>(receta.getIngredientes());
        List<String> listaMedida = new ArrayList<>(receta.getMedidas());

        for(int i = 0; i < listaIngrediente.size(); i++) {
            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setIngrediente(listaIngrediente.get(i));
            if (listaMedida.get(i).isBlank()) {
                ingrediente.setCantidad("");
            } else {
                ingrediente.setCantidad(listaMedida.get(i));
            }
            ingrediente.setImg("https://www.themealdb.com/images/ingredients/"+ receta.getIngredientes().get(i).replace(" ","%20") +".png");
            listaIngredientes.add(ingrediente);
        }
        return listaIngredientes;
    }

    public void modificarImagen(Receta receta, String imgUrl) {
        receta.setImagen(imgUrl);
    }

    public Uri convertirImagenAWebP(Context context, Uri uri, String nombreArchivo) throws IOException {
        InputStream input;
        Log.d("URIRI", " "+ uri);
        if ("content".equals(uri.getScheme())) {
            input = context.getContentResolver().openInputStream(uri);
        } else if ("file".equals(uri.getScheme())) {
            input = new FileInputStream(new File(uri.getPath()));
        } else {
            throw new IllegalArgumentException("Esquema de URI no soportado: " + uri.toString());
        }

        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();

        File file = new File(context.getCacheDir(), nombreArchivo + ".webp");
        FileOutputStream fos = new FileOutputStream(file);

        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, fos);
        fos.close();

        return Uri.fromFile(file);
    }

    public void eliminarReceta(Receta receta, FirebaseAuth auth, FirebaseFirestore db, EliminarRecetaCallback callback) {
        daoReceta.eliminarRecetaPorId(receta.getIdManual(), auth, db, new DaoReceta.DeleteCallback() {
            @Override
            public void onDeleteResult(boolean success, String errorMessage) {
                callback.onResultadoEliminacion(success, errorMessage);
            }
        });
    }

    public interface EliminarRecetaCallback {
        void onResultadoEliminacion(boolean success, String errorMessage);
    }

}
