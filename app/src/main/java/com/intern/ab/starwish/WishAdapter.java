package com.intern.ab.starwish;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.TimerTask;

import static android.widget.CompoundButton.OnClickListener;


public class WishAdapter extends ArrayAdapter<Wish_item> {
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    static String device_id;
    public ViewHolder holder;
    DBHelper dbHelper;
    SQLiteDatabase db;
    private int layoutRes;
    private Dialog dialog;
    private List<Wish_item> items;


    public WishAdapter(Context context, int layoutRes, List<Wish_item> items) {
        super(context, layoutRes, items);
        device_id = ((MainActivity) getContext()).device_id;
        initDB();
        this.layoutRes = layoutRes;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //ViewHolder holder;
        Wish_item item = getItem(position);
        if (null == convertView) {

            LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(layoutRes, null);
            holder = new ViewHolder();
            holder.realized = (CheckBox) convertView.findViewById(R.id.realized);
            holder.wish = (TextView) convertView.findViewById(R.id.wish);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.toDetail = (ImageButton) convertView.findViewById(R.id.toDetail);
            holder.LL = (LinearLayout) convertView.findViewById(R.id.wishItem_LL);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.realized.setChecked(item.isRealized());
        holder.wish.setText(item.getWish());
        holder.time.setText(item.getTime());

        holder.realized.setOnClickListener(new realizedListener(position));//setOnCheckedChangeListener(new realizedListener(position));
        holder.toDetail.setOnClickListener(new toDetailListener(position));

        return convertView;
    }

    /*
        public void set(int index, Wish_item item) {
            if (index >= 0 && index < items.size()) {
                items.set(index, item);
                notifyDataSetChanged();
            }
        }*/
    public void initDB() {
        dbHelper = ((MainActivity) getContext()).dbHelper;
        db = dbHelper.getReadableDatabase();
    }

    public void toDetail(int position) {
        Bundle b = new Bundle();
        b.putString("detailWish", getItem(position).getWish());
        b.putString("detailTime", getItem(position).getTime());
        b.putInt("recNo", getItem(position).getRecNo());
        b.putBoolean("Public", getItem(position).isPublic());
        b.putInt("SQLite_id", getItem(position).getId());
        //((MainActivity) getContext()).setBundle(b);
        ((MainActivity) getContext()).toDetailWish(position, b);
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    class ViewHolder {
        LinearLayout LL;
        CheckBox realized;
        TextView wish;
        TextView time;
        ImageButton toDetail;
    }

    class realizedListener implements OnClickListener {
        private int position;

        public realizedListener(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            CheckBox cb = (CheckBox)v;
            if (isConnected()) {
                if (!getItem(position).isRealized()) {
                    dialog = new Dialog(getContext(), R.style.MyDialog);
                    dialog.setContentView(R.layout.custom_dialog);
                    Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
                    Button confirm = (Button) dialog.findViewById(R.id.confirm_btn);
                    TextView question = (TextView) dialog.findViewById(R.id.dialogQuestion);
                    question.setText(getContext().getString(R.string.realized_confirm));
                    cancel.setOnClickListener(new dialogOnClick(position));
                    confirm.setOnClickListener(new dialogOnClick(position));
                    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                getItem(position).setRealized(false);
                                notifyDataSetChanged();
                                dialog.dismiss();
                            }
                            return false;
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                } else {
                    if (getItem(position).isPublic()) {
                        JSONObject updateRealized = new JSONObject();
                        try {
                            updateRealized.put("realized", 0);
                            updateRealized.put("device_id", device_id);
                            updateRealized.put("SQLite_id", getItem(position).getId());
                            new updateRealized().execute(updateRealized);
                        } catch (JSONException e) {
                            Log.e("JSONError", e.toString());
                        }
                        getItem(position).setRealized(false);
                        notifyDataSetChanged();
                    }
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("Realized", 0);
                    db.update("wish", contentValues, "_Id=?", new String[]{Integer.toString(getItem(position).getId())});
                }
            } else {
                cb.setChecked(false);
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            }
                /*AlertDialog.Builder ask_realized = new AlertDialog.Builder(getContext());
                ask_realized.setTitle(R.string.realized_title)
                        .setMessage(R.string.realized_confirm)
                        .setPositiveButton(R.string.confirm_label, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder congratulations = new AlertDialog.Builder(getContext());
                                congratulations.setMessage(R.string.congratulation);
                                Dialog dg = congratulations.show();
                                Timer timer = new Timer();
                                timer.schedule(new stopTask(dg), 3000);
                                getItem(position).setRealized(true);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getItem(position).setRealized(false);
                                notifyDataSetChanged();
                            }
                        })
                        .setCancelable(false)
                        .show();*/
                /*JSONObject updateRealized = new JSONObject();
                ContentValues contentValues = new ContentValues();
                if (getItem(position).isRealized()) {
                    //Cancel
                    try {
                        updateRealized.put("realized", 0);
                        updateRealized.put("device_id", device_id);
                        updateRealized.put("SQLite_id", getItem(position).getId());
                        new updateRealized().execute(updateRealized);
                        contentValues.put("Realized", 0);
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    }
                    getItem(position).setRealized(false);
                    notifyDataSetChanged();
                }
                else{
                    if (getItem(position).isPublic()) {
                        JSONObject JObj = new JSONObject();
                        updateRealized = new JSONObject();
                        try {
                            JObj.put("device_id", device_id);
                            JObj.put("SQLite_id", getItem(position).getId());
                            JObj.put("wish", getItem(position).getWish());
                            updateRealized.put("realized", 1);
                            updateRealized.put("device_id", device_id);
                            updateRealized.put("SQLite_id", getItem(position).getId());
                            new GcmBroadcast().execute(JObj);
                            new updateRealized().execute(updateRealized);
                        } catch (JSONException e) {
                            Log.e("JSONError", e.toString());
                        }
                    }
                    contentValues.put("Realized", 1);
                    getItem(position).setRealized(true);
                    notifyDataSetChanged();
                    Intent intent = new Intent();
                    intent.setClass(getContext(), Celebration_animation.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    getContext().startActivity(intent);
                }
            db.update("wish", contentValues, "_Id=?", new String[]{Integer.toString(getItem(position).getId())});*/
        }
    }

    class dialogOnClick implements View.OnClickListener {
        int position;

        public dialogOnClick(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            JSONObject updateRealized = new JSONObject();
            if (v.getId() == R.id.cancel_btn) {
                /*if (getItem(position).isRealized()) {
                    try {
                        updateRealized.put("realized", 0);
                        updateRealized.put("device_id", device_id);
                        updateRealized.put("SQLite_id", getItem(position).getId());
                        new updateRealized().execute(updateRealized);
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    }
                }
                getItem(position).setRealized(false);
                notifyDataSetChanged();*/
                getItem(position).setRealized(false);
                notifyDataSetChanged();
                dialog.dismiss();
            } else if (v.getId() == R.id.confirm_btn) {
                //Animation
                if (getItem(position).isPublic()) {
                    JSONObject JObj = new JSONObject();
                    updateRealized = new JSONObject();
                    try {
                        JObj.put("device_id", device_id);
                        JObj.put("SQLite_id", getItem(position).getId());
                        JObj.put("wish", getItem(position).getWish());
                        updateRealized.put("realized", 1);
                        updateRealized.put("device_id", device_id);
                        updateRealized.put("SQLite_id", getItem(position).getId());
                        new GcmBroadcast().execute(JObj);
                        new updateRealized().execute(updateRealized);
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    }
                }
                getItem(position).setRealized(true);
                notifyDataSetChanged();

                ContentValues contentValues = new ContentValues();
                contentValues.put("Realized", 1);
                db.update("wish", contentValues, "_Id=?", new String[]{Integer.toString(getItem(position).getId())});
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setClass(getContext(), Celebration_animation.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getContext().startActivity(intent);
            }
        }
    }

    class toDetailListener implements OnClickListener {
        int position;

        public toDetailListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (((MainActivity) getContext()).multipleLL.getVisibility() != View.VISIBLE)
                toDetail(position);
        }

    }

    class stopTask extends TimerTask {
        private Dialog dialog;

        public stopTask(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void run() {
            dialog.dismiss();
        }
    }

    class GcmBroadcast extends AsyncTask<JSONObject, Void, Void> {
        public GcmBroadcast() {
            super();
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            jsonParser.makeHttpRequest(ip + "/gcmBroadcast.php", params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }

    class updateRealized extends AsyncTask<JSONObject, Void, Void> {
        public updateRealized() {
            super();
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            jsonParser.makeHttpRequest(ip + "/updateRealized.php", params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}

