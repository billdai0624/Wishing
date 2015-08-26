package com.intern.ab.starwish;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONParser {
    static final int INSERT = 0;
    static final int UPDATE = 1;
    static final int DELETE = 2;
    static final int QUERY = 3;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public JSONParser() {
    }

    // getJSONFromUrl方法為單存取資料用
    /*public JSONObject getJSONFromUrl(String destination) {
        HttpURLConnection conn = null;
        // Making HTTP request
        try {
            // 建立連線
            URL url = new URL(destination);
            conn = (HttpURLConnection) url.openConnection();
            //===============================
            //下面註解兩行可有可無
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            //===============================
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            json = sb.toString();
        } catch (Exception e) {
            System.out.println("getJSONFromUrl Exception Error :" + e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "getJSONFromUrl Error parsing data " + e.toString());
        }
        return jObj;
    }*/

    public String makeHttpRequest(String destination, JSONObject JObj) {
        HttpURLConnection conn = null;
        json = "";
        try {
            // 建立連線
            URL url = new URL(destination);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST"); //URL 請求的方法
            //發送表單類型資料，設定請求標頭
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);

            //Send request
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(JObj.toString());
            writer.flush();
            writer.close();

            //Get Response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            json = sb.toString();
        } catch (Exception e) {
            json = "Fail";
            Log.e("makeHttpRequest Error:", e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        // try parse tJSON object
        /*try {he string to a
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "getJSONFromUrl Error parsing data " + e.toString());
        }*/
        // return JSON String
        return json;
    }
}
