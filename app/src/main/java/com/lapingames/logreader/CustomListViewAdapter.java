package com.lapingames.logreader;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListViewAdapter extends BaseAdapter {

    public ArrayList<ListRow> items;
    public Context context;

    public CustomListViewAdapter(Context context, ArrayList<ListRow> items) {
        super();
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_row, null);
        }
        ListRow r = (ListRow) getItem(position);
        final TextView text = convertView.findViewById(R.id.list_row);
        text.setText(((ListRow) getItem(position)).text);
        if (r.checked == true) {
            text.setTypeface(null, Typeface.BOLD);
        } else {
            text.setTypeface(null, Typeface.NORMAL);
        }
        return convertView;
    }
}
