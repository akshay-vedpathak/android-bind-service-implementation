package com.cs478.akshay.treasuryserv;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView serviceStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceStatus = findViewById(R.id.serviceStatus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch(TreasuryService.serviceStatus){
            case 0: serviceStatus.setText("No Client bound to the service");
                break;
            case 1: serviceStatus.setText("Service bound by client but not running processes");
                break;
            case 2: serviceStatus.setText("Service bound by client and running processes");
        }
    }
}
