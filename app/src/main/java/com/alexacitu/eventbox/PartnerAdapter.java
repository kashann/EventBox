package com.alexacitu.eventbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PartnerAdapter extends ArrayAdapter<User> {
    private int resourceLayout;
    private Context mContext;

    public PartnerAdapter(Context context, int resource, ArrayList<User> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }
        User u = getItem(position);
        if (u != null) {
            TextView name = v.findViewById(R.id.tvClientName);
            TextView contact = v.findViewById(R.id.tvClientContact);
            TextView event = v.findViewById(R.id.tvEvent);
            name.setText(u.getFirstName() + " " + u.getLastName());
            contact.setText("Phone number: " + u.getNumber());
            event.setText("Planning: " + u.getEvent());
        }
        return v;
    }
}
