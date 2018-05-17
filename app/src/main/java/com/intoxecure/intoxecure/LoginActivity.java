package com.intoxecure.intoxecure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

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
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            EditText username_view = findViewById(R.id.username_box);
            editor.putString("username", username_view.getText().toString());
            editor.commit();
            startActivity(intent);
        }
    }
}
