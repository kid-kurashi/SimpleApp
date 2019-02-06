package com.coal.projects.chat.creation;

import com.coal.projects.chat.ImageLoader;
import com.coal.projects.chat.NotificationHelper;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.data.SharedPreferecesManager;
import com.coal.projects.chat.domain.PushManager;

public class ChatInstance {

    private static ChatInstance instance;
    private final PushManager pushManager;

    public static ChatInstance getInstance() {
        return instance;
    }

    public ChatInstance(FirebaseRepository firebaseRepository,
                        SharedPreferecesManager sharedPreferecesManager,
                        NotificationHelper notificationHelper,
                        ImageLoader imageLoader,
                        String serverKey) {
        this.firebaseRepository = firebaseRepository;
        this.sharedPreferecesManager = sharedPreferecesManager;
        this.notificationHelper = notificationHelper;
        this.imageLoader = imageLoader;
        this.pushManager = new PushManager(firebaseRepository, serverKey);
        instance = this;
    }

    private FirebaseRepository firebaseRepository;

    private SharedPreferecesManager sharedPreferecesManager;

    private NotificationHelper notificationHelper;

    private ImageLoader imageLoader;

    public FirebaseRepository getFirebaseRepository() {
        return firebaseRepository;
    }

    public SharedPreferecesManager getSharedPreferecesManager() {
        return sharedPreferecesManager;
    }

    public NotificationHelper getNotificationHelper() {
        return notificationHelper;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public PushManager getPushManager() {
        return pushManager;
    }
}
