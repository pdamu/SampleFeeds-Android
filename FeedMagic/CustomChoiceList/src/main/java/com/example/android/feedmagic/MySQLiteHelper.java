package com.example.android.feedmagic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by pdamu on 1/14/14.
 */


public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_FEEDITEM = "feeditem";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_GUID = "guid";
    public static final String COLUMN_IMAGEURL = "imageurl";
    public static final String COLUMN_FEEDTYPE = "feedtype";


    private static final String DATABASE_NAME = "feed.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_FEEDITEM + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NAME
            + " text not null, "
            + COLUMN_SUBJECT + " text not null, "
            + COLUMN_BODY + " text not null, "
            + COLUMN_TIMESTAMP + " integer, "
            + COLUMN_GUID + " text not null unique, "
            + COLUMN_IMAGEURL + " text, "
            + COLUMN_FEEDTYPE + " integer "

            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDITEM);
        onCreate(db);
    }

}
