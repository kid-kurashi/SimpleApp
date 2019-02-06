package com.coal.projects.chat.data.pojo;

import java.util.ArrayList;
import java.util.List;

public class User {

    protected String displayName;
    protected String deviceToken;
    protected List<String> contacts;
    protected String login;
    protected String imageUrl;

    public User(String displayName,
                String deviceToken,
                String login,
                String imageUrl) {
        this.displayName = displayName;
        this.deviceToken = deviceToken;
        this.login = login;
        this.contacts = new ArrayList<>();
        this.imageUrl = imageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
