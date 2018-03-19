package com.intoxecure.intoxecure;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

class ContactList {
    List<String> contactName = new ArrayList<>();
    List<String> contactNo = new ArrayList<>();
    List<String> contactPhoto = new ArrayList<>();

    ContactList(Context context) {
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        assert cursor != null;
        while (cursor.moveToNext()) {
            contactName.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
            contactNo.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            contactPhoto.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)));
        }
        cursor.close();
    }

    ContactList(SavedContactsDbHelper dbHelper) {
        // TODO: Synchronize database contacts with contacts in phone
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(SavedContactsContract.ContactEntry.TABLE_NAME,
                null, null, null, null, null,
                SavedContactsContract.ContactEntry.COLUMN_NAME_NAME + " ASC");
        assert cursor != null;
        while (cursor.moveToNext()) {
            contactName.add(cursor.getString(cursor.getColumnIndex(SavedContactsContract.ContactEntry.COLUMN_NAME_NAME)));
            contactNo.add(cursor.getString(cursor.getColumnIndex(SavedContactsContract.ContactEntry.COLUMN_NAME_PHONE)));
        }
        cursor.close();
        db.close();
    }

    int size() {
        return contactName.size();
    }

    void SaveContactList(ListView listView, SavedContactsDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SavedContactsContract.ContactEntry.TABLE_NAME);
        if (listView != null) {
            Log.d("this.size()", Integer.toString(this.size()));
            for (int i = 0; i < this.size(); i++) {
                if (listView.isItemChecked(i)) {
                    ContentValues values = new ContentValues();
                    values.put(SavedContactsContract.ContactEntry.COLUMN_NAME_NAME, contactName.get(i));
                    values.put(SavedContactsContract.ContactEntry.COLUMN_NAME_PHONE, contactNo.get(i));
                    db.insert(SavedContactsContract.ContactEntry.TABLE_NAME, null, values);
                    Log.d("ContactList", "insterted");
                }
                Log.d("ContactList", "OKAY" + Integer.toString(i));
            }

        } else
            Log.d("ContactList", "NOT OKAY");
        db.close();
    }

    // constructor from arrays
    /*
    public ContactList(String[] contactName, String[] contactNo, String[] contactPhoto) {
        if (contactName.length == contactNo.length && contactName.length == contactPhoto.length) {
            for (int i; i<contactName.length; i++) {
                this.contactName.add(contactName[i]);
                this.contactNo.add(contactName[i]);
                this.contactPhoto.add(contactPhoto[i]);
            }
        }
    }*/
}
