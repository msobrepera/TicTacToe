package com.ymest.s18_tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ymest.s18_tictactoe.R;
import com.ymest.s18_tictactoe.model.User;

public class RegistroActivity extends AppCompatActivity {

    EditText etName, etEmail, etPass;
    Button btnRegistro;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    String name, email, pass;
    ProgressBar pbRegistro;
    ScrollView formRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        IniciarVistas();
        eventos();
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void eventos() {
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = etName.getText().toString();
                email = etEmail.getText().toString();
                pass = etPass.getText().toString();

                if(name.isEmpty()){
                    etName.setError("El nombre es obligatorio");
                }else if(email.isEmpty()){
                    etEmail.setError("El email es obligatorio");
                }else if(pass.isEmpty()){
                    etPass.setError("La contraseña es obligatoria");
                }else{
                    //TODO: realizar registro en firebase Auth
                    createUser();
                }
            }
        });
    }

    private void createUser() {
        ChangeRegistroFormVisibility(false);
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //todo ok
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUI(user);

                }else{
                    ChangeRegistroFormVisibility(true);
                    Toast.makeText(RegistroActivity.this, "Error en el registro", Toast.LENGTH_LONG);
                    updateUI(null);
                }
            }
        });
    }



    private void updateUI(FirebaseUser user) {
        if(user!= null) {
            //alamcenar info de usuario en firestore

            User nuevoUsuario = new User(name, 0 ,0);
            db.collection("users")
                    .document(user.getUid())
                    .set(nuevoUsuario)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            
                            finish();
                            // navegar hacia el siguiente pantalla de la aplicacion
                            Intent i = new Intent(RegistroActivity.this, FindGameActivity.class);
                            startActivity(i);
                        }
                    });


        } else{
            ChangeRegistroFormVisibility(true);
            etPass.setError("Email y/o contraseña incorrectos");
            etPass.requestFocus();
        }

    }

    private void IniciarVistas() {
        etName = findViewById(R.id.editTextNameRegistro);
        etEmail = findViewById(R.id.editTextEmailRegistro);
        etPass = findViewById(R.id.editTextPasswordRegistro);
        btnRegistro = findViewById(R.id.buttonRegsitro);
        pbRegistro = findViewById(R.id.progressBarRegistro);
        formRegistro = findViewById(R.id.formRegistro);
        ChangeRegistroFormVisibility(true);
    }
    private void ChangeRegistroFormVisibility(boolean showForm) {
        pbRegistro.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formRegistro.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }
}