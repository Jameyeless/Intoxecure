package com.intoxecure.intoxecure;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by EdRagasa on 16/03/2018.
 */

public class MainActivity extends AppCompatActivity {
    private  Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleButton toggle = findViewById(R.id.toggleButton);
        if (IntoxecureService.IS_SERVICE_RUNNING) {
            toggle.setChecked(true);
            //startService(new Intent(MainActivity.this, IntoxecureService.class)
             //   .setAction(Constants.ACTION.STARTFOREGROUND_ACTION));
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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
