package com.dve.tfg_recetario.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.activities.HistorialActivity;
import com.dve.tfg_recetario.activities.LoginActivity;
import com.dve.tfg_recetario.modelo.entidad.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PerfilFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LinearLayout editarPerfil;
    private LinearLayout cerrarSesion;
    private LinearLayout recentlyrecipes;
    private ImageView imagenPerfil;
    private TextView userName;
    private TextView fechJoin;
    private Usuario user;
    private AlertDialog usernameDialog;
    private Button cancelBtn, saveBtn;
    private EditText newUsername;
    private AppCompatCheckBox cbDarkTheme;
    private SharedPreferences prefs;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public PerfilFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editarPerfil = view.findViewById(R.id.editprofile_layout);
        cerrarSesion = view.findViewById(R.id.logout_layout);
        imagenPerfil = view.findViewById(R.id.img_perfil);
        userName = view.findViewById(R.id.username_perfil);
        fechJoin = view.findViewById(R.id.fech_join_perfil);
        cbDarkTheme = view.findViewById(R.id.darktheme_cb);
        recentlyrecipes = view.findViewById(R.id.recentlyrecipes_layout);

        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        user = Usuario.getInstance();

        Glide.with(imagenPerfil.getContext()).load(user.getImagenPerfil()).placeholder(R.drawable.user_default).into(imagenPerfil);
        userName.setText(user.getNombre());
        fechJoin.setText(user.getFechaCreacion());

        cbDarkTheme.setChecked(user.getTema() == 32);

        editarPerfil.setOnClickListener(v -> {
            loadDialog();
        });

        loadDarkTheme();

        cbDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("TAGA", "loadDarkTheme: "+isChecked);

            if(isChecked) {
                db.collection("usuarios").document(auth.getUid()).update("tema", 32);
                user.setTema(32);
            } else {
                db.collection("usuarios").document(auth.getUid()).update("tema", 16);
                user.setTema(16);
            }

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        Log.d("IMG", ""+imageUri);

                        if (isAdded()) {
                            user.setImagenPerfil(String.valueOf(imageUri));
                            Glide.with(requireContext()).load(imageUri).placeholder(R.drawable.user_default).into(imagenPerfil);
                            Log.d("IMAGE", "0");
                            uploadImageToFirebase(imageUri);
                        }


                    }
                }
        );

        imagenPerfil.setOnClickListener(v -> {
            changeImagePerfil();
        });

        recentlyrecipes.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HistorialActivity.class);
            startActivity(intent);
        });

        cerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            if(getActivity() != null) {
                getActivity().finish();
            }
        });

    }

    public void loadDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_modular, null);

        cancelBtn = dialogView.findViewById(R.id.cancel_btn_perfil);
        saveBtn = dialogView.findViewById(R.id.save_btn_perfil);
        newUsername = dialogView.findViewById(R.id.new_username_et_dialog);

        usernameDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (usernameDialog.getWindow() != null) {
            usernameDialog.getWindow().setGravity(Gravity.BOTTOM);
            usernameDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            usernameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        cancelBtn.setOnClickListener(v -> {
            usernameDialog.dismiss();
        });

        saveBtn.setOnClickListener(v -> {
            String newUserName = newUsername.getText().toString().trim();
            if(!newUserName.isBlank()){
                db.collection("usuarios").document(auth.getUid()).update("nombre", newUserName);
                user.setNombre("@"+newUserName);
                userName.setText(user.getNombre());
                Toast.makeText(getContext(), "Username changed successfully", Toast.LENGTH_SHORT).show();
                usernameDialog.dismiss();
            } else {
                Toast.makeText(getContext(), "The username field cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        usernameDialog.show();
    }

    private void loadDarkTheme(){
        prefs.edit().putString("last_fragment", "Perfil").apply();
    }

    private void changeImagePerfil() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select one picture"));
    }

    private void uploadImageToFirebase(Uri imageUri) {
        Context appContext = getContext() != null ? getContext().getApplicationContext() : null;
        Log.d("IMAGE", "1");
        new Thread(() -> {
            try {
                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference storageRef = storage.getReference()
                        .child("usuarios/" + auth.getUid() + "/perfil.jpg");

                Log.d("IMAGE", "2");
                storageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();

                                db.collection("usuarios").document(auth.getUid())
                                        .update("imagenPerfil", downloadUrl)
                                        .addOnSuccessListener(aVoid -> {

                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                Log.d("IMAGE", "3");
                                                Toast.makeText(appContext, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                                            });
                                        });
                            });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error while uploading image", Toast.LENGTH_SHORT).show();
                        });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error while uploading image", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

}