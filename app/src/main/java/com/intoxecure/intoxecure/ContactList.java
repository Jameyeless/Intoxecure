package com.intoxecure.intoxecure;


import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.util.ArrayList;
import java.util.List;

class ContactList {
    List<String> contactName = new ArrayList<>();
    List<String> contactNo = new ArrayList<>();
    List<String> contactPhoto = new ArrayList<>();

    ContactList(Context context) {
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        assert phones != null;
        while (phones.moveToNext()) {
            contactName.add(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
            contactNo.add(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            contactPhoto.add(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)));
        }
        phones.close();
    }

    int size() {
        return contactName.size();
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
