package com.coal.projects.chat.data;


import android.content.Context;
import android.content.SharedPreferences;
import com.coal.projects.chat.firestore_constants.Users;

import java.util.Set;

public class SharedPreferecesManager {

    private final Context context;
    private static final String PREF_NAME_FILE_FIREBASECHAT_PREFERENCES = "PREF_NAME_FILE_CHAT_PREFERENCES";
    private final SharedPreferences sharedPreferences;

    public SharedPreferecesManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME_FILE_FIREBASECHAT_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void writeContacts(Set<String> contacts) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(Users.FIELD_CONTACTS, contacts);
        editor.apply();
    }

    public Set<String> readContacts() {
        return sharedPreferences.getStringSet(Users.FIELD_CONTACTS, null);
    }

}
