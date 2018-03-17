package com.intoxecure.intoxecure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
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
        if (IntoxecureService.IS_SERVICE_RUNNING)
            toggle.setChecked(true);
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
/*
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }
    */
}
