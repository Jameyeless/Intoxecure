package com.intoxecure.intoxecure;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class ChangeTrusteeActivity extends AppCompatActivity {
    ArrayAdapter adapter;
    ContactList contactList;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trustee);

        contactList = new ContactList(this);
        adapter = new ContactsArrayAdapter(this, R.layout.contact_list_item, contactList);
        listView = findViewById(R.id.contactListView);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("OnItemClickListener", "clicked " + Integer.toString(position));
                CheckBox box = view.findViewById(R.id.checkBox);
                if (box.isChecked()) {
                    box.setChecked(false);
                    listView.setItemChecked(position, false);
                } else {
                    box.setChecked(true);
                    listView.setItemChecked(position, true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        contactList.SaveContactList(listView, new SavedContactsDbHelper(this));
        Log.d("onDestroy", "getChoiceMode" + Integer.toString(listView.getChoiceMode()));
    }
}
