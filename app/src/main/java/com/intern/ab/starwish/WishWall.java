package com.intern.ab.starwish;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.widget.AbsListView.OnScrollListener;

public class WishWall extends Fragment implements LocationListener {
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    static final int HOT = 0;
    static final int NEW = 1;
    static final int NEAR = 2;
    static final int MIN = 1;
    static final int HOUR = 2;
    static final int DAY = 3;
    static final int MONTH = 4;
    static final int YEAR = 5;
    static final int METER = 1;
    static final int KILOMETER = 2;
    static String device_id;
    public ListView publicWish_list;
    public List<PublicWish_item> items;
    public PublicWishAdapter publicWishItemAdapter;
    public Double longitude = 0.0;
    public Double latitude = 0.0;
    boolean shouldRefresh = true;
    boolean isRefreshing = false;
    boolean shouldLoadData = true;
    boolean isLoadingData = false;
    private AnimationDrawable loadingAnim;
    private ImageView loading;
    private Drawable img;
    private int pressed_color;
    private LocationManager lm;
    private String bestProvider;
    private boolean getService = false;
    private Location location;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private RadioGroup category;
    private RadioButton hot;
    private RadioButton newest;
    private RadioButton near;
    private View rootView;
    private View footer;
    private int lastData_id = -1;
    private int lowerBound = 0;
    private JSONObject JObj;
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
        ((MainActivity) getActivity()).actionBar_title.setText(getString(R.string.wishWall_title));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        isInit = true;
        rootView = inflater.inflate(R.layout.wish_wall, container, false);
        device_id = ((MainActivity) getActivity()).device_id;
        initDB();
        initView();
        return rootView;
    }

    public void initView() {
        publicWish_list = (ListView) rootView.findViewById(R.id.publicWish_list);
        img = ContextCompat.getDrawable(getActivity(), R.drawable.btn_stat_switch);
        pressed_color = getActivity().getResources().getColor(R.color.tab_underline);
        category = (RadioGroup) rootView.findViewById(R.id.category);
        hot = (RadioButton) rootView.findViewById(R.id.hot);
        newest = (RadioButton) rootView.findViewById(R.id.newest);
        near = (RadioButton) rootView.findViewById(R.id.near);
        newest.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
        newest.setTextColor(pressed_color);
        hot.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        hot.setTextColor(Color.WHITE);
        near.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        near.setTextColor(Color.WHITE);
        category.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                items.clear();
                publicWishItemAdapter.clearStates();
                footer.setVisibility(View.VISIBLE);
                publicWish_list.removeFooterView(footer);
                publicWish_list.addFooterView(footer);
                switch (checkedId) {
                    case R.id.hot:

                        publicWish_list.setSelection(0);
                        hot.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
                        hot.setTextColor(pressed_color);
                        near.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        near.setTextColor(Color.WHITE);
                        newest.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        newest.setTextColor(Color.WHITE);
                        if (isConnected()) {
                            lowerBound = 0;
                            cheeringSorted();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.newest:
                        publicWish_list.setSelection(0);
                        newest.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
                        newest.setTextColor(pressed_color);
                        hot.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        hot.setTextColor(Color.WHITE);
                        near.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        near.setTextColor(Color.WHITE);
                        if (isConnected()) {
                            lastData_id = -1;
                            timeSorted();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.near:
                        publicWish_list.setSelection(0);
                        near.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img);
                        near.setTextColor(pressed_color);
                        newest.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        newest.setTextColor(Color.WHITE);
                        hot.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        hot.setTextColor(Color.WHITE);
                        if (isConnected()) {
                            distanceSorted();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                }
            }
        });
        items = new ArrayList<PublicWish_item>();
        publicWishItemAdapter = new PublicWishAdapter(getActivity(), R.layout.custom_public_wish_item, items);
        footer = LayoutInflater.from(getActivity()).inflate(R.layout.footer, null);
        loading = (ImageView) footer.findViewById(R.id.loading);
        loadingAnim = (AnimationDrawable) loading.getBackground();
        publicWish_list.addFooterView(footer);
        publicWish_list.setAdapter(publicWishItemAdapter);
        publicWish_list.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    //當ListView拉到頂或底時
                    /*if (shouldRefresh) {//當ListView拉到頂
                        if (!isRefreshing)Refresh();//沒在更新資料時
                        publicWish_list.setSelection(1);//不管更不更新，都移到第一項
                    }*/
                    if (shouldLoadData && !isLoadingData) {
                        loadingAnim.start();
                        if (isConnected()) {
                            if (hot.isChecked()) {
                                lowerBound += 20;
                                cheeringSorted();
                            } else if (newest.isChecked()) {
                                if (!items.isEmpty()) {
                                    lastData_id = items.get(items.size() - 1).getWish_id();
                                    timeSorted();
                                }
                            } else if (near.isChecked()) {
                                distanceSorted();
                            }

                            //RunAnim runAnim=new RunAnim();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                        }
                    }
                    /*else{
                        loadingAnim.stop();
                    }*/
                    //當ListView拉到底,且沒在載入資料時
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                shouldLoadData = false;
                shouldRefresh = false;

                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    //拉到底時
                    shouldLoadData = true;
                     /*else {//只是測試用，如果超過60筆資料就不要再載入了
                        footer.setVisibility(View.GONE);
                        publicWish_list.removeFooterView(footer);
                        shouldRefresh = false;
                    }*/
                }
            }
        });
        //timeSorted();
        /*items.add(new PublicWish_item("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ","qq","qwe",123));
        items.add(new PublicWish_item("呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵呵","qq","qwe",123));
*/
        //publicWish_list.setAdapter(publicWishItemAdapter);
        /*publicWish_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });*/

    }

    public void alterCheeringState(boolean cheered, int position) {
        publicWishItemAdapter.alterCheeredState(cheered, position);
    }

    public void alterBlessingState(boolean blessed, int position) {
        publicWishItemAdapter.alterBlessedState(blessed, position);
    }

    public void initDB() {
        dbHelper = ((MainActivity) getActivity()).dbHelper;
        db = dbHelper.getReadableDatabase();
    }

    public void timeSorted() {
        loadingAnim.start();
        JObj = new JSONObject();
        try {
            JObj.put("lastData_id", lastData_id);
            JObj.put("currentTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
            JObj.put("mode", NEW);
            JObj.put("device_id", device_id);
        } catch (JSONException e) {
            Log.e("JSONError", e.toString());
        }
        new ReadFromSQL(NEW).execute(JObj);
    }

    public void cheeringSorted() {
        loadingAnim.start();
        JObj = new JSONObject();
        try {
            JObj.put("lowerBound", lowerBound);
            JObj.put("currentTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
            JObj.put("mode", HOT);
            JObj.put("device_id", device_id);
        } catch (JSONException e) {
            Log.e("JSONError", e.toString());
        }
        new ReadFromSQL(HOT).execute(JObj);
    }

    public void distanceSorted() {
        loadingAnim.start();
        JObj = new JSONObject();
        try {
            JObj.put("longitude", longitude);
            JObj.put("latitude", latitude);
            JObj.put("currentTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
            JObj.put("mode", NEAR);
            JObj.put("device_id", device_id);
        } catch (JSONException e) {
            Log.e("JSONError", e.toString());
        }
        new ReadFromSQL(NEAR).execute(JObj);
    }

    public void locationInitialization() {
        lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider = lm.getBestProvider(criteria, true);
        location = lm.getLastKnownLocation(bestProvider);
        getLocation();
    }

    public void getLocation() {
        if (null != lm.getLastKnownLocation(lm.GPS_PROVIDER)) {
            lm.requestLocationUpdates(bestProvider, 3000, 0, this);
            location = lm.getLastKnownLocation(bestProvider);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        } else if (null != lm.getLastKnownLocation(lm.NETWORK_PROVIDER)) {
            int i = 0;
            while (location == null) {
                lm.requestLocationUpdates(lm.GPS_PROVIDER, 3000, 0, this);
                location = lm.getLastKnownLocation(bestProvider);
                if (i++ == 500) {
                    location = lm.getLastKnownLocation(lm.NETWORK_PROVIDER);
                    break;
                }
            }
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        } else {
            cursor = db.rawQuery("SELECT longitude, latitude FROM wish ORDER BY _Id ASC LIMIT 1", null);
            if (cursor.moveToNext()) {
                if (cursor.getDouble(0) != 0 && cursor.getDouble(1) != 0) {
                    longitude = cursor.getDouble(0);
                    latitude = cursor.getDouble(1);
                }
            } else {
                Toast.makeText(getActivity(), "Your location is unavailable", Toast.LENGTH_LONG).show();
            }

            cursor.close();
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

    @Override
    public void onLocationChanged(Location location) {
        //lm.requestLocationUpdates(bestProvider, 3000, 0, this);
        //getLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isInit && isVisibleToUser) {
            LocationManager status = (LocationManager) (getActivity().getSystemService(Context.LOCATION_SERVICE));
            if (lm != null) {
                lm.requestLocationUpdates(bestProvider, 3000, 0, this);
            } else if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                getService = true;
                locationInitialization();
            } else {
                Toast.makeText(getActivity(), "Please activate GPS Service to update your location", Toast.LENGTH_SHORT).show();
            }
            if (items.isEmpty()) {
                loadingAnim.start();
                if (isConnected()) {
                    timeSorted();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
            if (((MainActivity) getActivity()).shouldRefresh) {
                if (isConnected()) {
                    items.clear();
                    publicWishItemAdapter.clearStates();
                    footer.setVisibility(View.VISIBLE);
                    publicWish_list.removeFooterView(footer);
                    publicWish_list.addFooterView(footer);
                    switch (category.getCheckedRadioButtonId()) {
                        case R.id.hot:
                            lowerBound = 0;
                            cheeringSorted();
                            publicWish_list.setSelection(0);
                            break;
                        case R.id.newest:
                            lastData_id = -1;
                            timeSorted();
                            publicWish_list.setSelection(0);
                            break;
                        case R.id.near:
                            distanceSorted();
                            publicWish_list.setSelection(0);
                            break;
                    }
                    ((MainActivity) getActivity()).shouldRefresh = false;
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (lm != null) {
                lm.removeUpdates(this);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //location = lm.getLastKnownLocation(bestProvider);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != lm) {
            lm.removeUpdates(this);
        }
    }

    class ReadFromSQL extends AsyncTask<JSONObject, Void, String> {
        private int mode;

        public ReadFromSQL(int mode) {
            this.mode = mode;
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            String JSONString = jsonParser.makeHttpRequest(ip + "/sortWish.php", params[0]);
            return JSONString;
        }

        @Override
        protected void onPostExecute(String JSONString) {
            super.onPostExecute(JSONString);
            if (JSONString.equals("Fail")) {
                Toast.makeText(getActivity(), getString(R.string.unstable_network), Toast.LENGTH_LONG).show();
                JSONString = "";
            } else {
                if (mode == NEAR && items.size() != 0) {
                    items.clear();
                    publicWishItemAdapter.clearStates();
                }
                try {
                    JSONArray JArray = new JSONArray(JSONString);
                    if (JArray.length() == 0) {
                        footer.setVisibility(View.GONE);
                        publicWish_list.removeFooterView(footer);
                    }
                    for (int i = 0; i < JArray.length(); i++) {
                        JSONObject JObj = JArray.getJSONObject(i);
                        String time = JObj.getString("time");
                        String distance = "";
                        switch (JObj.getInt("timeUnit")) {
                            case MIN:
                                if (time.equals("1")) {
                                    time = time + " minute ago";
                                } else {
                                    time = time + " minutes ago";
                                }
                                break;
                            case HOUR:
                                if (time.equals("1")) {
                                    time = time + " hour ago";
                                } else {
                                    time = time + " hours ago";
                                }
                                break;
                            case DAY:
                                if (time.equals("1")) {
                                    time = time + " day ago";
                                } else {
                                    time = time + " days ago";
                                }
                                break;
                            case MONTH:
                                if (time.equals("1")) {
                                    time = time + " month ago";
                                } else {
                                    time = time + " months ago";
                                }
                                break;
                            case YEAR:
                                if (time.equals("1")) {
                                    time = time + " year ago";
                                } else {
                                    time = time + " years ago";
                                }
                                break;
                            default:
                        }
                        if (mode == NEAR) {
                            distance = JObj.getString("distance");
                            if (JObj.getInt("distanceUnit") == 1) {
                                distance = distance + "m";
                            } else {
                                distance = distance + "km";
                            }
                            publicWishItemAdapter.setDistanceVisible(true);
                        }
                        publicWishItemAdapter.setCheered(JObj.getInt("cheered"));
                        publicWishItemAdapter.setBlessed(JObj.getInt("blessed"));
                        items.add(new PublicWish_item(JObj.getString("wish"), distance, time, JObj.getInt("cheering"), JObj.getString("country"), JObj.getString("city")));
                        items.get(items.size() - 1).setWish_id(JObj.getInt("wish_id"));
                        items.get(items.size() - 1).setOriginalTime(JObj.getString("originalTime"));
                    }
                    publicWishItemAdapter.notifyDataSetChanged();
                    loadingAnim.stop();
                    if (mode == NEAR) {
                        publicWish_list.setSelection(0);
                    }
                } catch (JSONException e) {
                    Log.e("JSONError", e.toString());
                    //Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
