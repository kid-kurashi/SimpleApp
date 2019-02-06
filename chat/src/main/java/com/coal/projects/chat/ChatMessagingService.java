package com.coal.projects.chat;

import com.coal.projects.chat.creation.ChatInstance;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            ChatInstance.getInstance().getNotificationHelper().remoteMessageReceived(remoteMessage);
        }
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

}
