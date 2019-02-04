package com.coal.projects.chat.data.pojo;

import java.util.Date;

public class Message {

    private String messageText;
    private String messageOwner;
    private long messageTime;

    public Message(String messageText, String messageOwner) {
        this.messageText = messageText;
        this.messageOwner = messageOwner;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public Message() {
    }

    public Message(String messageText, String messageOwner, long messageTime) {
        this.messageText = messageText;
        this.messageOwner = messageOwner;
        this.messageTime = messageTime;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageOwner() {
        return messageOwner;
    }

    public void setMessageOwner(String messageOwner) {
        this.messageOwner = messageOwner;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}