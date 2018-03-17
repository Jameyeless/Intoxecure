package com.intoxecure.intoxecure;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by EdRagasa on 16/03/2018.
 */

public class main_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        ToggleButton toggle = findViewById(R.id.toggleButton);
        if (isServiceRunningInForeground(this, IntoxecureService.class))
            toggle.setChecked(true);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(IntoxecureService.INTOXECURE_SERVICE);
                if (isChecked) {
                    intent.putExtra("stop", 0);
                    Toast.makeText(main_page.this, "Service Started", Toast.LENGTH_SHORT).show();
                    startService(intent);
                } else {
                    intent.putExtra("stop", 1);
                    Toast.makeText(main_page.this, "Service Stopped", Toast.LENGTH_SHORT).show();
                    startService(intent);
                }
            }
        });
    }

    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }
}
