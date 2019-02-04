package com.coal.projects.chat.data.pojo;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String displayName;
    private String uId;
    private String deviceToken;
    private List<String> contacts;
    private String login;

    public User(String displayName, String uId, String deviceToken, String login) {
        this.displayName = displayName;
        this.uId = uId;
        this.deviceToken = deviceToken;
        this.login = login;
        this.contacts = new ArrayList<>();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
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

    public User update(User user) {
        this.displayName = user.getDisplayName();
        this.uId = user.getuId();
        this.deviceToken = user.getDeviceToken();
        this.contacts = user.getContacts();
        return this;
    }
}
