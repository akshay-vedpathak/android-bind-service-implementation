package com.cs478.akshay.fedcash;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vedpa on 12/6/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "FEDCASH.db";
    private static final int DB_VERSION = 1;

    private static final String SQL_CREATE_TABLE ="CREATE TABLE FEDCASH_HISTORY ( " +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "QUERY TEXT, " +
            "OUTPUT TEXT)";
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS FEDCASH_HISTORY";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }


}
