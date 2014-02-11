package com.example.android.feedmagic;

/**
 * Created by pdamu on 1/14/14.
 */
public class InboxItem {
    private int mId;
    private String mName;
    private String mSubject;
    private String mGuid;
    private  String mImageUrl;
    private String mBody;
    private long mTimeStamp;


    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }


    public String getGuid() {
        return mGuid;
    }

    public void setGuid(String mGuid) {
        this.mGuid = mGuid;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String mBody) {
        this.mBody = mBody;
    }

    public String getSubject() {
        return mSubject;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setId(int inboxitemid) {
        mId = inboxitemid;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setSubject(String subject) {
        mSubject = subject;
    }

    public void setTimeStamp(long timeStamp) {
        mTimeStamp = timeStamp;
    }
}
