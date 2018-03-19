package com.intoxecure.intoxecure;

import android.provider.BaseColumns;

final class SavedContactsContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SavedContactsContract() {}

    /* Inner class that defines the table contents */
    static class ContactEntry implements BaseColumns {
        static final String TABLE_NAME = "contactList";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_PHONE = "phone";
    }
}