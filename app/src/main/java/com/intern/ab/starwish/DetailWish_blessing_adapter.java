package com.intern.ab.starwish;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by user on 2015/7/20.
 */
public class DetailWish_blessing_adapter extends ArrayAdapter<DetailWish_blessing_item> {
    private int layoutRes;
    private List<DetailWish_blessing_item> items;

    public DetailWish_blessing_adapter(Context context, int layoutRes, List<DetailWish_blessing_item> items) {
        super(context, layoutRes, items);
        this.layoutRes = layoutRes;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        DetailWish_blessing_item item = getItem(position);
        if (null == convertView) {
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(layoutRes, null);
            holder = new ViewHolder();
            holder.blessing = (TextView) convertView.findViewById(R.id.blessing);
            holder.location = (TextView) convertView.findViewById(R.id.locationTV);
            holder.time = (TextView) convertView.findViewById(R.id.blessingTimeTV);
            holder.fakeBlesser_icon = (ImageView) convertView.findViewById(R.id.fakeBlesser_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.blessing.setText(item.getBlessing());
        holder.location.setText(item.getLocation());
        holder.time.setText(item.getTime());
        holder.fakeBlesser_icon.setImageResource(item.getFakeBlesser_icon());
        return convertView;
    }

    private class ViewHolder {
        TextView blessing;
        TextView location;
        TextView time;
        ImageView fakeBlesser_icon;
    }
}
