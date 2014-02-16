package com.example.android.feedmagic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by pdamu on 2/14/14.
 */
public class Feeds {
     enum FeedType{
        Unknown,
        Technology,
        News ,
        Sports ,
        Business,
        Health,
        US

    };

    HashMap <String,FeedType> mFeedMap = new HashMap<String, FeedType>();

    public Feeds(){
        mFeedMap.put("http://www.npr.org/rss/rss.php?id=1006",FeedType.News);
        mFeedMap.put("http://feeds.bbci.co.uk/news/world/rss.xml",FeedType.News);
        mFeedMap.put("http://www.npr.org/rss/rss.php?id=1003",FeedType.US);
        mFeedMap.put("https://medium.com/feed/tech-talk",FeedType.Technology);
        mFeedMap.put("http://www.buzzfeed.com/tech.xml",FeedType.Technology);
        mFeedMap.put("http://feeds.bbci.co.uk/news/technology/rss.xml",FeedType.Technology);
        mFeedMap.put("http://feeds.bbci.co.uk/sport/0/rss.xml",FeedType.Sports);
        mFeedMap.put("http://feeds.bbci.co.uk/news/business/rss.xml",FeedType.Business);
        mFeedMap.put("http://rss.nytimes.com/services/xml/rss/nyt/Business.xml",FeedType.Business);
        mFeedMap.put("http://feeds.bbci.co.uk/news/health/rss.xml",FeedType.Health);
        mFeedMap.put("http://www.health.com/health/diet-fitness/feed",FeedType.Health);


    }

    public FeedType getFeedType(String url){
        FeedType type = mFeedMap.get(url);
        return type;
    }

    public String[] getFeedUrls(){
        Set<String> urlSet =  mFeedMap.keySet();
        String urls[] = new String[urlSet.size()];
        Iterator it = urlSet.iterator();
        int index = 0;
        while (it.hasNext()){
            urls[index++] = (String)it.next();
        }
        return urls;
    }

////    http://www.npr.org/rss/rss.php?id=1006",
////            "http://www.npr.org/rss/rss.php?id=1004",
////            "http://www.npr.org/rss/rss.php?id=1021",
////            "https://medium.com/feed/tech-talk",
////            "http://mobile.reuters.com/reuters/rss/TECH.xml",
////            "http://mobile.reuters.com/reuters/rss/INT.xml",   "
//};

}
