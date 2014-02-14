/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.feedmagic;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.content.CursorLoader;
import android.database.Cursor;
import android.content.Loader;
import android.app.LoaderManager;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * This sample demonstrates how to create custom single- or multi-choice
 * {@link android.widget.ListView} UIs. The most interesting bits are in
 * the <code>res/layout/</code> directory of this sample.
 */
public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter mAdapter;
    LruCache<String,Bitmap> bitmapHashMap = new LruCache<String, Bitmap>(200);
    private static String[] sUrl = {
            "http://www.npr.org/rss/rss.php?id=1006",
            "http://feeds.bbci.co.uk/news/rss.xml",
            "https://medium.com/feed/tech-talk",
            "http://feeds.bbci.co.uk/sport/0/rss.xml",
            "http://www.buzzfeed.com/tech.xml"

    };
//    http://www.npr.org/rss/rss.php?id=1006",
//            "http://www.npr.org/rss/rss.php?id=1004",
//            "http://www.npr.org/rss/rss.php?id=1021",
//            "https://medium.com/feed/tech-talk",
//            "http://mobile.reuters.com/reuters/rss/TECH.xml",
//            "http://mobile.reuters.com/reuters/rss/INT.xml",   "
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLoaderManager().initLoader(0, null, this);
        mAdapter = new MyAdapter(this, null);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.text3) {
                    long timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                    String timeText;
                    timeText = new SimpleDateFormat("dd MMM HH:mm").format(new Date(timestamp));
                    TextView tv = (TextView) view;
                    tv.setText(timeText);
                    return true;
                }
                if (view.getId() == android.R.id.icon1) {
                    ImageView iv = (ImageView) view;
                    try {
                        String url =  cursor.getString(cursor.getColumnIndex("subject"));
                        if (url.contains("medium")) {
                            Drawable image = getResources().getDrawable(R.drawable.medium);
                            iv.setImageDrawable(image);
                        } else if (url.contains("npr")) {
                            Drawable image = getResources().getDrawable(R.drawable.npr);
                            iv.setImageDrawable(image);
                        } else if (url.contains("bbc")){
                            Drawable image = getResources().getDrawable(R.drawable.bbc);
                            iv.setImageDrawable(image);
                        } else {
                            Drawable image = getResources().getDrawable(R.drawable.rss);
                            iv.setImageDrawable(image);
                        }

                        return true;
                    } catch (Exception e) {
                    }
                }
                if (view.getId() == android.R.id.icon2) {
                    String url =  cursor.getString(cursor.getColumnIndex("imageurl"));
                    ImageView iv = (ImageView)view;
                    if(url == null || url.isEmpty()){
                        iv.setVisibility(View.GONE);
                    } else {
                        iv.setVisibility(View.VISIBLE);
                        Bitmap bitmap = bitmapHashMap.get(url);
                        if (bitmap == null) {
                            iv.setTag(url);
                            new DownloadImagesTask(bitmapHashMap).execute(iv);
                        } else {
                            iv.setImageBitmap(bitmap);
                        }
                 }
                    return true;
                }

                return false;
            }
        });
        setListAdapter(mAdapter);
        if (isNetworkAvailable(getApplicationContext())) {
            for (String url : sUrl) {
                FeedPuller fp = new FeedPuller(getApplicationContext());
                fp.execute(url);
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "No network connectivity", 10).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, InboxItemProvider.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // mAdapter is a CursorAdapter
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView tv = (TextView) v.findViewById(android.R.id.text2);
        // Perform action on click
        InboxItemDao dao = new InboxItemDao(getApplicationContext());
        String uriString = InboxItemProvider.CONTENT_URI.toString() + "/" + id;
        Cursor cursor = getContentResolver().query(Uri.parse(uriString), null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            InboxItem inboxitem = dao.cursorToInboxItem(cursor);
            Uri uriUrl = Uri.parse(inboxitem.getSubject().trim());
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        String uriString;
        Toast toast;
        switch (item.getItemId()) {
            case R.id.action_add_item:
                toast = Toast.makeText(getApplicationContext(), "Refresh feeds..", 10);
                toast.show();
                for (String url : sUrl) {
                    FeedPuller fp = new FeedPuller(getApplicationContext());
                    fp.execute(url);
                }
                return true;
            case R.id.action_delete_item:
                uriString = InboxItemProvider.CONTENT_URI.toString();
                getContentResolver().delete(Uri.parse(uriString), null, null);
                toast = Toast.makeText(getApplicationContext(), "Clearing feeds...", 10);
                toast.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class MyAdapter extends SimpleCursorAdapter {
        public MyAdapter(Context context, Cursor c) {
            super(context, R.layout.list_item, c, new String[]{"imageurl","imageurl","name", "body", "timestamp", }, new int[]{android.R.id.icon1,android.R.id.icon2, android.R.id.text1, android.R.id.text2, R.id.text3}, 0);
        }
    }

    public static boolean isNetworkAvailable(Context context)
    {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

}
