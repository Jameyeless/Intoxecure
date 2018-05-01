package com.intoxecure.intoxecure;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

public class ChangeTrusteeActivity extends AppCompatActivity {
    ArrayAdapter adapter;
    ContactList contactList;
    ListView listView;
    private static boolean toggleAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trustee);

        contactList = new ContactList(this, true);
        adapter = new ContactsArrayAdapter(this, R.layout.contact_list_item, contactList);
        listView = findViewById(R.id.contactListView);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(clickListener);
    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener()  {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CheckBox box = view.findViewById(R.id.checkBox);
            if (box.isChecked()) {
                box.setChecked(false);
                ((ContactsArrayAdapter)listView.getAdapter()).setItemChecked(position, false);
            } else {
                box.setChecked(true);
                ((ContactsArrayAdapter)listView.getAdapter()).setItemChecked(position, true);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //contactList.SaveContactList(this, listView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        contactList.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.change_trustee_menu, menu);
        Log.d("OnCreateMenu", "test");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            /*
            case R.id.selectAllButton:

                if (toggleAll) {
                    for (int i = 0; i < contactList.size(); i++) {
                        ((ContactsArrayAdapter) listView.getAdapter()).setItemChecked(i, false);
                    }
                    toggleAll = false;
                } else {
                    for (int i = 0; i < contactList.size(); i++) {
                        ((ContactsArrayAdapter) listView.getAdapter()).setItemChecked(i, true);
                    }
                    toggleAll = true;
                }
                Log.d("toggleAll", Boolean.toString(toggleAll));
                return true;*/
            case R.id.setButton:
                contactList.SaveContactList(this, listView);
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
