package com.coal.projects.chat.data.push;

public class Notification {

    private String body;
    private boolean contentAvailable;
    private String priority;
    private String title;

    public Notification withBody(String body) {
        this.body = body;
        return this;
    }

    public Notification withContentAvailable(boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
        return this;
    }

    public Notification withPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public Notification withTitle(String title) {
        this.title = title;
        return this;
    }

}