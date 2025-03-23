package com.dve.tfg_recetario.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dve.tfg_recetario.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    MaterialButton btnLogin;
    TextInputEditText email;
    TextInputEditText password;

    TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.signing_btn);
        email = findViewById(R.id.email_signin);
        password = findViewById(R.id.password_signin);
        forgotPassword = findViewById(R.id.forgot_password);

        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> {
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();

            if (!emailText.isBlank()) {
                if (!passwordText.isBlank()) {

                    auth.signInWithEmailAndPassword(emailText, passwordText)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                } else {
                    Toast.makeText(this, "The password field cannot be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "The email field cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPassword.setOnClickListener(view -> {
            String emailText = email.getText().toString();
            if (!emailText.isBlank()) {
                auth.sendPasswordResetEmail(emailText)
                        .addOnCompleteListener(task -> {
                           if (task.isSuccessful()) {
                               Toast.makeText(LoginActivity.this, "Reset password email sent", Toast.LENGTH_SHORT).show();
                           } else {
                               Toast.makeText(LoginActivity.this, "Error sending reset password email, please try again", Toast.LENGTH_SHORT).show();
                           }
                        });
            } else {
                Toast.makeText(this, "Enter your email address to reset your password", Toast.LENGTH_SHORT).show();
            }
        });

        Button tvCrearCuenta = findViewById(R.id.tvCrearCuenta);
        tvCrearCuenta.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}