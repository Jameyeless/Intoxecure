package com.intoxecure.intoxecure;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatPreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_key_enable_sms")) {
            IntoxecureService.smsEnabled = sharedPreferences.getBoolean("pref_key_enable_sms", false);
            Log.d("smsEnabled", Boolean.toString(IntoxecureService.smsEnabled));
        } else if (key.equals("pref_key_sensor_sensitivity")) {
            IntoxecureService.sensorSensitivity = sharedPreferences.getFloat("pref_key_sensor_sensitivity", 0);
            IntoxecureService.sigmaDeltaTimeAlpha = IntoxecureService.sensorSensitivity*10;
            Log.d("sensitivity", Float.toString(IntoxecureService.sensorSensitivity));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}