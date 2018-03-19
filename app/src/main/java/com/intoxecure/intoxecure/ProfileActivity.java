package com.intoxecure.intoxecure;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {
    ContactList contactList;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // TODO: Instead of getting contactList from Contacts, get from SharedPreferences
        contactList = new ContactList(new SavedContactsDbHelper(this));
        adapter = new ContactsArrayAdapter(this, R.layout.trustee_list_item, contactList);
        ListView listView = findViewById(R.id.trusteeListView);
        listView.setAdapter(adapter);

        // ClickListener for listView
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        "Click ListItem Number " + position, Toast.LENGTH_LONG)
                        .show();
            }
        });*/
    }

    public void onChangeClick(View view) {
        startActivity(new Intent(this, ChangeTrusteeActivity.class));
    }

    @Override
    protected void onResume() {
        contactList = new ContactList(new SavedContactsDbHelper(this));
        adapter.notifyDataSetChanged();
        super.onResume();

        Log.d("OnResume", "resumed");
    }
/*
    @Override
    protected void onRestart() {
        super.onRestart();
        contactList = new ContactList(new SavedContactsDbHelper(this));
        adapter.notifyDataSetChanged();
        Log.d("OnRestart", "retarted");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "started");
    }*/
}
