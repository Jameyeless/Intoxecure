package com.intoxecure.intoxecure;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void StartAcivity(View view) {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction("com.intoxecure.intoxecure.IntoxecureService");
        startService(serviceIntent);
    }
}
