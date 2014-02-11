package com.example.android.feedmagic;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RssHandler extends DefaultHandler {
    private List<InboxItem> messages =  new ArrayList<InboxItem>();;
    private InboxItem currentMessage;
    private StringBuilder builder;

    // names of the XML tags
    static final String RSS = "rss";
    static final String CHANNEL = "channel";
    static final String ITEM = "item";

    static final String PUB_DATE = "pubDate";
    static final String DESCRIPTION = "description";
    static final String LINK = "link";
    static final String TITLE = "title";
    static final String GUID = "guid";
    static final String IMAGE = "image";


    public List<InboxItem> getMessages(){
        return this.messages;
    }
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        builder.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        super.endElement(uri, localName, name);
        if (this.currentMessage != null){
            if (localName.equalsIgnoreCase(TITLE)){
                currentMessage.setName(builder.toString());
            } else if (localName.equalsIgnoreCase(LINK)){
                currentMessage.setSubject(builder.toString());
            } else if (localName.equalsIgnoreCase(DESCRIPTION)){
                currentMessage.setBody(builder.toString());
            } else if (localName.equalsIgnoreCase(PUB_DATE)){
                currentMessage.setTimeStamp(parseDate(builder.toString()));
            }else if (localName.equalsIgnoreCase(GUID)){
                currentMessage.setGuid(builder.toString());
            } else if (localName.equalsIgnoreCase("media")){
                //currentMessage.setGuid(builder.toString());
                Log.v("feedmagic", "Image url is XXXX " + builder.toString());

            } else if (localName.equalsIgnoreCase(ITEM)){
                    messages.add(currentMessage);
            }
            builder.setLength(0);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        builder = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if (localName.equalsIgnoreCase(ITEM)){
            this.currentMessage = new InboxItem();
        }

        if (localName.equalsIgnoreCase(IMAGE)){
            Log.v("feedmagic", "Image url is XXXX ");
        }
        if(localName.equals("thumbnail")){
            if(attributes.getValue("url") !=null){
                String imageurl = attributes.getValue("url");
                Log.v("feedmagic", "thumbnail url is  " + imageurl);
                this.currentMessage.setImageUrl(imageurl);
            }
        }
    }
    private long parseDate(String dateString){
        long timeStamp = 0;
        dateString = dateString.replace('\n',' ');
        dateString = dateString.trim();
        try {
            DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            Date f = formatter.parse(dateString);
            timeStamp = f.getTime();

        }catch (Exception e){
            timeStamp = System.currentTimeMillis();
        }
        return timeStamp;
    }
}