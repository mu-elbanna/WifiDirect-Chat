package com.example.wifidirectchat.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "messages")
public class MessageEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "content")
    private String message;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "addressee")
    private String addressee;

    @ColumnInfo(name = "sent_by_me")
    private boolean sentByMe;

    @Ignore
    private boolean dateVisible;

    public MessageEntity(String message, Date date, String addressee, boolean sentByMe) {
        this.message = message;
        this.date = date;
        this.addressee = addressee;
        this.sentByMe = sentByMe;
        dateVisible = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }

    public void setSentByMe(boolean sentByMe) {
        this.sentByMe = sentByMe;
    }

    public boolean isDateVisible() {
        return dateVisible;
    }

    public void setDateVisible(boolean dateVisible) {
        this.dateVisible = dateVisible;
    }

}
