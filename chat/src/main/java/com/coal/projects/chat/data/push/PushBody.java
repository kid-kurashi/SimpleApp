package com.coal.projects.chat.data.push;

import android.util.Log;
import com.coal.projects.chat.firestore_constants.PushTypes;

public class PushBody {
    private String to;
    private Notification notification;
    private Data data;
    private String collapse_key;

    public PushBody withTo(String to) {
        this.to = to;
        return this;
    }

    public PushBody withNotification(Notification notification) {
        this.notification = notification;
        return this;
    }


    public PushBody withData(Data data) {
        this.data = data;
        return this;
    }

    public PushBody withCollapseKey(String collapse_key) {
        this.collapse_key = collapse_key;
        return this;
    }

    public static PushBody create(String token, String message, String chatId, String displayName) {
        Log.e("TOKEN_TO", token);
        return new PushBody()
                .withTo(token)
                .withCollapseKey("type_a")
                .withNotification(
                        new Notification()
                                .withTitle(displayName)
                                .withBody(message)
                                .withContentAvailable(true)
                                .withPriority("high"))
                .withData(
                        new Data()
                                .withChatId(chatId)
                                .withTitle(displayName)
                                .withBody(message)
                                .withType(PushTypes.CHAT_MESSAGE)
                );
    }
}