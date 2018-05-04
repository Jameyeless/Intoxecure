package com.intoxecure.intoxecure;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class ContactList {
    LinkedList<String> contactName = new LinkedList<>();
    LinkedList<String> contactNo = new LinkedList<>();
    LinkedList<String> contactPhoto = new LinkedList<>();
    private Context context;

    String getName(int index) {
        return contactName.get(index);
    }

    String getNo(int index) {
        return contactNo.get(index);
    }

    String getPhoto(int index) {
        return contactPhoto.get(index);
    }

    private void add(String contactName, String contactNo, String contactPhoto) {
        this.contactName.add(contactName);
        this.contactNo.add(contactNo);
        this.contactPhoto.add(contactPhoto);
    }

    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private boolean fromContacts;
    // If fromContacts = true, initialize from phone contacts
    // else, initialize from internal database
    ContactList(Context context, boolean fromContacts) {
        this.context = context;
        this.fromContacts = fromContacts;
        Log.d("ContactList Constructor", "fromContactts: " + Boolean.toString(fromContacts));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            initializeContacts();
        }
    }

    int size() {
        return contactName.size();
    }

    void SaveContactList(Context context, ListView listView) {
        SavedContactsDbHelper dbHelper = new SavedContactsDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SavedContactsContract.ContactEntry.TABLE_NAME);
        for (int i = 0; i < this.size(); i++) {
            boolean save = false;
            if (listView == null) {
                save = true;
            } else {
                if (((ContactsArrayAdapter)listView.getAdapter()).checkedItems.contains(i)) {
                    save = true;
                }
            }
            if (save) {
                ContentValues values = new ContentValues();
                values.put(SavedContactsContract.ContactEntry.COLUMN_NAME_NAME, contactName.get(i));
                values.put(SavedContactsContract.ContactEntry.COLUMN_NAME_PHONE, contactNo.get(i));
                db.insert(SavedContactsContract.ContactEntry.TABLE_NAME, null, values);
            }
        }
        db.close();
    }

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
                initializeContacts();
            } else {
                Toast.makeText(context, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeContacts() {
        if (fromContacts) {
            Cursor cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            assert cursor != null;
            while (cursor.moveToNext()) {
                this.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)));
            }
            cursor.close();
        } else {
            SavedContactsDbHelper dbHelper = new SavedContactsDbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.query(SavedContactsContract.ContactEntry.TABLE_NAME,
                    null, null, null, null, null,
                    SavedContactsContract.ContactEntry.COLUMN_NAME_NAME + " ASC");
            if (cursor != null) {
                String whereClause = SavedContactsContract.ContactEntry.COLUMN_NAME_NAME + "=? AND " +
                        SavedContactsContract.ContactEntry.COLUMN_NAME_PHONE + "=?";
                String whereClause2 = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=? AND " +
                        ContactsContract.CommonDataKinds.Phone.NUMBER + "=?";
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(SavedContactsContract.ContactEntry.COLUMN_NAME_NAME));
                    String no = cursor.getString(cursor.getColumnIndex(SavedContactsContract.ContactEntry.COLUMN_NAME_PHONE));

                    String[] contactUnit = {name, no};
                    Cursor cursor2 = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, whereClause2, contactUnit, null);
                    if (cursor2 != null) {
                        if (cursor2.getCount() != 0) {
                            cursor2.moveToNext();
                            this.add(name, no, cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)));
                        }
                        cursor2.close();
                    } else {
                        db.delete(SavedContactsContract.ContactEntry.TABLE_NAME, whereClause, contactUnit);
                    }
                }
                cursor.close();
            }
            db.close();
        }
    }
}
