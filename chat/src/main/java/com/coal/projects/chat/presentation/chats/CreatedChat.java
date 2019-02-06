package com.coal.projects.chat.presentation.chats;

public class CreatedChat {
    private String chatTitle;
    private String chatId;

    public CreatedChat(String chatTitle, String chatId) {
        this.chatTitle = chatTitle;
        this.chatId = chatId;
    }

    public String getChatTitle() {
        return chatTitle;
    }

    public String getChatId() {
        return chatId;
    }
}
