package com.coal.projects.chat.data;

public class ChatUser {

    private String login;
    private String displayName;
    private String photoUrl;

    public ChatUser(String login, String displayName, String photoUrl) {
        this.login = login;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String getLogin() {
        return login;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
