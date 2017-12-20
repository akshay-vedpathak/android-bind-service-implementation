package com.cs478.akshay.fedcash;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cs478.akshay.common.TreasuryAPI;

import java.util.List;

public class FedCash extends Activity{

    private Spinner spinner;
    private EditText year,month,day,working_days;
    private Button submit, unbind, view_history;
    private TreasuryAPI mTreasuryService;
    private boolean mIsBound = false;
    private TextView result;
    private List<Integer> monthlyCash;
    private List<Integer> dailyCash;
    private static final int monthly = 1 , daily = 2, avg = 3, failedValidation = 4;
    private DBHelper dbHelper = new DBHelper(FedCash.this);

    private SQLiteDatabase db;

    public Handler mainHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case monthly:
                    if(!result.toString().isEmpty()){result.clearComposingText();}
                    result.setText((String)msg.obj); result.setVisibility(View.VISIBLE);
                break;
                case daily:
                    if(!result.toString().isEmpty()){result.clearComposingText();}
                    result.setText((String) msg.obj); result.setVisibility(View.VISIBLE);
                break;
                case avg:
                    if(!result.toString().isEmpty()){result.clearComposingText();}
                    result.setText(String.valueOf(msg.arg1)); result.setVisibility(View.VISIBLE);
                break;
                case failedValidation:
                    Toast.makeText(FedCash.this,"Make sure the year is entered and is in range 2006-2016",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fed_cash);
        db = dbHelper.getWritableDatabase();
        spinner = findViewById(R.id.choices);

