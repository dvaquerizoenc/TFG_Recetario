package com.dve.tfg_recetario.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.adaptador.AdaptadorMisRecetas;
import com.dve.tfg_recetario.adaptador.AdaptadorRecetasCalendario;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.entidad.RecetaDayDecorator;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.dve.tfg_recetario.modelo.servicio.ApiCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarioFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private AlertDialog progressDialog = null;
    private Map<String, List<String>> listaDiasConReceta;
    private AdaptadorRecetasCalendario adaptadorRecetasCalendario;
    private Set<CalendarDay> fechasConRecetas;

    public CalendarioFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        CollectionReference calendarioRef = db.collection("usuarios").document(userId).collection("calendario");

        calendarioRef.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e("CalendarioFragment", "Listen failed.", error);
                return;
            }
            if (querySnapshot == null) {
                Log.w("CalendarioFragment", "Snapshot es null");
                return;
            }

            calendarView.removeDecorators();

            fechasConRecetas = new HashSet<>();
            listaDiasConReceta = new HashMap<>();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String fechaId = doc.getId();
                List<String> recetas = (List<String>) doc.get("receta");
                if (recetas == null) {
                    recetas = new ArrayList<>();
                }
                Log.d("RECETASS", "id "+ fechaId);
                Log.d("RECETASS", "rece "+ recetas);
                listaDiasConReceta.put(fechaId, recetas);
                String[] partes = fechaId.split("-");

                if (partes.length == 3) {
                    int year = Integer.parseInt(partes[0]);
                    int month = Integer.parseInt(partes[1]) - 1; // CalendarDay usa 0 = enero
                    int day = Integer.parseInt(partes[2]);

                    CalendarDay calendarDay = CalendarDay.from(year, month, day);
                    fechasConRecetas.add(calendarDay);
                }
            }

            if (isAdded()) {
                int color = ContextCompat.getColor(requireContext(), R.color.generico);
                RecetaDayDecorator recetaDayDecorator = new RecetaDayDecorator(fechasConRecetas, color);
                calendarView.addDecorator(recetaDayDecorator);
                recetaDayDecorator.setColor(color);
            }


            calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                @Override
                public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                    int year = date.getYear();
                    int month = date.getMonth();
                    int day = date.getDay();

                    String fechaFirebase = year + "-" + (month + 1) + "-" + day;

                    Log.d("RECETASS", ""+ listaDiasConReceta.get(fechaFirebase));
                    List<String> listaReceta = listaDiasConReceta.get(fechaFirebase);

                    if (listaReceta != null && !listaReceta.isEmpty()){
                        loadDialogFecha(listaReceta, fechaFirebase);
                    } else {
                        loadDialogFechaVacia();
                    }
                }
            });
        });



    }

    private void loadDialogFechaVacia() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_calendario_recetas_empty, null);

        progressDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(progressDialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
        }

        progressDialog.show();
    }

    private void loadDialogFecha(List<String> listaIdsRecetas, String fechaFirebase) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_calendario_recetas, null);
        RecyclerView rvRecetasCalendario = dialogView.findViewById(R.id.rv_recetas_calendario);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        rvRecetasCalendario.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        List<Receta> listaRecetas = new ArrayList<>();
        adaptadorRecetasCalendario = new AdaptadorRecetasCalendario(listaRecetas, auth, db, fechaFirebase, getContext());
        rvRecetasCalendario.setAdapter(adaptadorRecetasCalendario);
        for (String id : listaIdsRecetas) {
            try {
                int intId = Integer.parseInt(id);
                GestorReceta.getInstance().getRecetaById(id, new ApiCallback() {
                    @Override
                    public void onTaskCompleted(Receta receta) {
                        ApiCallback.super.onTaskCompleted(receta);

                        if (receta != null) {
                            GestorReceta.getInstance().montarReceta(receta);
                            receta.setManual(false);
                            receta.setEditable(false);
                            adaptadorRecetasCalendario.addItem(receta);
                        }
                    }
                });
            } catch (NumberFormatException e) {
                db.collection("usuarios")
                        .document(auth.getUid())
                        .collection("recetas")
                        .whereEqualTo("idManual", id)
                        .addSnapshotListener((querySnapshot, error) -> {
                            if (error != null) {
                                Log.e("Firebase", "Error escuchando recetas", error);
                                return;
                            }

                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    Receta receta = doc.toObject(Receta.class);

                                    if (receta != null) {
                                        receta.setManual(true);
                                        receta.setEditable(false);
                                        adaptadorRecetasCalendario.addItem(receta);
                                    }
                                }
                            } else {
                                Log.d("Firebase", "No se encontró receta con ese idManual");
                            }

                        });
            } catch (Exception e) {
                Log.e("ERROR", ""+e.getMessage());
            }
        }

        progressDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        adaptadorRecetasCalendario.setDialog(progressDialog);

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(progressDialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
        }

        progressDialog.show();
    }



}