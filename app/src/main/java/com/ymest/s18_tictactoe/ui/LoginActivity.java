package com.ymest.s18_tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ymest.s18_tictactoe.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnIraRegistro;
    private ScrollView formLogin;
    private ProgressBar pbLogin;
    private FirebaseAuth firebaseAuth;
    private String email, password;
    boolean tryLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        iniciarVistas();
        firebaseAuth = FirebaseAuth.getInstance();
        eventos();


    }

    private void eventos() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();

                if(email.isEmpty()){
                    etEmail.setError("El correo es obligatorio");
                }else if(password.isEmpty()){
                    etPassword.setError("La contraseña es obligatoria");
                }else {
                    //TODO: Realizar login en Firebase Auth
                    ChangeLoginFormVisibility(false);
                    loginUser();
                }
            }
        });
        btnIraRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(i);

            }
        });
    }

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            tryLogin = true;
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else{
                            Log.w("LOGIN", "SignIn Error: ", task.getException() );
                            updateUI(null);
                        }

                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user!= null) {
            //alamcenar info de usuario en firestore

            // navegar hacia el siguiente pantalla de la aplicacion
            Intent i = new Intent(LoginActivity.this, FindGameActivity.class);
            startActivity(i);
        } else{
            ChangeLoginFormVisibility(true);
            if(tryLogin) {
                etPassword.setError("Nombre, Email y/o contraseña incorrectos");
                etPassword.requestFocus();
            }
        }
    }

    private void ChangeLoginFormVisibility(boolean showForm) {
        pbLogin.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formLogin.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }

    private void iniciarVistas() {
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        btnIraRegistro = findViewById(R.id.buttonIraRegistro);
        formLogin = findViewById(R.id.formLogin);
        pbLogin = findViewById(R.id.progressBarLogin);
        ChangeLoginFormVisibility(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Comprobamos si el usuario ha iniciado sesión en este dispoditivo anteriormente
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);

    }
}