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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This sample demonstrates how to create custom single- or multi-choice
 * {@link android.widget.ListView} UIs. The most interesting bits are in
 * the <code>res/layout/</code> directory of this sample.
 */
public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter mAdapter;
    LruCache<String, Bitmap> bitmapHashMap = new LruCache<String, Bitmap>(200);
    Feeds mFeeds = new Feeds();

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
                        String url = cursor.getString(cursor.getColumnIndex("subject"));
                        if (url.contains("medium")) {
                            Drawable image = getResources().getDrawable(R.drawable.medium);
                            iv.setImageDrawable(image);
                        } else if (url.contains("npr")) {
                            Drawable image = getResources().getDrawable(R.drawable.npr);
                            iv.setImageDrawable(image);
                        } else if (url.contains("bbc")) {
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
                    String url = cursor.getString(cursor.getColumnIndex("imageurl"));
                    ImageView iv = (ImageView) view;
                    if (url == null || url.isEmpty()) {
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
            Handler myHandler = new DelayedHandler();
            Message m = new Message();
            myHandler.sendMessageDelayed(m, 2000);
        } else {
            Toast.makeText(getApplicationContext(), "No network connectivity", 10).show();
        }
    }


    class DelayedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            FeedPuller fp = new FeedPuller(getApplicationContext());
            fp.execute(mFeeds.getFeedUrls());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader loader = null;
        if (bundle != null && !bundle.isEmpty()) {
            String uri = bundle.getString("uri");
            if (uri != null && !uri.isEmpty()) {
                loader = new CursorLoader(this, Uri.parse(uri),
                        null, null, null, null);
            }
        } else {
            loader = new CursorLoader(this, FeedItemProvider.CONTENT_URI,
                    null, null, null, null);
        }
        return loader;
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
        FeedItemDao dao = new FeedItemDao(getApplicationContext());
        String uriString = FeedItemProvider.CONTENT_URI.toString() + "/" + id;
        Cursor cursor = getContentResolver().query(Uri.parse(uriString), null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            FeedItem inboxitem = dao.cursorToFeedItem(cursor);
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
        Bundle bundle = new Bundle();
        Toast toast;
        switch (item.getItemId()) {
            case R.id.action_add_item:
                if (!isNetworkAvailable(getApplicationContext())) {
                    toast = Toast.makeText(getApplicationContext(), "No network connectivity..", 10);
                } else {
                    toast = Toast.makeText(getApplicationContext(), "Refresh feeds..", 10);
                }
                toast.show();
                FeedPuller fp = new FeedPuller(getApplicationContext());
                fp.execute(mFeeds.getFeedUrls());
                return true;
            case R.id.action_delete_item:
                uriString = FeedItemProvider.CONTENT_URI.toString();
                getContentResolver().delete(Uri.parse(uriString), null, null);
                toast = Toast.makeText(getApplicationContext(), "Clearing feeds...", 10);
                toast.show();
                return true;
            case R.id.action_technology:
                Feeds.FeedType.Technology.toString();
                uriString = FeedItemProvider.CONTENT_URI_FILTER.toString() + Feeds.FeedType.Technology.toString();
                bundle.putString("uri", uriString);
                getLoaderManager().restartLoader(0, bundle, this);
                return true;
            case R.id.action_news:
                Feeds.FeedType.News.toString();
                uriString = FeedItemProvider.CONTENT_URI_FILTER.toString() + Feeds.FeedType.News.toString();
                bundle.putString("uri", uriString);
                getLoaderManager().restartLoader(0, bundle, this);
                return true;
            case R.id.action_sports:
                Feeds.FeedType.Sports.toString();
                uriString = FeedItemProvider.CONTENT_URI_FILTER.toString() + Feeds.FeedType.Sports.toString();
                bundle.putString("uri", uriString);
                getLoaderManager().restartLoader(0, bundle, this);
                return true;
            case R.id.action_business:
                Feeds.FeedType.Business.toString();
                uriString = FeedItemProvider.CONTENT_URI_FILTER.toString() + Feeds.FeedType.Business.toString();
                bundle.putString("uri", uriString);
                getLoaderManager().restartLoader(0, bundle, this);
                return true;
            case R.id.action_health:
                Feeds.FeedType.Health.toString();
                uriString = FeedItemProvider.CONTENT_URI_FILTER.toString() + Feeds.FeedType.Health.toString();
                bundle.putString("uri", uriString);
                getLoaderManager().restartLoader(0, bundle, this);
                return true;
            case R.id.action_us:
                Feeds.FeedType.US.toString();
                uriString = FeedItemProvider.CONTENT_URI_FILTER.toString() + Feeds.FeedType.US.toString();
                bundle.putString("uri", uriString);
                getLoaderManager().restartLoader(0, bundle, this);
                return true;
            case R.id.action_all_feeds:
                getLoaderManager().restartLoader(0, null, this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class MyAdapter extends SimpleCursorAdapter {
        public MyAdapter(Context context, Cursor c) {
            super(context, R.layout.list_item, c, new String[]{"imageurl", "imageurl", "name", "body", "timestamp",}, new int[]{android.R.id.icon1, android.R.id.icon2, android.R.id.text1, android.R.id.text2, R.id.text3}, 0);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

}
