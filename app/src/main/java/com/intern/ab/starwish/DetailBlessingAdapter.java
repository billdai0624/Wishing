package com.intern.ab.starwish;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by user on 2015/7/20.
 */
public class DetailBlessingAdapter extends ArrayAdapter<DetailBlessing_blessing_item> {
    private int layoutRes;
    private List<DetailBlessing_blessing_item> items;

    public DetailBlessingAdapter(Context context, int layoutRes, List<DetailBlessing_blessing_item> items) {
        super(context, layoutRes, items);
        this.layoutRes = layoutRes;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        DetailBlessing_blessing_item item = getItem(position);
        if (null == convertView) {
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(layoutRes, null);
            holder = new ViewHolder();
            holder.blessing = (TextView) convertView.findViewById(R.id.blessing_blessing);
            holder.location = (TextView) convertView.findViewById(R.id.blessing_location);
            holder.time = (TextView) convertView.findViewById(R.id.blessing_timeTv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.blessing.setText(item.getBlessing());
        holder.location.setText(item.getLocation());
        holder.time.setText(item.getTime());
        return convertView;
    }

    private class ViewHolder {
        TextView blessing;
        TextView location;
        TextView time;
    }
}
