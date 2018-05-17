package com.intoxecure.intoxecure;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {
    ContactList contactList;
    ArrayAdapter adapter;
    boolean firstOpen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView username_text = findViewById(R.id.username_text);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        username_text.setText(preferences.getString("username","Batman"));

        contactList = new ContactList(this, false);
        adapter = new ContactsArrayAdapter(this, R.layout.trustee_list_item, contactList);
        ListView listView = findViewById(R.id.trusteeListView);
        listView.setAdapter(adapter);
    }

    public void onChangeClick(View view) {
        startActivity(new Intent(this, ChangeTrusteeActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstOpen) {
            contactList = new ContactList(this, false);
            adapter.notifyDataSetChanged();
        } else {
            firstOpen = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        contactList.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
