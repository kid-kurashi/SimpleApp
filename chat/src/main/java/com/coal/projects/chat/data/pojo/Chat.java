package com.coal.projects.chat.data.pojo;

import java.util.List;
import java.util.Map;

public class Chat {


    private Map<String, String> members;
    private List<Message> messages;
    private String chatId;

    public Chat(Map<String, String> members, List<Message> messages, String chatId) {
        this.members = members;
        this.messages = messages;
        this.chatId = chatId;
    }

    public Chat(Map<String, String> members) {
        this.members = members;
    }

    public Chat() {
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String getChatId() {
        return chatId;
    }
}
