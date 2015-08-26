package com.intern.ab.starwish;

import android.app.Dialog;
import android.content.ComponentName;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;


public class DetailBlessingList extends Fragment {
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    static final String geoAPI = "AIzaSyC09zMRrsFJhnEX9puE494TYRbsi9tIpAU";
    static final String admin1 = "administrative_area_level_1";
    static final String admin2 = "administrative_area_level_2";
    static String device_id;
    private ListView blessingList;
    private DetailBlessingAdapter detailBlessing_adapter;
    private List<DetailBlessing_blessing_item> items;
    private View rootView;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private TextView detailWishTv;
    private TextView detailTimeTv;
    private TextView cheeringNumTv;
    private LinearLayout sendBlessing_LL;
    private CheckBox cheeringIcon;
    private EditText blessingET;
    private ImageButton sendBlessing;
    private String detailWish;
    private String detailTime;
    private int cheeringNum;
    private int detailWish_id;
    private int[] image;
    private Dialog dialog;
    private boolean isInit;
    private boolean blessed;
    private int position;
    private double longitude;
    private double latitude;
    private InputMethodManager imm;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.empty_menu, menu);
        ((MainActivity) getActivity()).actionBar.setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).actionBar_title.setText(getString(R.string.bless));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ((MainActivity) getActivity()).onBackPressed();
        }
        return true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        isInit = true;
        rootView = inflater.inflate(R.layout.detail_blessing_list, container, false);
        imm = ((MainActivity) getActivity()).imm;
        device_id = ((MainActivity) getActivity()).device_id;
        detailWishTv = (TextView) rootView.findViewById(R.id.blessing_detailWish);
        detailTimeTv = (TextView) rootView.findViewById(R.id.blessing_detailTime);
        cheeringNumTv = (TextView) rootView.findViewById(R.id.blessing_cheeringNum);
        cheeringIcon = (CheckBox) rootView.findViewById(R.id.blessing_cheeringIcon);
        cheeringIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.blessing_cheeringIcon) {
                    CheckBox cb = (CheckBox) v;
                    cb.setChecked(cb.isChecked());
                    JSONObject JObj = new JSONObject();
                    try {
                        if (cb.isChecked()) {
                            JObj.put("newCheering", Integer.valueOf(cheeringNumTv.getText().toString()) + 1);
                            JObj.put("cancel", 0);
                        } else {
                            JObj.put("newCheering", Integer.valueOf(cheeringNumTv.getText().toString()) - 1);
                            JObj.put("cancel", 1);
                        }
                        JObj.put("_Id", detailWish_id);
                        JObj.put("device_id", device_id);
                        new sendCheering().execute(JObj);
                        cheeringIcon.setEnabled(false);
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    }
                }
            }
        });
        sendBlessing_LL = (LinearLayout) rootView.findViewById(R.id.sendBlessing_LL);
        blessingET = (EditText) rootView.findViewById(R.id.blessingET);
        sendBlessing = (ImageButton) rootView.findViewById(R.id.sendBlessing);
        sendBlessing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    sendBlessing.setEnabled(false);
                    try {
                        String blessing = blessingET.getText().toString().trim();
                        if (blessing.equals("")) {
                            return;
                        }
                        JSONObject JObj = new JSONObject();
                        JObj.put("blessing", blessing);
                        JObj.put("id", detailWish_id);
                        //JObj.put("country", getActivity().getResources().getConfiguration().locale.getDisplayCountry());
                        JObj.put("device_id", device_id);
                        JObj.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
                        new SendBlessingToSQL().execute(JObj);
                        blessingET.setText("");
                        ((MainActivity) getActivity()).blessed = true;
                    } catch (JSONException e) {
                        Log.e("JSONError", e.toString());
                    } catch (Exception e) {
                        Log.e("", e.toString());
                        sendBlessing.setEnabled(true);
                    }
                } else {
                    dialog = new Dialog(getActivity(), R.style.MyDialog);
                    dialog.setContentView(R.layout.custom_dialog);
                    Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
                    Button confirm = (Button) dialog.findViewById(R.id.confirm_btn);
                    TextView question = (TextView) dialog.findViewById(R.id.dialogQuestion);
                    question.setText(getString(R.string.network_unavailable));
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
                    Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(blessingET.getWindowToken(), 0);
                } else {
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                }
            }
        });
        blessingList = (ListView) rootView.findViewById(R.id.detailBlessingList);
        items = new ArrayList<DetailBlessing_blessing_item>();
        detailBlessing_adapter = new DetailBlessingAdapter(getActivity(), R.layout.custom_detail_blessing_item, items);
        blessingList.setAdapter(detailBlessing_adapter);
        return rootView;
    }

    public void getWish(Bundle b, int cheeringNum, int id) {
        blessingET.clearFocus();
        items.clear();
        detailBlessing_adapter.notifyDataSetChanged();
        if (null != b) {
            detailWish = b.getString("detailWish");
            detailTime = b.getString("detailTime");
        }
        detailWish_id = id;
        //initDB();
        try {
            JSONObject JObj = new JSONObject();
            JObj.put("wish_id", detailWish_id);
            JObj.put("device_id", device_id);
            new GetBlessingFromSQL().execute(JObj);
        } catch (JSONException e) {
            Log.e("JSONError", e.toString());
        }
        detailWishTv.setText(detailWish);
        detailTimeTv.setText(detailTime);
        cheeringNumTv.setText(Integer.toString(cheeringNum));
        sendBlessing_LL.setVisibility(GONE);
        detailBlessing_adapter.notifyDataSetChanged();
    }

    public void getWishFromWall(String wish, String time, int cheeringNum, int id, int position, boolean cheered) {
        this.position = position;
        cheeringIcon.setChecked(cheered);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        blessingET.requestFocus();

        WishWall f = (WishWall) ((MainActivity) getActivity()).fm.getFragments().get(2);
        longitude = f.longitude;
        latitude = f.latitude;
        items.clear();
        detailBlessing_adapter.notifyDataSetChanged();
        detailWish_id = id;
        try {
            JSONObject JObj = new JSONObject();
            JObj.put("wish_id", id);
            JObj.put("device_id", device_id);
            new GetBlessingFromSQL().execute(JObj);
        } catch (JSONException e) {
            Log.e("JSONError", e.toString());
        }
        detailWishTv.setText(wish);
        detailTimeTv.setText(time);
        cheeringNumTv.setText(Integer.toString(cheeringNum));
        sendBlessing_LL.setVisibility(VISIBLE);
        detailBlessing_adapter.notifyDataSetChanged();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
    /*public void initDB() {
        dbHelper = new DBHelper(getActivity());
        db = dbHelper.getReadableDatabase();
    }*/

    /*   @Override
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
               }

               cursor = db.rawQuery("SELECT Wish, DateTime, Cheering FROM wish WHERE _Id=?", new String[]{detailWish_id});
               cursor.moveToNext();
               detailWish = cursor.getString(0);
               detailWishTv.setText(detailWish);
               detailTimeTv.setText(cursor.getString(1));
               //cheeringNumTv.setText(Integer.toString(cursor.getInt(2)));
               cursor.close();
               ((MainActivity) getActivity()).updateEditWish(detailWish_id);
           }
       }*/
    private void requestSoftInput(View v) {
        //if (imm.isActive()) {
        imm.showSoftInput(v, 0);
        //} else {
        //    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //}
    }

    public JSONObject getLocationName() {
        String localityName;
        HttpURLConnection connection;
        URL serverAddress;
        BufferedReader reader;
        JSONObject JObj = null;
        StringBuffer sb = new StringBuffer();
        try {
            // build the URL using the latitude & longitude you want to lookup
            serverAddress = new URL("https://maps.googleapis.com/maps/api/geocode/json?language=en&latlng=" + Double.toString(latitude) + "," + Double.toString(longitude) + "&key=" + geoAPI);
            // Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            JObj = new JSONObject(sb.toString());
            return JObj;
        } catch (Exception e) {
            Log.e("URLError", e.toString());
        }
        return JObj;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isInit && getUserVisibleHint()) {
            if (blessingET.hasFocus()) {
                requestSoftInput(blessingET);
            } else {
                //blessingET.requestFocus();
                //requestSoftInput(blessingET);

            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isInit && isVisibleToUser) {
            try {
                if (blessingET.hasFocus()) {
                    requestSoftInput(blessingET);
                } else {
                    //blessingET.requestFocus();
                    //requestSoftInput(blessingET);
                }
                JSONObject JObj = new JSONObject();
                JObj.put("id", detailWish_id);
                JObj.put("deviceId", device_id);
            } catch (JSONException e) {
                Log.e("JSONError", e.toString());
            }
        } else if (isInit && !isVisibleToUser) {
            ((MainActivity) getActivity()).pageHistory.clear();
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(blessingET.getWindowToken(), 0);
            } else {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
            blessingET.setText("");
        }
    }

    class dialogOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cancel_btn) {
                dialog.dismiss();
            } else if (v.getId() == R.id.confirm_btn) {
                Intent intent = new Intent("/");
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                intent.setComponent(cn);
                intent.setAction("android.intent.action.VIEW");
                startActivity(intent);
            }
        }
    }

    class GetBlessingFromSQL extends AsyncTask<JSONObject, Void, String> {
        public GetBlessingFromSQL() {

        }

        @Override
        protected String doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            String jsonString = jsonParser.makeHttpRequest(ip + "/getBlessing.php", params[0]);
            return jsonString;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            items.clear();
            try {
                JSONArray JArray = new JSONArray(jsonString);
                JSONObject JObj;
                for (int i = 0; i < JArray.length(); i++) {
                    JObj = JArray.getJSONObject(i);
                    String city = JObj.getString("city");
                    if (!city.equals("")) {
                        city = city + ", ";
                    }
                    items.add(new DetailBlessing_blessing_item(JObj.getString("blessing"), city + JObj.getString("country"), JObj.getString("time")));
                }
                detailBlessing_adapter.notifyDataSetChanged();
                blessingList.setSelection(blessingList.getCount() - 1);
            } catch (JSONException e) {
                Log.e("JSONError", e.toString());
            }
            if (items.isEmpty()) {
                ((MainActivity) getActivity()).blessed = false;
            } else {
                ((MainActivity) getActivity()).blessed = true;
            }
        }
    }

    class SendBlessingToSQL extends AsyncTask<JSONObject, Void, String> {
        public SendBlessingToSQL() {
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            String city = "";
            String country = "";
            JSONObject ret = getLocationName();
            JSONObject locationName;
            try {
                JSONArray array = ret.getJSONArray("results");
                for (int i = 0; i < array.length(); i++) {
                    JSONArray array1 = array.getJSONObject(i).getJSONArray("address_components");
                    for (int k = array1.length() - 1; k >= 0; k--) {
                        locationName = array1.getJSONObject(k);
                        if (country.equals("") && locationName.getJSONArray("types").getString(0).equals("country")) {
                            country = locationName.getString("long_name");
                        }
                        if (city.equals("") && ((locationName.getJSONArray("types").getString(0).equals("locality"))
                                || locationName.getJSONArray("types").getString(0).equals(admin1)
                                || locationName.getJSONArray("types").getString(0).equals(admin2))) {
                            city = locationName.getString("long_name");
                            break;
                        }
                    }
                    if (!city.equals("") && !country.equals("")) {
                        break;
                    }
                }
                params[0].put("city", city);
                params[0].put("country", country);
            } catch (JSONException e) {
                Log.e("JSONError", e.toString());
            }
            JSONParser jsonParser = new JSONParser();
            String jsonString = jsonParser.makeHttpRequest(ip + "/sendBlessing.php", params[0]);
            return jsonString;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            sendBlessing.setEnabled(true);
            items.clear();
            try {
                JSONArray JArray = new JSONArray(jsonString);

                JSONObject JObj;
                for (int i = 0; i < JArray.length(); i++) {
                    JObj = JArray.getJSONObject(i);
                    String city = JObj.getString("city");
                    if (!city.equals("")) {
                        city = city + ", ";
                    }
                    items.add(new DetailBlessing_blessing_item(JObj.getString("blessing"), city + JObj.getString("country"), JObj.getString("time")));
                }
                detailBlessing_adapter.notifyDataSetChanged();
                blessingList.setSelection(blessingList.getCount() - 1);
            } catch (JSONException e) {
                Log.e("JSONError", e.toString());
            }
            ((MainActivity) getActivity()).blessed = true;
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
            try {
                JSONArray JArray = new JSONArray(JSONString);
                JSONObject JObj = JArray.getJSONObject(0);
                cheeringNumTv.setText(Integer.toString(JObj.getInt("cheering")));
                ((MainActivity) getActivity()).alterCheeringState(cheeringIcon.isChecked(), position);

            } catch (JSONException e) {
                Log.e("JSONError", e.toString());
            }
            cheeringIcon.setEnabled(true);
        }
    }
}

