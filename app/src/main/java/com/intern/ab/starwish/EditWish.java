package com.intern.ab.starwish;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class EditWish extends AppCompatActivity implements LocationListener {
    static final int INSERT = 0;
    static final int UPDATE = 1;
    static final int DELETE = 2;
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    static final String geoAPI = "AIzaSyC09zMRrsFJhnEX9puE494TYRbsi9tIpAU";
    static String device_id;
    ActionBar actionBar;
    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor cursor;
    Menu actionBarMenu;
    EditText editWish;
    CheckBox edit_allowPublic;
    Bundle bun;
    Intent intent;
    String detailWish;
    String detailWish_id;
    boolean getService;
    InputMethodManager imm;
    private LocationManager lm;
    private String bestProvider;
    private Location location;
    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_wish);
        device_id = getDevice_id();
        init();
        initActionBar();
        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Toast.makeText(getApplicationContext(), "Check whether your disk is full", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            getService = true;
            locationInitialization();
        } else {
            Toast.makeText(this, "Please activate GPS Service", Toast.LENGTH_SHORT).show();
        }

    }

    public String getDevice_id() {
        final SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        String device_id = prefs.getString("device_id", "");
        if (device_id.equals("")) {
            SharedPreferences.Editor editor = prefs.edit();
            device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            editor.putString("device_id", device_id);
            editor.commit();
        }
        return device_id;
    }

    public void init() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        editWish = (EditText) this.findViewById(R.id.edit_wish);
        editWish.requestFocus();
        /*try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(editWish, R.drawable.cursor);
        } catch (Exception ignored) {
        }*/
        detailWish = getIntent().getStringExtra("detailWish");
        editWish.setText(getIntent().getStringExtra("detailWish"));
        editWish.setSelection(detailWish.length());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        edit_allowPublic = (CheckBox) this.findViewById(R.id.edit_allowPublic);
        edit_allowPublic.setChecked(getIntent().getBooleanExtra("detailPublic", true));
    }

    public void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.editWish_title));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void locationInitialization() {
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider = lm.getBestProvider(criteria, true);
//        Location location = lm.getLastKnownLocation(bestProvider);
        getLocation();
    }

    public void getLocation() {

        if (null != lm.getLastKnownLocation(lm.GPS_PROVIDER)) {
            lm.requestLocationUpdates(bestProvider, 2000, 0, this);
            location = lm.getLastKnownLocation(bestProvider);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        } else if (null != lm.getLastKnownLocation(lm.NETWORK_PROVIDER)) {
            int i = 0;
            while (location == null) {
                lm.requestLocationUpdates(lm.GPS_PROVIDER, 2000, 0, this);
                location = lm.getLastKnownLocation(bestProvider);
                if (i++ == 500) {
                    location = lm.getLastKnownLocation(lm.NETWORK_PROVIDER);
                    break;
                }
            }
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        } else {
            cursor = db.rawQuery("SELECT longitude, latitude FROM wish ORDER BY _Id DESC LIMIT 1", null);
            cursor.moveToNext();
            if (cursor.getDouble(0) != 0 && cursor.getDouble(1) != 0) {
                longitude = cursor.getDouble(0);
                latitude = cursor.getDouble(1);
            } else {
                Toast.makeText(this, "Your location is unavailable", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
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
            serverAddress = new URL("https://maps.googleapis.com/maps/api/geocode/json?language="
                    + new Locale(getApplication().getResources().getConfiguration().locale.getLanguage())
                    + "&latlng=" + Double.toString(latitude) + "," + Double.toString(longitude)
                    + "&key=" + geoAPI);
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

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
              /*  intent = new Intent();
                bun = new Bundle();
                bun.putString("editWish", editWish.getText().toString());
                bun.putString("editTime", android.text.format.DateFormat.format("yyyy-MM-dd hh:mm", new Date().getTime()).toString());
                bun.putBoolean("edit_allowPublic", edit_allowPublic.isChecked());
                intent.putExtras(bun);*/
                if (isConnected()) {
                    if ("".equals(editWish.getText().toString().trim()) || null == db) {
                        Toast.makeText(getApplicationContext(), getString(R.string.wish_denial_toast), Toast.LENGTH_SHORT).show();
                    } else {
                        detailWish_id = getIntent().getStringExtra("detailWish_id");
                        cursor = db.rawQuery("SELECT Public FROM wish WHERE _Id=?", new String[]{detailWish_id});
                        cursor.moveToNext();
                        int oldPublic = cursor.getInt(0);
                        cursor.close();
                        int newPublic = (edit_allowPublic.isChecked()) ? 1 : 0;
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("Wish", editWish.getText().toString());
                        contentValues.put("DateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
                        if (longitude != 0 || latitude != 0) {
                            contentValues.put("Longitude", longitude);
                            contentValues.put("Latitude", latitude);
                        }
                        contentValues.put("Public", newPublic);
                        db.update("wish", contentValues, "_Id=?", new String[]{detailWish_id});
                        JSONObject JObj = new JSONObject();
                        try {
                            JObj.put("Wish", editWish.getText().toString());
                            JObj.put("DateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
                            JObj.put("Longitude", longitude);
                            JObj.put("Latitude", latitude);
                            JObj.put("Cheering", 0);
                            JObj.put("country", getApplication().getResources().getConfiguration().locale.getDisplayCountry());
                            JObj.put("SQLite_id", Integer.valueOf(detailWish_id));
                            JObj.put("Realized", 0);
                            JObj.put("device_id", device_id);
                        } catch (JSONException e) {
                            Log.e("JSonError", e.toString());
                        }
                        if (oldPublic == 0 && newPublic == 0) {
                        } else if (oldPublic == 0 && newPublic == 1) {
                            //Insert into SQL
                            new SendToSQL(INSERT).execute(JObj);
                        } else if (oldPublic == 1 && newPublic == 0) {
                            //Delete from SQL
                            new SendToSQL(DELETE).execute(JObj);
                        } else {
                            //Update SQL
                            new SendToSQL(UPDATE).execute(JObj);
                        }
                        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                setResult(RESULT_OK);
                finish();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (this.getCurrentFocus() != null) {
                if (this.getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onLocationChanged(Location location) {
        lm.requestLocationUpdates(bestProvider, 3000, 0, this);
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
    protected void onResume() {
        super.onResume();
        if (getService) {
            lm.requestLocationUpdates(bestProvider, 2000, 0, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getService) {
            lm.removeUpdates(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    class SendToSQL extends AsyncTask<JSONObject, Void, Void> {
        private int mode;

        public SendToSQL(int mode) {
            super();
            this.mode = mode;
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            String city = "";
            JSONObject ret = getLocationName();
            JSONObject locationName;
            try {
                JSONArray array = ret.getJSONArray("results");
                for (int i = 0; i < array.length(); i++) {
                    JSONArray array1 = array.getJSONObject(i).getJSONArray("address_components");
                    for (int k = array1.length() - 1; k >= 0; k--) {
                        locationName = array1.getJSONObject(k);
                        if (locationName.getJSONArray("types").getString(0).equals("locality")) {
                            city = locationName.getString("long_name");
                            break;
                        }
                    }
                    if (!city.equals("")) {
                        break;
                    }
                }
                params[0].put("city", city);
            } catch (JSONException e) {
                Log.e("JSONError", e.toString());
            }
            JSONParser jsonParser = new JSONParser();
            switch (mode) {
                case INSERT:
                    jsonParser.makeHttpRequest(ip + "/newWish.php", params[0]);
                    break;
                case UPDATE:
                    jsonParser.makeHttpRequest(ip + "/updateWish.php", params[0]);
                    break;
                case DELETE:
                    jsonParser.makeHttpRequest(ip + "/deleteWish.php", params[0]);
                    break;
                default:
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}