        spinner.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item,getResources().getStringArray(R.array.choices)));

        year = findViewById(R.id.year);
        month = findViewById(R.id.month);
        day = findViewById(R.id.day);
        working_days = findViewById(R.id.working_days);
        submit = findViewById(R.id.submit_query);

        year.setVisibility(View.INVISIBLE);
        month.setVisibility(View.INVISIBLE);
        day.setVisibility(View.INVISIBLE);
        working_days.setVisibility(View.INVISIBLE);
        submit.setVisibility(View.INVISIBLE);

        result = findViewById(R.id.result);
        result.setVisibility(View.INVISIBLE);

        unbind = findViewById(R.id.unbind_service);

        unbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindFromTreasuryService();
            }
        });

        view_history = findViewById(R.id.view_history);

        view_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FedCash.this,History.class);
                startActivity(i);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                switch (position){
                    case 0:year.setVisibility(View.INVISIBLE);
                        month.setVisibility(View.INVISIBLE);
                        day.setVisibility(View.INVISIBLE);
                        working_days.setVisibility(View.INVISIBLE);
                        submit.setVisibility(View.INVISIBLE);
                        result.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        month.setVisibility(View.INVISIBLE); day.setVisibility(View.INVISIBLE); working_days.setVisibility(View.INVISIBLE);
                        year.setVisibility(View.VISIBLE); submit.setVisibility(View.VISIBLE);result.setVisibility(View.INVISIBLE);
                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(!mIsBound){
                                    bindToTreasuryService();
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String y = year.getText().toString();
                                        if(validateInput(position,new String[]{y})){
                                            int iy = Integer.parseInt(y);
                                            if(mIsBound){
                                                try {
                                                    monthlyCash = mTreasuryService.getMonthlyCash(iy);
                                                    String result = getFormattedString(monthlyCash);
                                                    Log.i("fedcash","response returned from service"+result);
                                                    ContentValues cv = new ContentValues();
                                                    cv.put("QUERY","GetMonthlyCash("+y+")");
                                                    cv.put("OUTPUT",result);
                                                    long row = db.insert("FEDCASH_HISTORY",null,cv);
                                                    Message msg = mainHandler.obtainMessage(monthly);
                                                    msg.obj  = result;
                                                    mainHandler.sendMessage(msg);

                                                } catch (RemoteException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }else{
                                            Message msg = mainHandler.obtainMessage(failedValidation);
                                            mainHandler.sendMessage(msg);
                                        }
                                    }
                                }).start();
                            }
                        });
                        break;

                    case 2: year.setVisibility(View.VISIBLE); submit.setVisibility(View.VISIBLE);
                        month.setVisibility(View.VISIBLE); day.setVisibility(View.VISIBLE);result.setVisibility(View.INVISIBLE);
                        working_days.setVisibility(View.VISIBLE);
                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(!mIsBound){
                                    bindToTreasuryService();
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String y = year.getText().toString();
                                        String m = month.getText().toString();
                                        String d = day.getText().toString();
                                        String wd = working_days.getText().toString();
                                        if(validateInput(position,new String[]{y,m,d,wd})){
                                            int iy1 = Integer.parseInt(y);
                                            int im = Integer.parseInt(m);
                                            int id = Integer.parseInt(d);
                                            int iwd = Integer.parseInt(wd);
                                            try {
                                                dailyCash = mTreasuryService.getDailyCash(iy1, im, id, iwd);
                                                String output = getFormattedString(dailyCash);
                                                ContentValues cv = new ContentValues();
                                                cv.put("QUERY","GetDailyCash("+y+","+m+","+d+","+wd+")");
                                                cv.put("OUTPUT",output);
                                                long row = db.insert("FEDCASH_HISTORY",null,cv);
                                                Message msg = mainHandler.obtainMessage(daily);
                                                msg.obj = output;
                                                mainHandler.sendMessage(msg);

                                            } catch (RemoteException e) {
                                                e.printStackTrace();
                                            }

                                        }else{
                                            Message msg = mainHandler.obtainMessage(failedValidation);
                                            mainHandler.sendMessage(msg);
                                        }
                                    }
                                }).start();
                            }
                        });
                        break;

                    case 3:
                        month.setVisibility(View.INVISIBLE); day.setVisibility(View.INVISIBLE); working_days.setVisibility(View.INVISIBLE);
                        year.setVisibility(View.VISIBLE); submit.setVisibility(View.VISIBLE);result.setVisibility(View.INVISIBLE);
                        submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!mIsBound) {
                                bindToTreasuryService();
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String y = year.getText().toString();
                                    if(validateInput(position,new String[]{y})){
                                        int iy = Integer.parseInt(y);
                                        if(mIsBound){
                                            try {
                                                int output = mTreasuryService.getYearlyAvg(iy);
                                                ContentValues cv = new ContentValues();
                                                cv.put("QUERY","GetYearlyAverageCash("+y+")");
                                                cv.put("OUTPUT",String.valueOf(output));
                                                long row = db.insert("FEDCASH_HISTORY",null,cv);
                                                Message msg = mainHandler.obtainMessage(avg);
                                                msg.arg1 = output;
                                                mainHandler.sendMessage(msg);
                                            } catch (RemoteException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }else{
                                        Message msg = mainHandler.obtainMessage(failedValidation);
                                        mainHandler.sendMessage(msg);
                                    }
                                }
                            }).start();

                        }
                    });
                    break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                year.setVisibility(View.INVISIBLE);
                month.setVisibility(View.INVISIBLE);
                day.setVisibility(View.INVISIBLE);
                working_days.setVisibility(View.INVISIBLE);
                submit.setVisibility(View.INVISIBLE);
                result.setVisibility(View.INVISIBLE);
            }
        });
    }

    private boolean validateInput(int position, String[] inputs){
        switch(position){
            case 1:
            case 3: if(inputs[0].isEmpty() || Integer.valueOf(inputs[0]) < 2006 || Integer.valueOf(inputs[0]) > 2016){
                return false;
            }
            break;
            case 2: if(inputs[0].isEmpty() || inputs[1].isEmpty() || inputs[2].isEmpty() || inputs[3].isEmpty() ||
                    Integer.valueOf(inputs[0]) < 2006 && Integer.valueOf(inputs[0]) > 2016){
                return false;
            }
        }
        return true;
    }

    private String getFormattedString(List<Integer> list) {
        StringBuilder builder = new StringBuilder();
        for(Integer i : list){
            builder.append(i+"\n");
        }
        return builder.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mIsBound){
            bindToTreasuryService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        result.setVisibility(View.INVISIBLE);
    }

    private void bindToTreasuryService(){
        boolean b = false;
        Intent i = new Intent(TreasuryAPI.class.getName());
        ResolveInfo info = getPackageManager().resolveService(i, Context.BIND_AUTO_CREATE);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
        b = bindService(i,this.mServiceConnection, Context.BIND_AUTO_CREATE);
        if(b){
            Log.i("FedCash","Service successfully bound");
            mIsBound=true;
        }else{
            Log.i("FedCash","Service not bound");
        }
    }

    private void unbindFromTreasuryService(){
        if(mIsBound){
            unbindService(FedCash.this.mServiceConnection);
            Toast.makeText(this, "Successfully unbinded from Treasury Service", Toast.LENGTH_SHORT).show();
            mIsBound = false;
        }else{
            Toast.makeText(this,"Service already unbounded",Toast.LENGTH_SHORT).show();
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTreasuryService = TreasuryAPI.Stub.asInterface(service);
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTreasuryService = null;
            mIsBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
