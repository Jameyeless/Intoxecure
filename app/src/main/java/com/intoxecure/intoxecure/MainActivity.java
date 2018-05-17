package com.intoxecure.intoxecure;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleButton toggle = findViewById(R.id.toggleButton);
        if (IntoxecureService.IS_SERVICE_RUNNING) {
            toggle.setChecked(true);
        }
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent service = new Intent(MainActivity.this, IntoxecureService.class);
                if (!IntoxecureService.IS_SERVICE_RUNNING) {
                    service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                    IntoxecureService.IS_SERVICE_RUNNING = true;
                } else {
                    service.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                    IntoxecureService.IS_SERVICE_RUNNING = false;
                }
                startService(service);

            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (! ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (! ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onSettingsClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onProfileClick(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    public void onLogoutClick(View v) {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    public void onAboutClick(View v) {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }
}