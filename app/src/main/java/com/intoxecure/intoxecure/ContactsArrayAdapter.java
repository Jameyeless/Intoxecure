package com.intoxecure.intoxecure;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;

public class ContactsArrayAdapter extends ArrayAdapter {
    private final ContactList contactList;
    private final int resource;
    private static LayoutInflater inflater = null;
    Set<Integer> checkedItems = new HashSet<>();

    ContactsArrayAdapter(Context context, int resource, ContactList contactList) {
        super(context, R.layout.trustee_list_item);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.contactList = contactList;
        this.resource = resource;
    }

    public static class ViewHolder {
        TextView contactNameView;
        TextView contactNoView;
        ImageView contactIconView;
        ConstraintLayout mainLayout;
        CheckBox checkbox;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.contactNameView = convertView.findViewById(R.id.contact_name);
            holder.contactNoView = convertView.findViewById(R.id.contact_no);
            holder.contactIconView = convertView.findViewById(R.id.contact_icon);
            holder.checkbox = convertView.findViewById(R.id.checkBox);
            holder.mainLayout = convertView.findViewById(R.id.mainLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.contactNameView.setText(contactList.getName(position));
        holder.contactNoView.setText(contactList.getNo(position));
        if (holder.checkbox != null)
            holder.checkbox.setChecked(checkedItems.contains(position));
        String temp = contactList.getPhoto(position);
        if (temp != null) {
            if (!temp.equals("")) {
                holder.contactIconView.setImageURI(Uri.parse(temp));
            } else {
                holder.contactIconView.setImageResource(R.mipmap.ic_launcher);
            }
        } else
            holder.contactIconView.setImageResource(R.mipmap.ic_launcher);


        return convertView;
    }

    public int getCount() {
        return contactList.size();
    }

    public long getItemId(int position) {
        return position;
    }

    void setItemChecked(int position, boolean check) {
        if (check)
            checkedItems.add(position);
        else
            checkedItems.remove(position);
    }
}
