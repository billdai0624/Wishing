package com.intern.ab.starwish.ui.blessing;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.intern.ab.starwish.model.Blessing_item;
import com.intern.ab.starwish.util.DBHelper;
import com.intern.ab.starwish.util.JSONParser;
import com.intern.ab.starwish.ui.MainActivity;
import com.intern.ab.starwish.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyBless extends Fragment {
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    static String device_id;
    private ArrayList<Blessing_item> blessing_items;
    private BlessingAdapter blessingAdapter;
    private ListView blessingList;
    private ImageView myBless_hint;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private boolean isInit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.empty_menu, menu);
        ((MainActivity) getActivity()).actionBar.setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).actionBar_title.setText(getString(R.string.myBless_title));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        isInit = true;
        View rootView = inflater.inflate(R.layout.my_bless, container, false);
        device_id = ((MainActivity) getActivity()).device_id;
        blessingList = (ListView) rootView.findViewById(R.id.blessingList);
        myBless_hint = (ImageView) rootView.findViewById(R.id.myBless_hint);
        blessing_items = new ArrayList<>();
        initDB();
        blessingAdapter = new BlessingAdapter(getActivity(), R.layout.custom_blessing_item, blessing_items);
        blessingList.setAdapter(blessingAdapter);
        blessingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                blessingAdapter.toDetail(position, blessing_items.get(position).getCheeringNum(), blessing_items.get(position).getId());
            }
        });
        if (blessing_items.isEmpty()) {
            myBless_hint.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    public void initDB() {
        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isInit && isVisibleToUser) {
            if (isConnected()) {
                JSONObject JObj = new JSONObject();
                try {
                    JObj.put("device_id", device_id);
                } catch (JSONException e) {
                    Log.e("JSONError", e.toString());
                }
                new getFromSQL().execute(JObj);
            } else {
                Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            }
        } else {

        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    class getFromSQL extends AsyncTask<JSONObject, Void, String> {
        public getFromSQL() {
            super();
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            String JSONString = jsonParser.makeHttpRequest(ip + "/getBlessedWish.php", params[0]);
            return JSONString;
        }

        @Override
        protected void onPostExecute(String JSONString) {
            if (JSONString.equals("Fail")) {
                Toast.makeText(getActivity(), getString(R.string.unstable_network), Toast.LENGTH_SHORT).show();
                JSONString = "";
            } else {
                try {
                    JSONArray JArray = new JSONArray(JSONString);
                    blessing_items.clear();
                    for (int i = 0; i < JArray.length(); i++) {
                        JSONObject JObj = JArray.getJSONObject(i);
                        boolean realized = (JObj.getInt("realized") == 1);
                        blessing_items.add(new Blessing_item(JObj.getInt("wish_id"), JObj.getString("wish"), JObj.getString("time"), JObj.getInt("cheering"), realized));
                    }
                    blessingAdapter.notifyDataSetChanged();
                    if (!blessing_items.isEmpty()) {
                        myBless_hint.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    Log.e("JSONError", e.toString());
                }
            }
        }
    }
}

