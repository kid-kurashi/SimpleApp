package com.coal.projects.chat.data.pojo;

public class Message {

    private String messageText;
    private String messageOwner;
    private String messageTime;
    private boolean isRead;

    public Message() {
    }

    public Message(String messageText, String messageOwner, String messageTime, boolean isRead) {
        this.messageText = messageText;
        this.messageOwner = messageOwner;
        this.messageTime = messageTime;
        this.isRead = isRead;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
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

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }
}