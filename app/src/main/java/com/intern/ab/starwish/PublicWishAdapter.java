package com.intern.ab.starwish;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

public class PublicWishAdapter extends ArrayAdapter<PublicWish_item> {
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    static String device_id;
    public ViewHolder holder;
    private int layoutRes;
    private List<PublicWish_item> items;
    private ArrayList<Boolean> cheeringCheckedState;
    private ArrayList<Boolean> blessingCheckedState;
    private int[] itemSize = {R.dimen.publicWishItem_s, R.dimen.publicWishItem_m, R.dimen.publicWishItem_l};
    private boolean isDistance;
    private boolean checked;

    public PublicWishAdapter(Context context, int layoutRes, List<PublicWish_item> items) {
        super(context, layoutRes, items);
        this.layoutRes = layoutRes;
        this.items = items;
        cheeringCheckedState = new ArrayList<Boolean>();
        blessingCheckedState = new ArrayList<Boolean>();
        device_id = ((MainActivity) getContext()).device_id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PublicWish_item item = getItem(position);
        if (null == convertView) {
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(layoutRes, null);
            holder = new ViewHolder();
            holder.wishTv = (TextView) convertView.findViewById(R.id.publicWish);
            holder.distanceTv = (TextView) convertView.findViewById(R.id.distance);
            holder.elapsedTimeTv = (TextView) convertView.findViewById(R.id.elapsedTime);
            holder.cheeringNumTv = (TextView) convertView.findViewById(R.id.public_cheeringNum);
            holder.cheering = (CheckBox) convertView.findViewById(R.id.public_cheering);
            holder.blessing = (CheckBox) convertView.findViewById(R.id.blessing_icon);
            holder.location = (TextView) convertView.findViewById(R.id.publicWish_item_location);
            holder.publicWishItem_LL = (LinearLayout) convertView.findViewById(R.id.publicWish_item_height);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.distanceTv.setText(item.getDistance());
        holder.wishTv.setText(item.getWish());
        holder.elapsedTimeTv.setText(item.getElapsedTime());
        holder.cheeringNumTv.setText(Integer.toString(item.getCheeringNum()));
        if (!item.getCity().equals("")) {
            String city = item.getCity() + ", ";
            holder.location.setText(city + item.getCountry());
        } else {
            holder.location.setText(item.getCountry());
        }
        /*if (holder.wishTv.getText().toString().length() <= 40) {
            holder.publicWishItem_LL.setMinimumHeight((int) (getContext().getResources().getDimension(itemSize[0])));
        } else if (holder.wishTv.getText().toString().length() <= 90) {
            holder.publicWishItem_LL.setMinimumHeight((int) (getContext().getResources().getDimension(itemSize[1])));
        } else {
            holder.publicWishItem_LL.setMinimumHeight((int) (getContext().getResources().getDimension(itemSize[2])));
        }*/

        if (isDistance) {
            holder.distanceTv.setVisibility(VISIBLE);
        } else {
            holder.distanceTv.setVisibility(GONE);
        }
        holder.cheering.setChecked(cheeringCheckedState.get(position));
        holder.blessing.setChecked(blessingCheckedState.get(position));
        holder.cheering.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if (isConnected()) {
                    JSONObject JObj = new JSONObject();
                    cheeringCheckedState.set(position, cb.isChecked());
                    try {
                        if (cb.isChecked()) {
                            JObj.put("newCheering", item.getCheeringNum() + 1);
                            JObj.put("cancel", 0);
                        } else {
                            JObj.put("newCheering", item.getCheeringNum() - 1);
                            JObj.put("cancel", 1);
                        }
                        JObj.put("_Id", item.getWish_id());
                        JObj.put("device_id", device_id);
                        new sendCheering(item).execute(JObj);
                        holder.cheering.setEnabled(false);
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    }
                }
                else{
                    Toast.makeText(getContext(), getContext().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.blessing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                cb.setChecked(true);
                blessingCheckedState.set(position, true);
                ((MainActivity) getContext()).detailBlessingItemPosition = position;
                ((MainActivity) getContext()).toDetailBlessingFromWall(item.getWish(), item.getOriginalTime(),
                        item.getCheeringNum(), item.getWish_id(), position, cheeringCheckedState.get(position));
            }
        });
        return convertView;
    }

    public void setDistanceVisible(boolean isDistance) {
        this.isDistance = isDistance;
    }

    public void setCheered(int cheered) {
        cheeringCheckedState.add((cheered == 1));
    }

    public void setBlessed(int blessed) {
        blessingCheckedState.add((blessed == 1));
    }

    public void alterCheeredState(boolean cheered, int position) {
        cheeringCheckedState.set(position, cheered);
        if (cheered) {
            getItem(position).setCheeringNum(getItem(position).getCheeringNum() + 1);
        } else {
            getItem(position).setCheeringNum(getItem(position).getCheeringNum() - 1);
        }
        notifyDataSetChanged();
    }

    public void alterBlessedState(boolean blessed, int position) {
        blessingCheckedState.set(position, blessed);
        //items.clear();
        notifyDataSetChanged();
    }

    public void clearStates() {
        cheeringCheckedState.clear();
        blessingCheckedState.clear();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
  /*  class onCheckedChangeListener implements OnCheckedChangeListener {
        PublicWish_item item;

        public onCheckedChangeListener(PublicWish_item item) {
            this.item = item;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.public_cheering) {
                JSONObject JObj = new JSONObject();
                checked = isChecked;
                try {
                    if (buttonView.isChecked()) {
                        JObj.put("newCheering", item.getCheeringNum() + 1);
                    } else {
                        JObj.put("newCheering", item.getCheeringNum() - 1);
                    }
                    JObj.put("_Id", item.getWish_id());
                    JObj.put("device_id", device_id);
                    new sendCheering(item).execute(JObj);
                } catch (JSONException e) {
                    Log.e("JSONError", e.toString());
                }
            }
        }
    }*/

    class ViewHolder {
        LinearLayout publicWishItem_LL;
        TextView wishTv;
        TextView cheeringNumTv;
        TextView distanceTv;
        TextView elapsedTimeTv;
        CheckBox cheering;
        CheckBox blessing;
        TextView location;
    }

    class sendCheering extends AsyncTask<JSONObject, Void, String> {
        PublicWish_item item;

        public sendCheering(PublicWish_item item) {
            this.item = item;
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            String JSONString = jsonParser.makeHttpRequest(ip + "/cheering.php", params[0]);
            return JSONString;
        }

        @Override
        protected void onPostExecute(String JSONString) {
            try {
                JSONArray JArray = new JSONArray(JSONString);
                JSONObject JObj = JArray.getJSONObject(0);
                item.setCheeringNum(JObj.getInt("cheering"));
                notifyDataSetChanged();

            } catch (JSONException e) {
                Log.e("JSONError", e.toString());
            }
            holder.cheering.setEnabled(true);
        }
    }
}
