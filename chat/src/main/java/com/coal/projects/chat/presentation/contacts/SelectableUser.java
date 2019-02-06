package com.coal.projects.chat.presentation.contacts;

import com.coal.projects.chat.data.pojo.User;

public class SelectableUser extends User {

    private boolean isSelected;

    public SelectableUser(String displayName,
                          String deviceToken,
                          String login,
                          String imageUrl) {
        super(displayName, deviceToken, login, imageUrl);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
