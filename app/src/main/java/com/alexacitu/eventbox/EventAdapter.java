package com.alexacitu.eventbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends ArrayAdapter<Event> {
    private int resourceLayout;
    private Context mContext;
    private Filter filter;
    private ArrayList<Event> events;

    public EventAdapter(Context context, int resource, ArrayList<Event> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
        events = items;
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
        Event e = getItem(position);
        if (e != null) {
            TextView title = v.findViewById(R.id.tvRowEventTitle);
            TextView date = v.findViewById(R.id.tvRowEventDate);
            ImageView logo = v.findViewById(R.id.ivEventType);
            title.setText(e.getTitle());
            SimpleDateFormat simpleDateformat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            date.setText(simpleDateformat.format(e.getDate()));
            switch (e.getType()){
                case BIRTHDAY:
                    logo.setImageResource(R.mipmap.cake);
                    break;
                case CHRISTENING:
                    logo.setImageResource(R.mipmap.wings);
                    break;
                case CONCERT:
                    logo.setImageResource(R.mipmap.spotlights);
                    break;
                case CORPORATE:
                    logo.setImageResource(R.mipmap.corporate);
                    break;
                case FESTIVAL:
                    logo.setImageResource(R.mipmap.stage);
                    break;
                case GRADUATION:
                    logo.setImageResource(R.mipmap.mortarboard);
                    break;
                case PARTY:
                    logo.setImageResource(R.mipmap.disco_ball);
                    break;
                case WEDDING:
                    logo.setImageResource(R.mipmap.rings);
                    break;
            }
        }
        return v;
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new EventFilter<Event>(events);
        return filter;
    }

    private class EventFilter<T> extends Filter {
        private ArrayList<com.alexacitu.eventbox.Event> sourceObjects;

        public EventFilter(List<com.alexacitu.eventbox.Event> objects) {
            sourceObjects = new ArrayList<>();
            synchronized (this) {
                sourceObjects.addAll(objects);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            String filterSeq = chars.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (filterSeq != null && filterSeq.length() > 0) {
                ArrayList<com.alexacitu.eventbox.Event> filter = new ArrayList<>();

                for (Event ev : sourceObjects) {
                    if (ev.getTitle().toLowerCase().contains(filterSeq))
                        filter.add(ev);
                }
                result.count = filter.size();
                result.values = filter;
            } else {
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<T> filtered = (ArrayList<T>) results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = filtered.size(); i < l; i++)
                add((Event) filtered.get(i));
            notifyDataSetInvalidated();
        }
    }
}
