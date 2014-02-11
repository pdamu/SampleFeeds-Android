
/**
 * Created by pdamu on 1/15/14.
 */
package com.example.android.feedmagic;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class InboxItemProvider extends ContentProvider {

    // database
    private InboxItemDao mDao = new InboxItemDao(getContext());


    // used for the UriMacher
    private static final int INBOX_ITEMS = 10;
    private static final int INBOX_ITEM_ID = 20;

    private static final String AUTHORITY = "com.example.android.feedmagic";

    private static final String BASE_PATH = "inboxitems";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/inboxitems";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/inboxitem";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, INBOX_ITEMS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", INBOX_ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mDao = new InboxItemDao(getContext());
        mDao.open();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int uriType   = sURIMatcher.match(uri);
        Cursor cursor = null;
        switch (uriType) {
            case INBOX_ITEMS:
                cursor = mDao.getDataCursor();
                break;

            case INBOX_ITEM_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    cursor = mDao.getItem(Long.parseLong(uri.getLastPathSegment()));
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //For the sample , just do a bulk add..
        mDao.createInboxItem(values.getAsString("title"),values.getAsString("link"),
                values.getAsString("description"),values.getAsLong("timestamp"),
                values.getAsString("guid"),values.getAsString("imageurl"));
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + 0);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v("SampleList", "XXX Delete with uri"+uri.toString());

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case INBOX_ITEMS:
                mDao.bulkDelete();
                break;
            case INBOX_ITEM_ID:
                    String id = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(selection)) {
                        mDao.deleteInboxItem(Long.parseLong(uri.getLastPathSegment()));
                    }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        //Not implemented yet...
        return 0;
    }



}
