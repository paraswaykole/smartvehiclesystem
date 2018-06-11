package com.project.smartvehicle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class APIManager {

    private APIManager(){
    }

    private static APIManager INSTANCE = new APIManager();

    public static APIManager getInstance(){
        return INSTANCE;
    }

    String host_domain = "http://192.168.43.32:8000";
    String host_url = host_domain+"/driverapp/";
    public static final String SHAREDPREFS_NAME = "SmartVehicle";

    public String get_httpget_result(String url)
    {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(50000);
            c.setReadTimeout(50000);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return "";
    }


    public String get_httppost_result(String url,HashMap<String, String> postDataParams)
    {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("POST");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(50000);
            c.setReadTimeout(50000);
            c.setDoInput(true);
            c.setDoOutput(true);

            OutputStream os = c.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return "";
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public boolean register(Activity activity,String name,String phone,String vehicle_model,String vehicle_number,String password,String city)
    {
        HashMap<String, String> params = new HashMap<String,String>();
        params.put("name",name);
        params.put("phone",phone);
        params.put("password",password);
        params.put("vehicle_model",vehicle_model);
        params.put("vehicle_number", vehicle_number);
        params.put("city", city);

        String result = get_httppost_result(host_url+"register/",params);

        try {
            JSONObject jsonObject = new JSONObject(result);
            boolean success = jsonObject.getBoolean("result");
            if(success)
            {
                int userid = jsonObject.getInt("userid");
                SharedPreferences sp = activity.getSharedPreferences(SHAREDPREFS_NAME, activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("userid", userid);
                editor.apply();

                return true;
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        return false;
    }


    public boolean login(Activity activity,String username,String pass)
    {

        HashMap<String,String> params = new HashMap<>();
        params.put("username",username);
        params.put("password",pass);

        String result = get_httppost_result(host_url+"login/",params);
        try {
            JSONObject jsonObject = new JSONObject(result);
            boolean success = jsonObject.getBoolean("result");
            if(success)
            {
                int userid = jsonObject.getInt("userid");
                SharedPreferences sp = activity.getSharedPreferences(SHAREDPREFS_NAME, activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("userid", userid);
                editor.apply();

                return true;
            }
            else
            {
                return false;
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public int rideid;
    public boolean startride(Activity activity)
    {
        SharedPreferences sp = activity.getSharedPreferences(SHAREDPREFS_NAME, activity.MODE_PRIVATE);

        int userid =  sp.getInt("userid",-1);
        if(userid==-1)
            return false;

        HashMap<String,String> params = new HashMap<>();
        params.put("driverid",""+userid);

        String result = get_httppost_result(host_url+"startride/",params);
        try {
            JSONObject jsonObject = new JSONObject(result);
            boolean success = jsonObject.getBoolean("result");
            if(success)
            {
                rideid = jsonObject.getInt("rideid");
                return true;
            }
            else
            {
                return false;
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public int speed;
    public int speedlimit;
    public String status;
    public boolean getridedata()
    {

        HashMap<String,String> params = new HashMap<>();
        params.put("rideid",""+rideid);

        String result = get_httppost_result(host_url+"currentdata/",params);
        try {
            JSONObject jsonObject = new JSONObject(result);
            boolean success = jsonObject.getBoolean("result");
            if(success)
            {
                speed = jsonObject.getInt("speed");
                speedlimit = jsonObject.getInt("speedlimit");
                status = jsonObject.getString("status");
                return true;
            }
            else
            {
                return false;
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }





    /*
     * DEVICE CONNECTION
     *
     */

    String device_ip = "http://192.168.43.192";

    public boolean check_connection_to_device(Activity activity)
    {
        SharedPreferences sp = activity.getSharedPreferences(SHAREDPREFS_NAME, activity.MODE_PRIVATE);
        int uid = sp.getInt("userid",-1);
        if(uid==-1)
            return false;

        String result = get_httpget_result(device_ip+"/connection?u="+uid);
        if(result.equals("it works\n")){
            return true;
        }
        return false;
    }

    public boolean startride_to_device(){

        String result = get_httpget_result(device_ip+"/startdriving?r="+rideid);
        if (result.equals("true\n")){
            return true;
        }
        return false;
    }

    public boolean newlocation_to_device(String location){

        String result = get_httpget_result(device_ip+"/newlocation?loc="+location);
        if (result.equals("true\n")){
            return true;
        }
        return false;
    }



}