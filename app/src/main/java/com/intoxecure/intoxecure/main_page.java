package com.intoxecure.intoxecure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

/**
 * Created by EdRagasa on 16/03/2018.
 */

public class main_page extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        ToggleButton toggle = findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(new Intent(main_page.this, IntoxecureService.class));
                } else {
                    stopService(new Intent(main_page.this, IntoxecureService.class));
                }
            }
        });
    }
}
