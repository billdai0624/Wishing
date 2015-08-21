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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.provider.Settings.Secure;

public class MakeWish extends AppCompatActivity implements LocationListener {
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.112";
    static final String geoAPI = "AIzaSyC09zMRrsFJhnEX9puE494TYRbsi9tIpAU";
    static String device_id;
    ImageButton send;
    Button skip;
    EditText wish;
    CheckBox allowPublic;
    Intent intent;
    Bundle bun;
    InputMethodManager imm;
    private SQLiteDatabase db;
    private Cursor cursor;
    private DBHelper dbHelper;
    private LocationManager lm;
    private String bestProvider;
    private boolean getService = false;
    private Location location;
    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_wish);
        device_id = getDevice_id();
        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Toast.makeText(getApplicationContext(), "Check whether your disk is full", Toast.LENGTH_LONG).show();
        }
        init();
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            getService = true;
            locationInitialization();
        } else {
            Toast.makeText(this, "Please activate GPS Service", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }

    public String getDevice_id() {
        final SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        String device_id = prefs.getString("device_id", "");
        if (device_id.equals("")) {
            SharedPreferences.Editor editor = prefs.edit();
            device_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
            editor.putString("device_id", device_id);
            editor.commit();
        }
        return device_id;
    }

    public void init() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        wish = (EditText) this.findViewById(R.id.myWish);
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(wish, R.drawable.cursor);
        } catch (Exception ignored) {
        }
        wish.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        allowPublic = (CheckBox) this.findViewById(R.id.allowPublic);
        skip = (Button) this.findViewById(R.id.skip);
        if (null != getIntent().getExtras()) {
            skip.setVisibility(View.GONE);
        }
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplication(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        send = (ImageButton) this.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(wish.getText().toString().trim()) || null == db) {
                    Toast.makeText(getApplicationContext(), getString(R.string.wish_denial_toast), Toast.LENGTH_LONG).show();
                } else {

                    intent = new Intent();
                    bun = new Bundle();
                    int i = (allowPublic.isChecked()) ? 1 : 0;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("Wish", wish.getText().toString());
                    contentValues.put("DateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
                    contentValues.put("Longitude", longitude);
                    contentValues.put("Latitude", latitude);
                    contentValues.put("Cheering", 0);
                    contentValues.put("Public", i);
                    contentValues.put("Realized", 0);
                    db.insertOrThrow("wish", null, contentValues);
                    if (allowPublic.isChecked()) {
                        cursor = db.rawQuery("SELECT _Id FROM wish ORDER BY _Id DESC LIMIT 1", null);
                        cursor.moveToNext();
                        int SQLite_id = cursor.getInt(0);
                        cursor.close();
                        JSONObject JObj = new JSONObject();
                        try {
                            JObj.put("Wish", wish.getText().toString());
                            JObj.put("DateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
                            JObj.put("Longitude", longitude);
                            JObj.put("Latitude", latitude);
                            JObj.put("Cheering", 0);
                            JObj.put("Realized", 0);
                            JObj.put("country", getApplication().getResources().getConfiguration().locale.getDisplayCountry());
                            JObj.put("SQLite_id", SQLite_id);
                            JObj.put("device_id", device_id);
                            new SendToSQL().execute(JObj);
                        } catch (JSONException e) {
                            Log.e("JSONError", e.toString());
                        }
                    }
                    bun.putString("newWish", wish.getText().toString());
                    bun.putString("time", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date().getTime()));
                    intent.setClass(getApplication(), MakeWish_animation.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    if (null == getIntent().getExtras()) {
                        bun.putBoolean("fromMain", false);
                        intent.putExtras(bun);
                        startActivity(intent);
                        finish();
                    } else {
                        bun.putBoolean("fromMain", true);
                        intent.putExtras(bun);
                        startActivityForResult(intent, 1);
                    }

                }
            }
        });
    }

    public void locationInitialization() {
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider = lm.getBestProvider(criteria, true);
        location = lm.getLastKnownLocation(bestProvider);

        getLocation();

    }

    public void getLocation() {

        if (null != lm.getLastKnownLocation(lm.GPS_PROVIDER)) {
            lm.requestLocationUpdates(bestProvider, 2000, 0, this);
            //location = lm.getLastKnownLocation(bestProvider);
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
            cursor = db.rawQuery("SELECT longitude, latitude FROM wish ORDER BY _Id ASC LIMIT 1", null);
            if (cursor.moveToNext()) {
                if (cursor.getDouble(0) != 0 && cursor.getDouble(1) != 0) {
                    longitude = cursor.getDouble(0);
                    latitude = cursor.getDouble(1);
                }
            } else {
                Toast.makeText(this, "Your location is unavailable", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            setResult(RESULT_FIRST_USER, data);
            finish();
        }
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
    protected void onResume() {
        super.onResume();
        location = lm.getLastKnownLocation(bestProvider);
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

    @Override
    public void onLocationChanged(Location location) {
        //this.location = location;

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

    class SendToSQL extends AsyncTask<JSONObject, Void, Void> {
        public SendToSQL() {
            super();
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
            } catch (Exception e) {
                Log.e("Can't get location", e.toString());
            }
            JSONParser jsonParser = new JSONParser();
            jsonParser.makeHttpRequest(ip + "/newWish.php", params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}
