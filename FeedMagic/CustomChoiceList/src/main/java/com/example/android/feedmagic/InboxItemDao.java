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

import javax.security.auth.Subject;

public class InboxItemDao {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private int mCount;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_NAME,
            MySQLiteHelper.COLUMN_SUBJECT,
            MySQLiteHelper.COLUMN_BODY,
            MySQLiteHelper.COLUMN_GUID,
            MySQLiteHelper.COLUMN_TIMESTAMP,
            MySQLiteHelper.COLUMN_IMAGEURL

    };

    public InboxItemDao(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public InboxItem createInboxItem(String name, String subject, String body, long timeStamp,
                                     String guid,String imageUrl) {
        InboxItem newInboxItem = null;

        try {
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_NAME, name);
            values.put(MySQLiteHelper.COLUMN_SUBJECT, subject);
            values.put(MySQLiteHelper.COLUMN_BODY, body);
            values.put(MySQLiteHelper.COLUMN_TIMESTAMP, timeStamp);
            values.put(MySQLiteHelper.COLUMN_GUID, guid);
            values.put(MySQLiteHelper.COLUMN_IMAGEURL,imageUrl);

            long insertId = database.insert(MySQLiteHelper.TABLE_INBOXITEM, null,
                    values);
            Cursor cursor = database.query(MySQLiteHelper.TABLE_INBOXITEM,
                    allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                    null, null, null);
            cursor.moveToFirst();
            newInboxItem = cursorToInboxItem(cursor);
            cursor.close();
        } catch (Exception e) {
            Log.v("Feedmagic", e.toString());
        }
        return newInboxItem;
    }

    public void deleteInboxItem(InboxItem inboxitem) {
        long id = inboxitem.getId();
        System.out.println("InboxItem deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_INBOXITEM, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void bulkDelete() {
        database.delete(MySQLiteHelper.TABLE_INBOXITEM, null, null);
    }

    public void deleteInboxItem(long id) {
        System.out.println("InboxItem deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_INBOXITEM, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void bulkAdd() {
        database.beginTransaction();
        for (int i = mCount; i < (mCount + 3); i++) {
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_NAME, "Name           " + i);
            values.put(MySQLiteHelper.COLUMN_SUBJECT, "Subject        " + i);
            values.put(MySQLiteHelper.COLUMN_BODY, "Body        " + i);
            values.put(MySQLiteHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
            database.insert(MySQLiteHelper.TABLE_INBOXITEM, null, values);
        }
        mCount = mCount + 3;
        database.setTransactionSuccessful();
        database.endTransaction();

        List<InboxItem> list = getAllInboxItems();
        Log.v("SampleList", "List has items " + list.size());

    }

    public void dropTables() {
        System.out.println("Dropping database table inboxitem");
        database.delete(MySQLiteHelper.TABLE_INBOXITEM, null, null);
    }

    public List<InboxItem> getAllInboxItems() {
        List<InboxItem> inboxitems = new ArrayList<InboxItem>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_INBOXITEM,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            InboxItem inboxitem = cursorToInboxItem(cursor);
            inboxitems.add(inboxitem);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return inboxitems;
    }

    public Cursor getDataCursor() {

        Cursor cursor = database.query(MySQLiteHelper.TABLE_INBOXITEM,
                allColumns, null, null, null, null, MySQLiteHelper.COLUMN_TIMESTAMP + " DESC", null);
        return cursor;

    }

    public Cursor getItem(long id) {

        Cursor cursor = database.query(MySQLiteHelper.TABLE_INBOXITEM,
                allColumns, MySQLiteHelper.COLUMN_ID
                + " = " + id, null, null, null, MySQLiteHelper.COLUMN_ID + " DESC", null);
        return cursor;

    }

    public InboxItem cursorToInboxItem(Cursor cursor) {
        InboxItem inboxitem = new InboxItem();
        inboxitem.setId(cursor.getInt(0));
        inboxitem.setName(cursor.getString(1));
        inboxitem.setSubject(cursor.getString(2));
        inboxitem.setBody(cursor.getString(3));
        inboxitem.setGuid(cursor.getString(4));
        inboxitem.setTimeStamp(cursor.getLong(5));
        inboxitem.setImageUrl(cursor.getString(6));
        return inboxitem;
    }
}
