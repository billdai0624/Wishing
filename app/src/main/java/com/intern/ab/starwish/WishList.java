package com.intern.ab.starwish;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class WishList extends Fragment {
    public static final int SINGLE_MODE = 0;
    public static final int MULTI_MODE = 1;
    //static final String ip = "http://10.0.3.2";
    static final String ip = "http://192.168.0.111";
    static String device_id;
    public int mode = 0;
    public Menu menu;
    //private MenuItem deleteMenuItem;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private ListView wishList;
    private WishAdapter wishItemAdapter;
    private List<Wish_item> items;
    private Bundle wishData;
    private View rootView;
    private TextView selectedNumTv;
    private int selectedNum;
    private String newWish;
    private String time;
    private ImageView makeWish_hint;
    private boolean selectAllFlag;
    private LinearLayout multipleLL;
    private RadioGroup tabsGroup;
    private Button selectAll;
    private Button delete;
    private Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.delete).setVisible(false);
        ((MainActivity) getActivity()).actionBar.setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).actionBar_title.setText(getString(R.string.myWish_title));
        selectedNumTv = ((MainActivity) getActivity()).selectNum;
        selectedNumTv.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            /*mode = MULTI_MODE;
            ((MainActivity) getActivity()).actionBar.setDisplayHomeAsUpEnabled(true);
            ((MainActivity) getActivity()).actionBar_title.setText(getString(R.string.select_title));
            item.setVisible(false);
            selectAll.setText(getString(R.string.selectAll_label));
            selectAllFlag = true;
            selectedNumTv.setVisibility(View.VISIBLE);
            selectedNumTv.setText("(0)");
            changeMode();*/
            if (!isConnected()) {
                Toast.makeText(getActivity(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            } else if (selectedNum == 0) {
                Toast.makeText(getActivity(), getString(R.string.multiDelete_hint), Toast.LENGTH_SHORT).show();
            } else {
                dialog = new Dialog(getActivity(), R.style.MyDialog);
                dialog.setContentView(R.layout.custom_dialog);
                Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
                Button confirm = (Button) dialog.findViewById(R.id.confirm_btn);
                TextView question = (TextView) dialog.findViewById(R.id.dialogQuestion);
                question.setText(getString(R.string.multiDelete_confirm));
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
            }
        }
        if (item.getItemId() == android.R.id.home) {
            menu.findItem(R.id.delete).setVisible(false);
            wishList.clearChoices();
            itemDeactivated();
            mode = SINGLE_MODE;
            selectedNumTv.setVisibility(View.GONE);
            multipleLL.setVisibility(View.GONE);
            tabsGroup.setVisibility(View.VISIBLE);
            ((MainActivity) getActivity()).actionBar.setDisplayHomeAsUpEnabled(false);
            ((MainActivity) getActivity()).actionBar_title.setText(getString(R.string.myWish_title));

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        device_id = ((MainActivity) getActivity()).device_id;
        mode = SINGLE_MODE;
        selectAllFlag = true;
        rootView = inflater.inflate(R.layout.wish_list, container, false);
        initDB();
        wishList = (ListView) rootView.findViewById(R.id.wishList);
        makeWish_hint = (ImageView) rootView.findViewById(R.id.makeWish_hint);
        if (Locale.getDefault().getLanguage().equals("zh")) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) makeWish_hint.getLayoutParams();
            lp.rightMargin = (int) (getActivity().getResources().getDimension(R.dimen.hint_rightMargin));
        }
        multipleLL = ((MainActivity) getActivity()).multipleLL;
        tabsGroup = ((MainActivity) getActivity()).tabsGroup;
        selectAll = ((MainActivity) getActivity()).selectAll;
        delete = ((MainActivity) getActivity()).delete;
        items = new ArrayList<Wish_item>();

        cursor = db.rawQuery("SELECT _Id, Wish, DateTime, Realized, Public, rowid " +
                "FROM wish ORDER BY rowid DESC", null);
        while (cursor.moveToNext()) {
            boolean i = (cursor.getInt(3) == 1);
            boolean k = (cursor.getInt(4) == 1);
            items.add(new Wish_item(cursor.getInt(0), cursor.getString(1), cursor.getString(2), i, k, cursor.getInt(5)));
        }
        /*wishData = ((MainActivity) getActivity()).getIntent().getExtras();
        if (null != wishData) {
            newWish = wishData.getString("newWish");
            time = wishData.getString("time");
            items.add(new Wish_item(false, newWish, time));
        }*/
        cursor.close();
        wishItemAdapter = new WishAdapter(getActivity(), R.layout.custom_wish_item, items);
        wishList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        wishList.setAdapter(wishItemAdapter);
        wishList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mode != MULTI_MODE) {
                    mode = MULTI_MODE;
                    ((MainActivity) getActivity()).actionBar.setDisplayHomeAsUpEnabled(true);
                    ((MainActivity) getActivity()).actionBar_title.setText(getString(R.string.select_title));
                    menu.findItem(R.id.delete).setVisible(true);
                    selectAll.setText(getString(R.string.selectAll_label));
                    selectAllFlag = true;
                    selectedNumTv.setVisibility(View.VISIBLE);
                    selectedNumTv.setText("(0)");
                    changeMode();
                }
                return false;
            }
        });
        wishList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mode == SINGLE_MODE) {
                    wishItemAdapter.toDetail(position);
                    wishList.setItemChecked(position, false);
                    wishItemAdapter.notifyDataSetChanged();
                } else {
                    checkItemSelected();
                    wishItemAdapter.notifyDataSetChanged();
                }
            }
        });
        if (items.isEmpty()) {
            makeWish_hint.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    public void initDB() {
        dbHelper = ((MainActivity) getActivity()).dbHelper;
        db = dbHelper.getReadableDatabase();
    }

    public void update(Bundle bun) {
        //wishData = ((MainActivity) getActivity()).getBundle();
        /*if (null != bun) {
            newWish = bun.getString("newWish");
            time = bun.getString("time");
            items.add(new Wish_item(false, newWish, time));
            wishItemAdapter.notifyDataSetChanged();
        }*/
        updateListView();
        wishItemAdapter.notifyDataSetChanged();

    }

    public void updateEditWish(String wish_id) {
        /*String editWish = bundle.getString("editWish");
        String editTime = bundle.getString("editTime");
        */
        /*cursor = db.rawQuery("SELECT Wish, DateTime, Realized FROM wish WHERE _Id=?", new String[]{wish_id});
        cursor.moveToNext();
        String editWish = cursor.getString(0);
        String editTime = cursor.getString(1);
        boolean realized = (cursor.getInt(2)==1);
        int position = ((MainActivity) getActivity()).detailItemPosition;
        items.set(position, new Wish_item(realized, editWish, editTime));
        wishItemAdapter.notifyDataSetChanged();*/
        updateListView();
        wishItemAdapter.notifyDataSetChanged();
    }

    public void deleteFromDetail(String detailWish_id) {
        //items.remove(((MainActivity) getActivity()).detailItemPosition);
        db.delete("wish", "_Id=?", new String[]{detailWish_id});
        updateListView();
        /*cursor = db.rawQuery("SELECT _Id, Wish, DateTime, Realized " +
                "FROM wish ORDER BY DateTime DESC", null);
        items.clear();
        while (cursor.moveToNext()) {
            boolean i = (cursor.getInt(3) == 1);
            boolean k = (cursor.getInt(4) == 1);
            items.add(new Wish_item(cursor.getInt(0), cursor.getString(1), cursor.getString(2), i, k));
        }
        cursor.close();*/
        wishItemAdapter.notifyDataSetChanged();
    }

    public void updateListView() {
        cursor = db.rawQuery("SELECT _Id, Wish, DateTime, Realized, Public, rowid " +
                "FROM wish ORDER BY DateTime DESC, rowid DESC", null);
        items.clear();
        while (cursor.moveToNext()) {
            boolean i = (cursor.getInt(3) == 1);
            boolean k = (cursor.getInt(4) == 1);
            items.add(new Wish_item(cursor.getInt(0), cursor.getString(1), cursor.getString(2), i, k, cursor.getInt(5)));
        }
        cursor.close();
        if (!items.isEmpty()) {
            makeWish_hint.setVisibility(View.GONE);
        }

    }

    public void changeMode() {
        wishList.clearChoices();
        multipleLL.setVisibility(View.VISIBLE);
        tabsGroup.setVisibility(View.GONE);
        Button selectAll = (Button) ((MainActivity) getActivity()).findViewById(R.id.selectAll);
        Button multiDelete = (Button) ((MainActivity) getActivity()).findViewById(R.id.cancelDelete);
        selectAll.setOnClickListener(new onClickListener());
        multiDelete.setOnClickListener(new onClickListener());
    }

    public void checkItemSelected() {
        //boolean deleteEnabled = false;
        selectedNum = 0;
        for (int i = 0; i < wishList.getCount(); i++) {
            if (wishList.isItemChecked(i)) {
                /*delete.setTextColor(Color.parseColor("#FFFFFF"));
                delete.setEnabled(true);
                deleteEnabled = true;*/
                selectedNum++;
            }
        }
        selectedNumTv.setText("(" + selectedNum + ")");
        /*if (!deleteEnabled) {
            delete.setTextColor(Color.parseColor("#8EC8D9"));
            delete.setEnabled(false);
        }*/
        if (selectedNum == wishList.getCount()) {
            selectAll.setText(getString(R.string.cancelAll_label));
            selectAllFlag = false;
        } else {
            selectAll.setText(getString(R.string.selectAll_label));
            selectAllFlag = true;
        }
    }

    public void itemDeactivated() {
        for (int i = 0; i < wishList.getCount(); i++) {
            wishList.setItemChecked(i, false);
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

    class onClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.selectAll) {
                if (selectAllFlag) {
                    for (int i = 0; i < wishItemAdapter.getCount(); i++) {
                        wishList.setItemChecked(i, true);
                    }
                    checkItemSelected();
                    wishItemAdapter.notifyDataSetChanged();
                    selectAll.setText(getString(R.string.cancelAll_label));
                } else {
                    for (int i = 0; i < wishItemAdapter.getCount(); i++) {
                        wishList.setItemChecked(i, false);
                    }
                    checkItemSelected();
                    wishItemAdapter.notifyDataSetChanged();
                    selectAll.setText(getString(R.string.selectAll_label));
                }
            } else {
                /*dialog = new Dialog(getActivity(), R.style.MyDialog);
                dialog.setContentView(R.layout.custom_dialog);
                Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
                Button confirm = (Button) dialog.findViewById(R.id.confirm_btn);
                TextView question = (TextView) dialog.findViewById(R.id.dialogQuestion);
                question.setText(getString(R.string.multiDelete_confirm));
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
                dialog.show();*/
                ((MainActivity) getActivity()).onBackPressed();
            }
        }
    }

    class dialogOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cancel_btn) {
                dialog.dismiss();
            } else if (v.getId() == R.id.confirm_btn) {
                StringBuilder sb = new StringBuilder();
                for (int i = wishItemAdapter.getCount() - 1; i >= 0; i--) {
                    if (wishList.isItemChecked(i)) {
                        //wishItemAdapter.remove(wishItemAdapter.getItem(i));
                        int id = wishItemAdapter.getItem(i).getId();
                        if (sb.length() == 0) {
                            sb.append("" + id);
                        } else {
                            sb.append(", " + id);
                        }
                    }
                }
                cursor = db.query("wish", new String[]{"_Id"}, String.format("_Id IN (%s) AND Public=?", sb.toString()), new String[]{"1"}, null, null, null);
                JSONObject JObj = new JSONObject();
                try {
                    for (int k = 1; cursor.moveToNext(); k++) {
                        if (k == 1) {
                            JObj.put("SQLite_id", "'" + cursor.getInt(0) + "'");
                        } else {
                            JObj.put("SQLite_id", ", '" + cursor.getInt(0) + "'");
                        }
                    }
                    JObj.put("device_id", device_id);
                    new DeleteFromSQL().execute(JObj);
                } catch (JSONException e) {
                    Log.e("JSonError", e.toString());
                }
                db.delete("wish", String.format("_Id IN (%s)", sb.toString()), null);
                updateListView();
                wishItemAdapter.notifyDataSetChanged();
                wishList.clearChoices();
                checkItemSelected();
                if (items.isEmpty()) {
                    makeWish_hint.setVisibility(View.VISIBLE);
                }
                dialog.dismiss();
                selectAll.setText(getString(R.string.selectAll_label));
                selectAllFlag = true;
                ((MainActivity) getActivity()).shouldRefresh = true;
                ((MainActivity) getActivity()).onBackPressed();
            }
        }
    }

    class DeleteFromSQL extends AsyncTask<JSONObject, Void, Void> {
        public DeleteFromSQL() {
            super();
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONParser jsonParser = new JSONParser();
            jsonParser.makeHttpRequest(ip + "/multiDelete.php", params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}
