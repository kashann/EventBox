package com.alexacitu.eventbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class ToDosAdapter extends ArrayAdapter<ToDo> implements Filterable {
    private int resourceLayout;
    private Context mContext;

    public ToDosAdapter(Context context, int resource, ArrayList<ToDo> items) {
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
        ToDo td = getItem(position);
        if (td != null) {
            TextView title = v.findViewById(android.R.id.text1);
            title.setText(td.getTitle());
        }
        return v;
    }
}
