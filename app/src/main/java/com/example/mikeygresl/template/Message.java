package com.example.mikeygresl.template;

import android.os.Parcel;
import android.text.TextUtils;

public class Message {

    private String MID;
    private long timestamp;
    private String type;
    private String sender_email;
    private String rec_email;
    private String content;
    private String URL;
    private String sender_id;

    public Message(String MID, long timestamp, String type, String sender_email, String rec_email, String content, String URL) {
        this.MID = MID;
        this.type = type;
        this.sender_email = sender_email;
        this.rec_email = rec_email;
        this.content = content;
        this.URL = URL;

        setTimestamp(timestamp);
    }

    public Message() {

//        Long tsLong = System.currentTimeMillis() / 1000;
//        this.timestamp = tsLong.toString();
        this.timestamp = System.currentTimeMillis();
    }

    protected Message(Parcel in) {
        MID = in.readString();
        timestamp = in.readLong();
        type = in.readString();
        sender_email = in.readString();
        rec_email = in.readString();
        content = in.readString();
        URL = in.readString();
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMID(String MID) {
        this.MID = MID;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSender_email(String sender_email) {
        this.sender_email = sender_email;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getMID() {

        return MID;
    }

    public void setRec_email(String rec_email) {
        this.rec_email = rec_email;
    }

    public String getRec_email() {

        return rec_email;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getSender_email() {
        return sender_email;
    }

    public String getContent() {
        return content;
    }

    public String getURL() {
        return URL;
    }

    public boolean hasImage() {
        return !TextUtils.isEmpty(getURL());
    }
}
