package com.dve.tfg_recetario.modelo.persistencia;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.dve.tfg_recetario.modelo.entidad.Ingrediente;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class DaoReceta {
    private static DaoReceta instance = null;
    private Context context;

    public static DaoReceta getInstance(){
        if(instance == null) {
            instance = new DaoReceta();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void uploadReceta(Receta receta, FirebaseAuth auth, FirebaseFirestore db, FirebaseStorage storage, UploadCallback callback) {
        String imagenRecetaUrl = receta.getImagen();
        Log.d("IMAG", " " +imagenRecetaUrl);

        boolean imagenYaSubida = imagenRecetaUrl != null &&
                (imagenRecetaUrl.startsWith("https://") || imagenRecetaUrl.startsWith("gs://"));

        if (imagenYaSubida) {
            Log.d("UPLOAD", "Imagen ya está en Firebase. Paso directo a ingredientes.");
            procesarIngredientes(receta, auth, db, storage, callback);
        } else {
            try {
                Uri webpUri = GestorReceta.getInstance().convertirImagenAWebP(context, Uri.parse(imagenRecetaUrl), receta.getIdManual());
                StorageReference recetaRef = storage.getReference()
                        .child("usuarios/" + auth.getUid() + "/recetas/" + receta.getIdManual() + ".webp");

                recetaRef.putFile(webpUri)
                        .addOnSuccessListener(taskSnapshot -> recetaRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            receta.setImagen(uri.toString());
                            Log.d("UPLOAD", "Imagen de receta subida. Paso a ingredientes.");
                            procesarIngredientes(receta, auth, db, storage, callback);
                        }).addOnFailureListener(e -> callback.onUploadResult(false)))
                        .addOnFailureListener(e -> callback.onUploadResult(false));
            } catch (Exception e) {
                Log.d("EXCEPTION", "Excepcion al convertir imagen receta: " + e.getMessage());
                callback.onUploadResult(false);
            }
        }
    }

    private void procesarIngredientes(Receta receta, FirebaseAuth auth, FirebaseFirestore db, FirebaseStorage storage, UploadCallback callback) {
        List<Ingrediente> ingredientes = receta.getListaIngredientes();
        if (ingredientes == null || ingredientes.isEmpty()) {
            subirRecetaAFirestore(receta, auth, db, callback);
            return;
        }

        AtomicInteger contador = new AtomicInteger(0);
        AtomicBoolean falloReportado = new AtomicBoolean(false);
        int total = ingredientes.size();
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < total; i++) {
            int index = i;
            executor.execute(() -> {
                Ingrediente ing = ingredientes.get(index);
                String imgUrl = ing.getImg();

                boolean yaSubida = imgUrl != null && (imgUrl.startsWith("https://") || imgUrl.startsWith("gs://") || imgUrl.isEmpty());

                if (yaSubida) {
                    if (contador.incrementAndGet() == total) {
                        executor.shutdown();
                        subirRecetaAFirestore(receta, auth, db, callback);
                    }
                    return;
                }

                try {
                    Uri originalUri = Uri.parse(imgUrl);
                    Uri webpIngUri = GestorReceta.getInstance().convertirImagenAWebP(context, originalUri, receta.getIdManual() + "_ing_" + index);

                    StorageReference ingRef = storage.getReference()
                            .child("usuarios/" + auth.getUid() + "/recetas/" + receta.getIdManual() + "/ingredientes/ingrediente_" + index + ".webp");

                    ingRef.putFile(webpIngUri)
                            .addOnSuccessListener(taskSnapshot1 -> ingRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                ing.setImg(uri1.toString());

                                if (contador.incrementAndGet() == total) {
                                    executor.shutdown();
                                    subirRecetaAFirestore(receta, auth, db, callback);
                                }
                            }).addOnFailureListener(e -> reportarFallo(falloReportado, executor, callback)))
                            .addOnFailureListener(e -> reportarFallo(falloReportado, executor, callback));

                } catch (Exception e) {
                    Log.e("UPLOAD", "Error procesando imagen de ingrediente: " + e.getMessage());
                    reportarFallo(falloReportado, executor, callback);
                }
            });
        }
    }

    private void reportarFallo(AtomicBoolean falloReportado, ExecutorService executor, UploadCallback callback) {
        if (falloReportado.compareAndSet(false, true)) {
            executor.shutdownNow();
            callback.onUploadResult(false);
        }
    }

    private void subirRecetaAFirestore(Receta receta, FirebaseAuth auth, FirebaseFirestore db, UploadCallback callback) {
        db.collection("usuarios")
                .document(auth.getUid())
                .collection("recetas")
                .document(receta.getIdManual())
                .set(receta.toMapFirebase())
                .addOnSuccessListener(unused -> {
                    Log.d("UPLOAD", "Receta subida con éxito a Firestore");
                    callback.onUploadResult(true);
                })
                .addOnFailureListener(e -> callback.onUploadResult(false));
    }

    public void eliminarRecetaPorId(String idReceta, FirebaseAuth auth, FirebaseFirestore db, DeleteCallback callback) {
        String uid = auth.getUid();
        if (uid == null) {
            callback.onDeleteResult(false, "Usuario no autenticado.");
            return;
        }

        db.collection("usuarios")
                .document(uid)
                .collection("recetas")
                .document(idReceta)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE", "Receta eliminada correctamente: " + idReceta);
                    callback.onDeleteResult(true, null);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Error al eliminar receta: " + e.getMessage());
                    callback.onDeleteResult(false, e.getMessage());
                });
    }

    public void subirRecetaCalendario(Receta receta, String fecha, FirebaseAuth auth, FirebaseFirestore db, UploadDateCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("Usuario no autenticado"));
            return;
        }

        String userId = auth.getUid();

        String recetaId = String.valueOf(receta.getId());
        if (recetaId == null || recetaId.equals("0") || recetaId.trim().isEmpty()) {
            recetaId = receta.getIdManual();
        }

        if (recetaId == null || recetaId.trim().isEmpty()) {
            callback.onFailure(new Exception("ID de receta inválido"));
            return;
        }

        DocumentReference calendarioRef = db
                .collection("usuarios")
                .document(userId)
                .collection("calendario")
                .document(fecha);

        // Leer el documento antes de guardar
        String finalRecetaId = recetaId;
        calendarioRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> recetasExistentes = new ArrayList<>();

            if (documentSnapshot.exists() && documentSnapshot.contains("receta")) {
                recetasExistentes = (List<String>) documentSnapshot.get("receta");
            }

            // Verificar si ya hay 3 recetas
            if (recetasExistentes != null && recetasExistentes.size() >= 3) {
                callback.onFailure(new Exception("Ya hay 3 recetas para esta fecha"));
                return;
            }

            // Guardar la nueva receta
            Map<String, Object> data = new HashMap<>();
            data.put("receta", FieldValue.arrayUnion(finalRecetaId));

            calendarioRef.set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> callback.onSuccess())
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }

    public void eliminarRecetaCalendario(String recetaId, String fecha, FirebaseAuth auth, FirebaseFirestore db, DeleteCallback callback) {
        DocumentReference docRef = db.collection("usuarios")
                .document(auth.getUid())
                .collection("calendario")
                .document(fecha);

        // Paso 1: eliminar receta del array
        docRef.update("receta", FieldValue.arrayRemove(recetaId))
                .addOnSuccessListener(aVoid -> {
                    // Paso 2: consultar el documento actualizado
                    docRef.get().addOnSuccessListener(documentSnapshot -> {
                        List<String> recetas = (List<String>) documentSnapshot.get("receta");

                        if (recetas == null || recetas.isEmpty()) {
                            // Paso 3: si está vacío, eliminar el documento completo
                            docRef.delete()
                                    .addOnSuccessListener(aVoid2 -> {
                                        Log.d("Firebase", "Documento eliminado por estar vacío.");
                                        callback.onDeleteResult(true, "");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firebase", "Error al eliminar documento vacío", e);
                                        callback.onDeleteResult(false, e.getMessage());
                                    });
                        } else {
                            // Solo se eliminó del array, documento sigue con otras recetas
                            callback.onDeleteResult(true, "");
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error al eliminar receta", e);
                    callback.onDeleteResult(false, e.getMessage());
                });
    }


    public interface UploadDateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface UploadCallback {
        void onUploadResult(boolean success);
    }

    public interface DeleteCallback {
        void onDeleteResult(boolean success, String errorMessage);
    }
}
