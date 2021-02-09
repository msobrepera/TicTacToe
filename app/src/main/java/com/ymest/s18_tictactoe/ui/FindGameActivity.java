package com.ymest.s18_tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.ymest.s18_tictactoe.R;
import com.ymest.s18_tictactoe.app.Constantes;
import com.ymest.s18_tictactoe.model.Jugada;

import javax.annotation.Nullable;

public class FindGameActivity extends AppCompatActivity {

    private TextView tvLoadingMessage;
    private ProgressBar pbLoading;
    private ScrollView layoutProgressBar, layoutMenuJuego;
    private Button btnJugar, btnRanking;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String uid, jugadaId = "";
    private ListenerRegistration listenerRegistration = null;
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);

        IniciarVistas();

        ChangeMenuVisibility(true);
        initFirebase();
        eventos();

    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
    }

    private void eventos() {
        btnJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeMenuVisibility(false);
                buscarJugadaLibre();

            }
        });
        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void buscarJugadaLibre() {
        tvLoadingMessage.setText("Buscando una Partida");
        animationView.playAnimation();
        db.collection("jugadas")
                .whereEqualTo("jugadorDosId", "")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() == 0){
                            //no existen partidas libres. Hay que crear partida nueva
                            crearnuevaPartida();
                        } else {
                            boolean encontrado = false;

                            for (DocumentSnapshot docJugada : task.getResult().getDocuments()) {
                                if(!docJugada.get("jugadorUnoId").equals(uid)){
                                    //Existe partida
                                    encontrado = true;
                                    jugadaId = docJugada.getId();
                                    Jugada jugada = docJugada.toObject(Jugada.class);
                                    jugada.setJugadorDosId(uid);
                                    db.collection("jugadas")
                                            .document(jugadaId)
                                            .set(jugada)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    tvLoadingMessage.setText("Partida encontrada. Vamos allá!");
                                                    animationView.setRepeatCount(0);
                                                    animationView.setAnimation("checked_animation.json");
                                                    animationView.playAnimation();
                                                    final Handler handler = new Handler();
                                                    final Runnable r = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startGame();
                                                        }
                                                    };
                                                    handler.postDelayed(r, 1500);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            ChangeMenuVisibility(true);
                                            Toast.makeText(FindGameActivity.this, "Hubo algún error al entrar en la partida", Toast.LENGTH_LONG);
                                        }
                                    });
                                    break;
                                }

                                if (!encontrado){
                                    crearnuevaPartida();
                                }

                            }
                        }
                }
        });
    }

    private void crearnuevaPartida() {
        tvLoadingMessage.setText("Creando nueva partida...");
        Jugada nuevajugada = new Jugada(uid);

        db.collection("jugadas")
                .add(nuevajugada)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        jugadaId = documentReference.getId();
                        //tenemos creada la jugada, debemos esperar a otro jugador
                        esperarJugador();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ChangeMenuVisibility(true);
                Toast.makeText(FindGameActivity.this, "Hubo algún error al crear la partida", Toast.LENGTH_LONG);
            }
        });
    }

    private void esperarJugador() {
        tvLoadingMessage.setText("Esperando a otro Jugador");
        //Escuchamos db hasta que se produzca un cambio
        listenerRegistration = db.collection("jugadas")
                .document(jugadaId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(!documentSnapshot.get("jugadorDosId").equals("")){
                            tvLoadingMessage.setText("Jugador encontrado. Comienza la partida!");
                            animationView.setRepeatCount(0);
                            animationView.setAnimation("checked_animation.json");
                            animationView.playAnimation();
                            final Handler handler = new Handler();
                            final Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    startGame();
                                }
                            };
                            handler.postDelayed(r, 1500);

                        }
                    }
                });

    }

    private void startGame() {
        if(listenerRegistration!= null){
            listenerRegistration.remove();
        }
        Intent i = new Intent(FindGameActivity.this, GameActivity.class);
        i.putExtra(Constantes.EXTRA_JUGADA_ID, jugadaId);
        startActivity(i);
        jugadaId = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(jugadaId != ""){
            ChangeMenuVisibility(false);
        //    esperarJugador();
        }else {
            ChangeMenuVisibility(true);
        }


    }

    private void ChangeMenuVisibility(boolean showMenu) {
        layoutProgressBar.setVisibility(showMenu ? View.GONE : View.VISIBLE);
        layoutMenuJuego.setVisibility(showMenu ? View.VISIBLE : View.GONE);

    }

    private void IniciarVistas() {

        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        layoutMenuJuego = findViewById(R.id.layoutMenuJuego);
        tvLoadingMessage = findViewById(R.id.textViewLoading);
        pbLoading = findViewById(R.id.progressBarLoading);
        pbLoading.setIndeterminate(true);
        tvLoadingMessage.setText("Cargando...");
        btnJugar = findViewById(R.id.buttonJugar);
        btnRanking = findViewById(R.id.buttonRanking);
        animationView = findViewById(R.id.animation_view);


    }

    @Override
    protected void onStop() {
        if(listenerRegistration !=null){
            listenerRegistration.remove();

        }

        if(jugadaId !=""){
            db.collection("jugadas")
                    .document(jugadaId)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            jugadaId = "";
                        }
                    });
        }
        super.onStop();

    }
}