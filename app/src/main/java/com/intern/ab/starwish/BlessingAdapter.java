package com.intern.ab.starwish;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import static android.widget.CompoundButton.OnClickListener;


public class BlessingAdapter extends ArrayAdapter<Blessing_item> {
    public ViewHolder holder;
    private int layoutRes;
    private List<Blessing_item> items;

    public BlessingAdapter(Context context, int layoutRes, List<Blessing_item> items) {
        super(context, layoutRes, items);
        this.layoutRes = layoutRes;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //ViewHolder holder;
        Blessing_item item = getItem(position);
        if (null == convertView) {

            LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(layoutRes, null);
            holder = new ViewHolder();
            holder.realized = (CheckBox) convertView.findViewById(R.id.blessing_realized);
            holder.wish = (TextView) convertView.findViewById(R.id.blessing_wish);
            holder.time = (TextView) convertView.findViewById(R.id.blessing_time);
            holder.toDetail = (ImageButton) convertView.findViewById(R.id.blessing_toDetail);
            holder.LL = (LinearLayout) convertView.findViewById(R.id.blessingItem_LL);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(item.isRealized()){
            holder.realized.setChecked(item.isRealized());
            holder.realized.setVisibility(View.VISIBLE);
        }
        else{
            holder.realized.setVisibility(View.INVISIBLE);
        }
        holder.wish.setText(item.getWish());
        holder.time.setText(item.getTime());
        holder.toDetail.setOnClickListener(new toDetailListener(position, item.getCheeringNum(), item.getId()));
        return convertView;
    }

    /*
        public void set(int index, Wish_item item) {
            if (index >= 0 && index < items.size()) {
                items.set(index, item);
                notifyDataSetChanged();
            }
        }*/
    public void toDetail(int position, int cheeringNum, int id) {
        Bundle b = new Bundle();
        b.putString("detailWish", getItem(position).getWish());
        b.putString("detailTime", getItem(position).getTime());
        //((MainActivity) getContext()).setBundle(b);
        ((MainActivity) getContext()).toDetailBlessing(b, cheeringNum, id);
    }

    class ViewHolder {
        LinearLayout LL;
        CheckBox realized;
        TextView wish;
        TextView time;
        ImageButton toDetail;
    }

    class toDetailListener implements OnClickListener {
        int position;
        int cheeringNum;
        int id;

        public toDetailListener(int position, int cheeringNum, int id) {
            this.position = position;
            this.cheeringNum = cheeringNum;
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            toDetail(position, cheeringNum, id);
        }

    }
}

