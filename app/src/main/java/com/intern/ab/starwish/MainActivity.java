package com.intern.ab.starwish;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "message";
    public static final String REG_ID = "registration_id";
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    private static final String TAG = "GCM :";
    private static final String APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static String device_id;
    public boolean shouldRefresh;
    DBHelper dbHelper;
    SQLiteDatabase db;
    ActionBar actionBar;
    View actionbarLayout;
    Menu actionBarMenu;
    FragmentManager fm;
    List<Fragment> fragments;
    ViewPager viewPager;
    FragmentPagerAdapter myPagerAdapter;
    RadioGroup tabsGroup;
    RadioButton wishList;
    RadioButton wishWall;
    RadioButton myBless;
    LinearLayout multipleLL;
    TextView actionBar_title;
    TextView selectNum;
    ImageButton makeWish;
    Button delete;
    Button selectAll;
    Stack<Integer> pageHistory;
    boolean saveToHistory;
    int detailItemPosition;
    boolean blessed;
    int detailBlessingItemPosition;
    LocationManager lm;
    InputMethodManager imm;
    private String SENDER_ID = "1037979663684";
    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private Context context = this;
    private String regid;

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        device_id = getDevice_id();
        fragments = new ArrayList<>();
        fragments.add(new WishList());
        fragments.add(new DetailWishList());
        fragments.add(new WishWall());
        fragments.add(new MyBless());
        fragments.add(new DetailBlessingList());
        fm = getSupportFragmentManager();
        fm.getFragments();
        dbHelper = new DBHelper(this);
        initActionBar();
        initView();
        viewPager.setCurrentItem(0, false);
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
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

    public void initView() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        pageHistory = new Stack<Integer>();
        multipleLL = (LinearLayout) this.findViewById(R.id.multiChoices);
        makeWish = (ImageButton) this.findViewById(R.id.makeWish);
        viewPager = (ViewPager) this.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(5);
        tabsGroup = (RadioGroup) this.findViewById(R.id.tabsGroup);
        wishList = (RadioButton) this.findViewById(R.id.tab1);
        wishWall = (RadioButton) this.findViewById(R.id.tab2);
        myBless = (RadioButton) this.findViewById(R.id.tab3);
        selectAll = (Button) this.findViewById(R.id.selectAll);
        delete = (Button) this.findViewById(R.id.cancelDelete);
        myPagerAdapter = new FragmentPagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                //Fragment f = fragments.get(position);
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };
        viewPager.setAdapter(myPagerAdapter);
        saveToHistory = true;
        makeWish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bun = new Bundle();
                intent.putExtras(bun);
                intent.setClass(getApplication(), MakeWish.class);
                startActivityForResult(intent, 0);
            }
        });
        tabsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.tab1:
                        viewPager.setCurrentItem(0, false);
                        //actionBar.setTitle("我的星願");
                        break;
                    case R.id.tab2:
                        viewPager.setCurrentItem(2, false);
                        //actionBar.setTitle("願望星空");
                        break;
                    case R.id.tab3:
                        viewPager.setCurrentItem(3, false);
                        //actionBar.setTitle("我的祝福");
                        break;
                    default:
                }
            }
        });

    }

    public void toDetailWish(int position, Bundle b) {
        detailItemPosition = position;
        DetailWishList f = (DetailWishList) fm.getFragments().get(1);
        f.getWish(b);
        if (saveToHistory) {
            pageHistory.clear();
            pageHistory.push(0);
        }
        viewPager.setCurrentItem(1, false);
    }

    public void toDetailBlessing(Bundle b, int cheeringNum, int id) {
        DetailBlessingList f = (DetailBlessingList) fm.getFragments().get(4);
        f.getWish(b, cheeringNum, id);
        if (saveToHistory) {
            pageHistory.clear();
            pageHistory.push(3);
        }
        viewPager.setCurrentItem(4, false);
    }

    public void toDetailBlessingFromWall(String wish, String time, int cheeringNum, int id, int position, boolean cheered) {
        DetailBlessingList f = (DetailBlessingList) fm.getFragments().get(4);
        tabsGroup.setVisibility(View.GONE);
        makeWish.setVisibility(View.GONE);
        f.getWishFromWall(wish, time, cheeringNum, id, position, cheered);
        if (saveToHistory) {
            pageHistory.clear();
            pageHistory.push(2);
        }
        viewPager.setCurrentItem(4, false);
    }

    public void updateEditWish(String wish_id) {
        WishList f = (WishList) fm.getFragments().get(0);
        f.updateEditWish(wish_id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        //menu.findItem(R.id.edit).setActionView(R.id.edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void initActionBar() {
        actionBar = getSupportActionBar();
        //actionBar.setTitle("我的星願");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionbarLayout = LayoutInflater.from(this).inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(actionbarLayout);
        actionBar_title = (TextView) actionbarLayout.findViewById(R.id.customActionBarTitle);
        selectNum = (TextView) actionbarLayout.findViewById(R.id.selectNum);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        //Save regId and its version
        editor.clear();
        editor.putString(REG_ID, regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.commit();
    }

    public void alterCheeringState(boolean checked, int position) {
        WishWall f = (WishWall) fm.getFragments().get(2);
        f.alterCheeringState(checked, position);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        //prefs.edit().clear().commit();
        String registrationId = prefs.getString(REG_ID, "");

        //if never registered
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        //compare the version, if older, return "" to get a new regId
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    //Get a new regId
                    regid = gcm.register(SENDER_ID);

                    msg = "Device registered, registration ID=" + regid;
                    //Send to server
                    sendRegistrationIdToBackend(regid);
                    //Store in SharedPreference
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute(null, null, null);
    }

    private SharedPreferences getGcmPreferences(Context context) {
        return getSharedPreferences("user", Context.MODE_PRIVATE);
    }

    private void sendRegistrationIdToBackend(String reg_id) {
        //裝置向GCM註冊完畢時進入
        JSONObject JObj = new JSONObject();
        try {
            JObj.put("reg_id", reg_id);
            JObj.put("device_id", device_id);
        } catch (JSONException e) {
            Log.e("JSONError", e.toString());
        }
        new SendReg_id().execute(JObj);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_FIRST_USER) {
            if (null != data) {
                viewPager.setCurrentItem(0);
                wishList.setChecked(true);
                WishList f = (WishList) fm.getFragments().get(0);
                f.update(data.getExtras());
                shouldRefresh = true;
            }
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
    public void onBackPressed() {
        if (multipleLL.getVisibility() == View.VISIBLE) {
            multipleLL.setVisibility(View.GONE);
            selectNum.setVisibility(View.GONE);
            tabsGroup.setVisibility(View.VISIBLE);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar_title.setText(getString(R.string.myWish_title));
            WishList f = (WishList) fm.getFragments().get(0);
            f.mode = f.SINGLE_MODE;
            f.menu.findItem(R.id.delete).setVisible(false);
            f.itemDeactivated();
        } else if (pageHistory.empty()) {
            super.onBackPressed();
        } else {
            if (pageHistory.peek() == 0) {
                actionBar_title.setText(getString(R.string.myWish_title));
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else if (pageHistory.peek() == 3) {
                actionBar_title.setText(getString(R.string.myBless_title));
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else if (pageHistory.peek() == 2) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                makeWish.setVisibility(View.VISIBLE);
                tabsGroup.setVisibility(View.VISIBLE);
                WishWall f = (WishWall) fm.getFragments().get(2);
                f.alterBlessingState(blessed, detailBlessingItemPosition);
                blessed = false;
                actionBar_title.setText(getString(R.string.wishWall_title));
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
            saveToHistory = false;
            viewPager.setCurrentItem(pageHistory.pop());
            saveToHistory = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
        //lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

    }

    class SendReg_id extends AsyncTask<JSONObject, Void, Void> {
        public SendReg_id() {
            super();
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            jsonParser.makeHttpRequest(ip + "/getReg_id.php", params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}


