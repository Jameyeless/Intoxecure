package com.intoxecure.intoxecure;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (IntoxecureService.IS_SERVICE_RUNNING)
            startActivity(intent);
    }

    public void onLoginClick(View v) {
        if(v.getId() == R.id.Blogin){
            startActivity(intent);
        }
    }
}
