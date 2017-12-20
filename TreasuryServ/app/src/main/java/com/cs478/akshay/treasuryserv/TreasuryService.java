package com.cs478.akshay.treasuryserv;

import android.app.Service;
import android.content.Intent;
import android.net.http.HttpResponseCache;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.cs478.akshay.common.TreasuryAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vedpa on 12/5/2017.
 */

public class TreasuryService extends Service {

    private static final String prefix = "http://api.treasury.io/cc7znvq/47d80ae900e04f2/sql/?q=";

    public static int serviceStatus = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private final TreasuryAPI.Stub mBinder = new TreasuryAPI.Stub() {
        @Override
        public List getMonthlyCash(int year) throws RemoteException {
            serviceStatus = 2;
            List<Integer> monthlyCash = new ArrayList<>();
            String query = "select distinct open_mo from t1 where month >= 1 and month <=12 and is_total = 1 and year="+year;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(prefix+query);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int status = connection.getResponseCode();
                String response = null;
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        response = sb.toString();
                }
                if(response!=null){
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int value = jsonObject.getInt("open_mo");
                        monthlyCash.add(value);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finally {
                if(connection!=null){
                    connection.disconnect();
                }
            }
            serviceStatus = 1;
            return monthlyCash;
        }

        @Override
        public List getDailyCash(int year, int month, int day, int workingDays) throws RemoteException {
            serviceStatus = 2;
            List<Integer> dailyCash = new ArrayList<>();
            String date = year+"-"+month+"-"+day;
            String query = "select open_today from t1 where is_total = 1 and date >= '"+date+"' order by date limit "+workingDays+";";
            Log.i("dailycashquery",query);
            HttpURLConnection connection = null;
            try {
                URL url = new URL(prefix+query);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int status = connection.getResponseCode();
                String response = null;
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        response = sb.toString();
                }
                if(response!=null){
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int value = jsonObject.getInt("open_today");
                        dailyCash.add(value);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finally {
                if(connection!=null){
                    connection.disconnect();
                }
            }
            serviceStatus = 1;
            return dailyCash;
        }

        @Override
        public int getYearlyAvg(int year) throws RemoteException {
            serviceStatus = 2;
            Double output = 0.0;
            String query = "select avg(open_today) from t1 where is_total = 1 and year ="+year;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(prefix+query);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int status = connection.getResponseCode();
                String response = null;
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        response = sb.toString();
                }
                if(response!=null){
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    output = jsonObject.getDouble("avg(open_today)");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            serviceStatus = 1;
            return output.intValue();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        serviceStatus = 1;
        return this.mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        serviceStatus = 0;
        return super.onUnbind(intent);
    }
}
