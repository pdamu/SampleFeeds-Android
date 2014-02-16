package com.example.android.feedmagic;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;


/**
 * Created by pdamu on 2/2/14.
 */
public class FeedPuller extends AsyncTask<String, Void, List<FeedItem>> {

    private Context mContext;
    private List<FeedItem> mItems;


    public FeedPuller(Context context) {
        mContext = context;
    }

    protected void onPreExecute() {

    }

    protected List<FeedItem> doInBackground(String... urls) {
        RssHandler rh = new RssHandler();
        try {
            for (String urlString : urls) {
                URL feedUrl = new URL(urlString);
                try {
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    rh.setCurrentSource(urlString);
                    xr.setContentHandler(rh);
                    xr.parse(new InputSource(feedUrl.openStream()));
                } catch (Exception ioe) {
                    Log.v("FeedPuller", "IO exception" + urls);
                }
            }
        } catch (MalformedURLException mfe) {
            Log.v("FeedPuller", "Malformed URL" + urls);
        }
        mItems = rh.getMessages();

        ContentValues values = new ContentValues();
        for (FeedItem item : mItems) {
            if (item.getName().length() < 100) {
                values.put("title", item.getName().trim());
                values.put("link", item.getSubject());
                StringBuffer description = new StringBuffer(Html.fromHtml(item.getBody()).toString().trim());
                description.trimToSize();
                if(description.length() > 150 ){
                    description.setLength(150);
                    description.append("..");
                }
                values.put("description",description.toString() );
                values.put("guid", item.getGuid());
                values.put("timestamp", item.getTimeStamp());
                values.put("imageurl",item.getImageUrl());
                values.put("feedtype",item.getType());
            }

            if (values.size() > 0) {
                String uriString = FeedItemProvider.CONTENT_URI.toString();
                mContext.getContentResolver().insert(Uri.parse(uriString), values);
            }
        }
        return mItems;
    }

    protected void onPostExecute(List inboxItems) {

    }


}
