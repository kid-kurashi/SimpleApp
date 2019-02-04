package com.coal.projects.chat;

import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.data.SharedPreferecesManager;
import org.jetbrains.annotations.NotNull;

public class ChatInstance {
    @NotNull
    public static FirebaseRepository firebaseRepository;
    @NotNull
    public static SharedPreferecesManager sharedPreferecesManager;
    @NotNull
    public static ChatNotificationHelper chatNotificationHelper;
}
