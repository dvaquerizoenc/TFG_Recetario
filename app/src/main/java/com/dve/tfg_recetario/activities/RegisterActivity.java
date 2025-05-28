package com.dve.tfg_recetario.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.modelo.entidad.LoadDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextInputEditText email;
    private TextInputEditText password;
    private TextInputEditText confirmPassword;
    private TextInputEditText username;

    private MaterialButton btnSignUp;

    private AlertDialog progressDialog;

    String[] meses = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        email = findViewById(R.id.emailRegister);
        password = findViewById(R.id.password_register);
        confirmPassword = findViewById(R.id.confirm_password_register);
        btnSignUp = findViewById(R.id.sign_up_btn);
        username = findViewById(R.id.username_register);

        ImageButton btnAtras = findViewById(R.id.btn_atras_register);
        Button btnLogin = findViewById(R.id.tvLogin);

        btnAtras.setOnClickListener(view -> {
            finish();
        });

        btnLogin.setOnClickListener(view -> {
            finish();
        });

        btnSignUp.setOnClickListener(view -> {
            String usernameText = username.getText().toString().trim();
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString();
            String confirmPasswordText = confirmPassword.getText().toString();

            if (!usernameText.isBlank()) {
                if (!emailText.isBlank()){
                    if (emailText.contains("@")){
                        if (passwordText.equals(confirmPasswordText)) {
                            if (isPasswordValid(passwordText).equals("OK")) {
                                auth.createUserWithEmailAndPassword(emailText, passwordText)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (!task.isSuccessful()) {
                                                    Exception e = task.getException();
                                                    if (e != null && e.getMessage() != null && e.getMessage().contains("email address is already in use")) {
                                                        Toast.makeText(RegisterActivity.this, "There is already an account with this email address.", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        Log.e("AUTH_ERROR", "Error: ", e);
                                                    }
                                                } else {
                                                    loadDialog();
                                                    FirebaseUser userFb = auth.getCurrentUser();
                                                    if(userFb!=null){
                                                        String uid = userFb.getUid();
                                                        try {
                                                            // Crear datos del usuario
                                                            Map<String, Object> usuario = new HashMap<>();

                                                            String fecha = "";
                                                            Calendar calendar = Calendar.getInstance();
                                                            fecha = "Joined " + calendar.get(Calendar.YEAR) + " - " + meses[calendar.get(Calendar.MONTH)];

                                                            usuario.put("correo", emailText);
                                                            usuario.put("nombre", usernameText);
                                                            usuario.put("fechaCreacion", fecha);
                                                            usuario.put("imagenPerfil", "https://firebasestorage.googleapis.com/v0/b/tfg-recetario.firebasestorage.app/o/user_default.png?alt=media&token=06d7b771-0291-49f4-815b-cab5b784f432");
                                                            usuario.put("tema", getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
                                                            usuario.put("recientes", Collections.emptyList());
                                                            usuario.put("favoritos", Collections.emptyList());


                                                            db.collection("usuarios").document(uid)
                                                                    .set(usuario)
                                                                    .addOnSuccessListener(aVoid -> Log.d("SUCCESS", "Usuario agregado con UID: " + uid))
                                                                    .addOnFailureListener(e -> Log.d("ERROR", "Error al agregar usuario: " + e.getMessage()));
                                                        } catch (Exception e){
                                                            Log.d("ERROR:"," "+e.getMessage());
                                                        }
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "An error occurred while creating the user, please try again.", Toast.LENGTH_SHORT).show();
                                                    }

                                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                                    if (user != null) {
                                                        // Si el usuario ya está autenticado, lo llevamos a MainActivity
                                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                        Toast.makeText(RegisterActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(this, isPasswordValid(passwordText), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "The passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "The email is not valid", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "The email field cannot be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "The username field cannot be empty", Toast.LENGTH_SHORT).show();

            }

        });

    }

    /**
     * Funcion para determinar si una contraseña es valida
     * @param password contraseña que se desea comprobar
     * @return
     */
    public String isPasswordValid(String password) {
        if (password.length() < 8) {
            return "The password must be at least 8 characters long.";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "The password must contain at least one uppercase letter.";
        }

        if (!password.matches(".*\\d.*")) {
            return "The password must contain at least one number.";
        }

        return "OK";

    }

    /**
     * Funcion para mostrar el dialogo de carga
     */
    public void loadDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);

        progressDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        LoadDialog.getInstance().inicializar(progressDialog);

        TextView tvProgressText = dialogView.findViewById(R.id.tvProgressText);

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        progressDialog.show();

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int dotCount = 0;
            @Override
            public void run() {
                String dots = new String(new char[dotCount % 4]).replace("\0", ".");
                tvProgressText.setText(getString(R.string.progress_dialog) + dots);
                dotCount++;
                handler.postDelayed(this, 400); // Se repite cada 500ms
            }
        };

        handler.post(runnable);
    }
}