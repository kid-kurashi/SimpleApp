package com.coal.projects.chat.data.push;

public class Data {

    private String body;
    private String title;
    private String chatId;
    private String type;

    public Data withBody(String body) {
        this.body = body;
        return this;
    }

    public Data withTitle(String title) {
        this.title = title;
        return this;
    }

    public Data withChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public Data withType(String type) {
        this.type = type;
        return this;
    }
}
