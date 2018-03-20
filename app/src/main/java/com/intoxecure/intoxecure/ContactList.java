package com.intoxecure.intoxecure;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

class ContactList {
    private List<String> contactName = new ArrayList<>();
    private List<String> contactNo = new ArrayList<>();
    private List<String> contactPhoto = new ArrayList<>();

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

    // If fromContacts = true, initialize from phone contacts
    // else, initialize from internal database
    ContactList(Context context, boolean fromContacts) {
        Log.d("ContactList Constructor", "fromContactts: " + Boolean.toString(fromContacts));
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
}
