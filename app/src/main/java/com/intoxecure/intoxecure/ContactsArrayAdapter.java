package com.intoxecure.intoxecure;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final ContactList contactList;
    private final int resource;

    ContactsArrayAdapter(Context context, int resource, ContactList contactList) {
        super(context, R.layout.trustee_list_item);
        this.context = context;
        this.contactList = contactList;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(resource, parent, false);
            TextView contactNameView = convertView.findViewById(R.id.contact_name);
            TextView contactNoView = convertView.findViewById(R.id.contact_no);
            ImageView contactIconView = convertView.findViewById(R.id.contact_icon);
            contactNameView.setText(contactList.contactName.get(position));
            contactNoView.setText(contactList.contactNo.get(position));
            String photoUriString;
            if (position<contactList.contactPhoto.size()) {
                photoUriString = contactList.contactPhoto.get(position);
                if (photoUriString != null)
                    contactIconView.setImageURI(Uri.parse(photoUriString));
            }
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return contactList.size();
    }
}
