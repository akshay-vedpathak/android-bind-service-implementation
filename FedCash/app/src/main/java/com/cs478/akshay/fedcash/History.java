package com.cs478.akshay.fedcash;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vedpa on 12/6/2017.
 */

public class History extends Activity implements RecordsFragment.ListSelectionListener{

    private DBHelper dbHelper = new DBHelper(History.this);
    public static Map<String, String> results = new HashMap<>();

    private SQLiteDatabase db;

    private final OutputFragment mOutputFragment = new OutputFragment();
    private FragmentManager mFragmentManager;
    private FrameLayout mRecordsFrameLayout, mOutputFrameLayout;

    private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = dbHelper.getReadableDatabase();
        setContentView(R.layout.fedcash_history);

        String [] projection = {"ID", "QUERY", "OUTPUT"};

        String order = "ID ASC";

        Cursor cursor = db.query("FEDCASH_HISTORY",projection,null,null,null,null,order);

        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
            String query = cursor.getString(cursor.getColumnIndexOrThrow("QUERY"));
            String output = cursor.getString(cursor.getColumnIndexOrThrow("OUTPUT"));
            results.put(id+". "+query,output);
        }
        mRecordsFrameLayout = findViewById(R.id.records_container);
        mOutputFrameLayout = findViewById(R.id.output_container);

        mFragmentManager = getFragmentManager();

        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();

        fragmentTransaction.replace(R.id.records_container,
                new RecordsFragment());

        fragmentTransaction.commit();

        /*mFragmentManager
                .addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        setLayout();
                    }
                });*/

        setLayout();

    }

    private void setLayout() {
        /*if (!mOutputFragment.isAdded()) {

            // Make the TitleFragment occupy the entire layout
            mRecordsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    MATCH_PARENT, MATCH_PARENT));
            mOutputFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT));
        } else {*/

            // Make the TitleLayout take 1/3 of the layout's width
            mRecordsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT, 1f));

            // Make the QuoteLayout take 2/3's of the layout's width
            mOutputFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT, 1f));
       // }
    }


    @Override
    public void onListSelection(int index) {

        // If the QuoteFragment has not been added, add it now
        if (!mOutputFragment.isAdded()) {

            // Start a new FragmentTransaction
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();

            // Add the QuoteFragment to the layout
            fragmentTransaction.add(R.id.output_container,
                    mOutputFragment);

            // Add this FragmentTransaction to the backstack
            fragmentTransaction.addToBackStack(null);

            // Commit the FragmentTransaction
            fragmentTransaction.commit();

            // Force Android to execute the committed FragmentTransaction
            mFragmentManager.executePendingTransactions();
        }

        if (mOutputFragment.getShownIndex() != index) {

            // Tell the QuoteFragment to show the quote string at position index
            mOutputFragment.showQuoteAtIndex(index);

        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
