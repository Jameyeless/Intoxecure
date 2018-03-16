package com.intoxecure.intoxecure;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.content.Intent;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginClick(View v) {
        if(v.getId() == R.id.Blogin){
            Intent i = new Intent(Login.this, main_page.class);
            startActivity(i);
        }
    }

}
