package com.intoxecure.intoxecure;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Login extends AppCompatActivity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        intent = new Intent(Login.this, main_page.class);
        if(main_page.isServiceRunningInForeground(this, IntoxecureService.class))
            startActivity(intent);
    }


    public void onLoginClick(View v) {
        if(v.getId() == R.id.Blogin){
            startActivity(intent);
        }
    }
}
