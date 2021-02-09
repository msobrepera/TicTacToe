package com.ymest.s18_tictactoe.app;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Inicializamos Firebase y lo inidcamos en AndroidManifest.xml
        FirebaseApp.initializeApp(this);
    }
}
