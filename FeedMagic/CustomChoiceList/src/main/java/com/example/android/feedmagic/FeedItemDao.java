/**
 * Created by pdamu on 1/14/14.
 */
package com.example.android.feedmagic;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FeedItemDao {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private int mCount;
    private Feeds mFeeds = new Feeds();

    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_NAME,
            MySQLiteHelper.COLUMN_SUBJECT,
            MySQLiteHelper.COLUMN_BODY,
            MySQLiteHelper.COLUMN_GUID,
            MySQLiteHelper.COLUMN_TIMESTAMP,
            MySQLiteHelper.COLUMN_IMAGEURL,
            MySQLiteHelper.COLUMN_FEEDTYPE

    };

    public FeedItemDao(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long createFeedItem(String name, String subject,
                               String body, long timeStamp,
                               String guid, String imageUrl, int type) {
        long insertId = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_NAME, name);
            values.put(MySQLiteHelper.COLUMN_SUBJECT, subject);
            values.put(MySQLiteHelper.COLUMN_BODY, body);
            values.put(MySQLiteHelper.COLUMN_TIMESTAMP, timeStamp);
            values.put(MySQLiteHelper.COLUMN_GUID, guid);
            values.put(MySQLiteHelper.COLUMN_IMAGEURL,imageUrl);
            values.put(MySQLiteHelper.COLUMN_FEEDTYPE,type);

            insertId = database.insertWithOnConflict(MySQLiteHelper.TABLE_FEEDITEM, null,
                    values, SQLiteDatabase.CONFLICT_IGNORE);

        } catch (Exception e) {
            Log.v("Feedmagic", e.toString());
        }
        return insertId;
    }

    public void deleteInboxItem(FeedItem inboxitem) {
        long id = inboxitem.getId();
        System.out.println("FeedItem deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_FEEDITEM, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void bulkDelete() {
        database.delete(MySQLiteHelper.TABLE_FEEDITEM, null, null);
    }

    public void deleteInboxItem(long id) {
        System.out.println("FeedItem deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_FEEDITEM, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

      public void dropTables() {
        System.out.println("Dropping database table inboxitem");
        database.delete(MySQLiteHelper.TABLE_FEEDITEM, null, null);
    }

    public List<FeedItem> getAllInboxItems() {
        List<FeedItem> feedItems = new ArrayList<FeedItem>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_FEEDITEM,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FeedItem feedItem = cursorToFeedItem(cursor);
            feedItems.add(feedItem);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return feedItems;
    }

    public Cursor getDataCursor(String filter) {
        Cursor cursor;
        if(!filter.isEmpty() && !filter.contentEquals("inboxitems")){
            int type = Feeds.FeedType.valueOf(filter).ordinal();
            cursor = database.query(MySQLiteHelper.TABLE_FEEDITEM,
                    allColumns, MySQLiteHelper.COLUMN_FEEDTYPE
                    + " = " + type, null, null, null, MySQLiteHelper.COLUMN_TIMESTAMP + " DESC", null);
        } else {
            cursor = database.query(MySQLiteHelper.TABLE_FEEDITEM,
                allColumns, null, null, null, null, MySQLiteHelper.COLUMN_TIMESTAMP + " DESC", null);
        }
        return cursor;

    }

    public Cursor getItem(long id) {

        Cursor cursor = database.query(MySQLiteHelper.TABLE_FEEDITEM,
                allColumns, MySQLiteHelper.COLUMN_ID
                + " = " + id, null, null, null, MySQLiteHelper.COLUMN_ID + " DESC", null);
        return cursor;

    }

    public FeedItem cursorToFeedItem(Cursor cursor) {
        FeedItem feedItem = new FeedItem();
        feedItem.setId(cursor.getInt(0));
        feedItem.setName(cursor.getString(1));
        feedItem.setSubject(cursor.getString(2));
        feedItem.setBody(cursor.getString(3));
        feedItem.setGuid(cursor.getString(4));
        feedItem.setTimeStamp(cursor.getLong(5));
        feedItem.setImageUrl(cursor.getString(6));
        feedItem.setType(cursor.getInt(7));

        return feedItem;
    }
}
