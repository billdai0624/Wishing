package com.intern.ab.starwish.ui.wish_list.detail;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.intern.ab.starwish.ui.wish_list.WishList;
import com.intern.ab.starwish.ui.wish_list.edit.EditWish;
import com.intern.ab.starwish.util.DBHelper;
import com.intern.ab.starwish.model.DetailWish_blessing_item;
import com.intern.ab.starwish.util.JSONParser;
import com.intern.ab.starwish.ui.MainActivity;
import com.intern.ab.starwish.R;
import com.intern.ab.starwish.ui.wish_list.detail.DetailWish_blessing_adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DetailWishList extends Fragment {
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    static final int DELETE = 0;
    static final int QUERY = 1;
    static String device_id;
    private ListView blessingList;
    private DetailWish_blessing_adapter detailWish_blessing_adapter;
    private List<DetailWish_blessing_item> items;
    private View rootView;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private TextView detailWishTv;
    private TextView detailTimeTv;
    private TextView cheeringNumTv;
    private CheckBox cheering_icon;
    private String detailWish;
    private String detailTime;
    private boolean detailPublic;
    private int cheeringNum;
    private String detailWish_id;
    private int[] image;
    private Dialog dialog;
    private boolean isInit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.detail_wish_list_menu, menu);
        ((MainActivity) getActivity()).actionBar.setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).actionBar_title.setText(getString(R.string.detailWish_bless_title));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((MainActivity) getActivity()).onBackPressed();
                return true;
            case R.id.detailWishList_delete:
                dialog = new Dialog(getActivity(), R.style.MyDialog);
                dialog.setContentView(R.layout.custom_dialog);
                Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
                Button confirm = (Button) dialog.findViewById(R.id.confirm_btn);
                TextView question = (TextView) dialog.findViewById(R.id.dialogQuestion);
                question.setText(getString(R.string.singleDelete_confirm));
                cancel.setOnClickListener(new dialogOnClick());
                confirm.setOnClickListener(new dialogOnClick());
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }
                        return false;
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
                return true;
            case R.id.edit:
                Intent intent = new Intent();
                intent.putExtra("detailWish", detailWish);
                intent.putExtra("detailWish_id", detailWish_id);
                intent.putExtra("detailPublic", detailPublic);
                intent.setClass(getActivity(), EditWish.class);
                startActivityForResult(intent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        isInit = true;
        rootView = inflater.inflate(R.layout.detail_wish_list, container, false);
        device_id = ((MainActivity) getActivity()).device_id;
        detailWishTv = (TextView) rootView.findViewById(R.id.detailWish);
        detailTimeTv = (TextView) rootView.findViewById(R.id.detailTime);
        cheeringNumTv = (TextView) rootView.findViewById(R.id.cheeringNum);
        cheering_icon = (CheckBox) rootView.findViewById(R.id.cheeringIcon);
        cheering_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if (isConnected()) {
                    JSONObject JObj = new JSONObject();
                    try {
                        cheering_icon.setEnabled(false);
                        if (cb.isChecked()) {
                            JObj.put("newCheering", Integer.valueOf(cheeringNumTv.getText().toString()) + 1);
                            JObj.put("cancel", 0);
                        } else {
                            JObj.put("newCheering", Integer.valueOf(cheeringNumTv.getText().toString()) - 1);
                            JObj.put("cancel", 1);
                        }
                        JObj.put("SQLite_id", detailWish_id);
                        JObj.put("device_id", device_id);
                        new sendCheering().execute(JObj);
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    cb.setChecked(!cb.isChecked());
                }
            }
        });
        blessingList = (ListView) rootView.findViewById(R.id.detailWishList);
        items = new ArrayList<DetailWish_blessing_item>();
        detailWish_blessing_adapter = new DetailWish_blessing_adapter(getActivity(), R.layout.custom_detail_wish_blessing_item, items);
        blessingList.setAdapter(detailWish_blessing_adapter);
        initImages();

        /*detailWish_blessing_adapter.add(new DetailWish_blessing_item("GOGOGO!!!", image[(int)((Math.random() * 5))]));
        detailWish_blessing_adapter.add(new DetailWish_blessing_item("GOGOGOQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ!!!", image[(int)(Math.random()*5)]));
        detailWish_blessing_adapter.add(new DetailWish_blessing_item("GOGOGOQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ!!!", image[(int)(Math.random()*5)]));
        detailWish_blessing_adapter.add(new DetailWish_blessing_item("GOGOGOQQQQQQQQQQQQQQWQEWQEWQEQWEQQQQQQQQQQQQQQQQQQQQQQQQ!!!", image[(int)(Math.random()*5)]));
        detailWish_blessing_adapter.add(new DetailWish_blessing_item("GOGOGOQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ!!!", image[(int)(Math.random()*5)]));
        detailWish_blessing_adapter.add(new DetailWish_blessing_item("GOGOGOQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ!!!", image[(int)(Math.random()*5)]));
*/
        return rootView;
    }

    public void getWish(Bundle b) {
        items.clear();
        detailWish_blessing_adapter.notifyDataSetChanged();
        if (null != b) {
            //   wishData = getArguments();
            detailWish = b.getString("detailWish");
            detailTime = b.getString("detailTime");
            detailPublic = b.getBoolean("Public");
            //cheeringNum = prevWish.getString("cheeringNum");
        }
        initDB();
        cursor = db.rawQuery("SELECT _Id FROM wish WHERE rowid=?", new String[]{Integer.toString(b.getInt("recNo"))});
        cursor.moveToNext();
        detailWish_id = Integer.toString(cursor.getInt(0));
        cursor = db.rawQuery("SELECT Cheering FROM wish WHERE _Id=?", new String[]{Integer.toString(cursor.getInt(0))});
        cursor.moveToNext();
        detailWishTv.setText(detailWish);
        detailTimeTv.setText(detailTime);
        //cheeringNumTv.setText(Integer.toString(cursor.getInt(0)));
        cursor.close();
        detailWish_blessing_adapter.notifyDataSetChanged();
    }

    public void initImages() {
        image = new int[5];
        image[0] = R.drawable.ic_random1;
        image[1] = R.drawable.ic_random2;
        image[2] = R.drawable.ic_random3;
        image[3] = R.drawable.ic_random4;
        image[4] = R.drawable.ic_random5;
    }

    public void initDB() {
        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getReadableDatabase();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
           /* if (null != data) {
                String editWish = data.getExtras().getString("editWish");
                String editTime = data.getExtras().getString("editTime");
                boolean edit_allowPublic = data.getExtras().getBoolean("edit_allowPublic");
                detailWish = editWish;
                detailWishTv.setText(editWish);
                detailTimeTv.setText(editTime);
            }*/

            cursor = db.rawQuery("SELECT Wish, DateTime, Cheering FROM wish WHERE _Id=?", new String[]{detailWish_id});
            cursor.moveToNext();
            detailWish = cursor.getString(0);
            detailWishTv.setText(detailWish);
            detailTimeTv.setText(cursor.getString(1));
            //cheeringNumTv.setText(Integer.toString(cursor.getInt(2)));
            cursor.close();
            ((MainActivity) getActivity()).shouldRefresh = true;
            ((MainActivity) getActivity()).updateEditWish(detailWish_id);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isInit && isVisibleToUser) {
            cheering_icon.setEnabled(true);
            if (isConnected()) {
                try {
                    JSONObject JObj = new JSONObject();
                    JObj.put("SQLite_id", detailWish_id);
                    JObj.put("device_id", device_id);
                    new ActionToSQL(QUERY).execute(JObj);
                } catch (JSONException e) {
                    Log.e("JSONError", e.toString());
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            }
        } else if (isInit && !isVisibleToUser) {
            items.clear();
            detailWish_blessing_adapter.notifyDataSetChanged();
            ((MainActivity) getActivity()).pageHistory.clear();
        }
    }

    class dialogOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cancel_btn) {
                dialog.dismiss();
            } else if (v.getId() == R.id.confirm_btn) {
                cursor = db.rawQuery("SELECT Public FROM wish WHERE _Id=?", new String[]{detailWish_id});
                cursor.moveToNext();
                if (cursor.getInt(0) == 1) {
                    JSONObject JObj = new JSONObject();
                    try {
                        JObj.put("SQLite_id", detailWish_id);
                        JObj.put("device_id", device_id);
                        new ActionToSQL(DELETE).execute(JObj);
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    }
                }
                cursor.close();
                dialog.dismiss();
                WishList f = (WishList) ((MainActivity) getActivity()).fm.getFragments().get(0);
                f.deleteFromDetail(detailWish_id);
                ((MainActivity) getActivity()).shouldRefresh = true;
                ((MainActivity) getActivity()).onBackPressed();
            }
        }
    }

    class ActionToSQL extends AsyncTask<JSONObject, Void, String> {
        private int mode;

        public ActionToSQL(int mode) {
            this.mode = mode;
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            String jsonString = "";
            if (mode == DELETE) {
                jsonString = jsonParser.makeHttpRequest(ip + "/deleteWish.php", params[0]);
            } else {
                jsonString = jsonParser.makeHttpRequest(ip + "/queryData.php", params[0]);
            }
            return jsonString;
        }

        @Override
        protected void onPostExecute(String JSONString) {
            if (JSONString.equals("Fail")) {
                Toast.makeText(getActivity(), getString(R.string.unstable_network), Toast.LENGTH_SHORT).show();
                JSONString = "";
            } else {
                if (mode == QUERY) {
                    try {
                        JSONArray JArray = new JSONArray(JSONString);
                        JSONObject JObj = JArray.getJSONObject(0);
                        cheeringNumTv.setText(Integer.toString(JObj.getInt("cheering")));
                        boolean k = (JObj.getInt("self_cheered") == 1);
                        cheering_icon.setChecked(k);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("Cheering", JObj.getInt("cheering"));
                        db.update("wish", contentValues, "_Id=?", new String[]{detailWish_id});
                        if(JObj.getString("blessing").equals("null")){
                            return;
                        }
                        for (int i = 0; i < JArray.length(); i++) {
                            JObj = JArray.getJSONObject(i);
                            String city = JObj.getString("city");
                            if (!city.equals("")) {
                                city = city + ", ";
                            }
                            items.add(new DetailWish_blessing_item(JObj.getString("blessing"), city + JObj.getString("country"), JObj.getString("time"), image[(int) ((Math.random() * 5))]));
                        }
                        detailWish_blessing_adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    }
                }
            }
        }
    }

    class sendCheering extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            String JSONString = jsonParser.makeHttpRequest(ip + "/cheering.php", params[0]);
            return JSONString;
        }

        @Override
        protected void onPostExecute(String JSONString) {
            cheering_icon.setEnabled(true);
            if (JSONString.equals("Fail")) {
                Toast.makeText(getActivity(), getString(R.string.unstable_network), Toast.LENGTH_SHORT).show();
                JSONString = "";
            } else {
                try {
                    JSONArray JArray = new JSONArray(JSONString);
                    JSONObject JObj = JArray.getJSONObject(0);
                    cheeringNumTv.setText(Integer.toString(JObj.getInt("cheering")));
                } catch (JSONException e) {
                    Log.e("JSONError", e.toString());
                }
            }
        }
    }
}

