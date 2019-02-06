package com.coal.projects.chat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.coal.projects.chat.creation.ChatIcons;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.presentation.chat.ChatActivity;
import com.google.firebase.messaging.RemoteMessage;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class NotificationHelper implements AppInnerReceiver.OnReceiveCallback {

    private final Context context;

    private final String N_CHANNEL_ID;
    private final String N_CHANNEL_NAME;
    private final Uri uriSound;
    private final AppInnerReceiver bReceiver;
    private LocalBroadcastManager bManager;
    private NotificationManager nManager;

    private NotificationManagerCompat nManagerCompat;
    private int pushId = 1;

    public static String CHAT_INNER_ACTION = "com.coal.projects.chat.NotificationHelper.CHAT_INNER_ACTION";
    private Intent intent;

    public NotificationHelper(Context mContext) {

        this.context = mContext;

        bReceiver = new AppInnerReceiver();
        bReceiver.setCallback(this);

        registerReceiver(context);

        N_CHANNEL_ID = context.getString(R.string.channel_id);
        N_CHANNEL_NAME = N_CHANNEL_ID + "NAME";

        uriSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotificationManager();
        } else {
            nManagerCompat = NotificationManagerCompat.from(context);
        }

    }

    private void registerReceiver(Context context) {
        bManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CHAT_INNER_ACTION);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void initNotificationManager() {
        nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = new NotificationChannel(
                N_CHANNEL_ID,
                N_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{200, 200, 200});
        if (nManager != null) {
            nManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationCompat.Builder createNewBuilder() {
        pushId++;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, N_CHANNEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(ChatIcons.drawableIcNotification);
        } else {
            builder.setSmallIcon(ChatIcons.mipmapIcNotification);
        }

        builder
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), ChatIcons.drawableIcNotification))
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setColorized(true)
                .setAutoCancel(true)
                .setContentTitle(context.getString(R.string.app_name))
                .setVisibility(VISIBILITY_PUBLIC)
                .setPriority(PRIORITY_MAX)
                .setSound(uriSound);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(N_CHANNEL_ID);
        }
        return builder;
    }


    private void notifyBySpecifyManager(RemoteMessage remoteMessage, int lPushId, Notification notification) {

        if (canNotify(remoteMessage)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nManager.notify(lPushId, notification);
            } else {
                nManagerCompat.notify(lPushId, notification);
            }
        } else {
            playSound();
        }
    }

    private boolean canNotify(RemoteMessage remoteMessage) {
        Log.e("remote", remoteMessage.getData().toString());
        String chatId = "";
        if (intent != null) {
            chatId = intent.getStringExtra(Chats.FIELD_CHAT_ID);
        }
        if (chatId == null)
            return true;
        if (chatId.isEmpty())
            return true;
        return !chatId.equals(remoteMessage.getData().get(Chats.FIELD_CHAT_ID));
    }

    private void playSound() {

    }

    public void remoteMessageReceived(RemoteMessage remoteMessage) {

        NotificationCompat.Builder mBuilder = createNewBuilder();

        if (remoteMessage.getNotification() != null) {

            mBuilder.setContentText(remoteMessage.getNotification().getBody());
            mBuilder.setContentTitle(remoteMessage.getNotification().getTitle());

            route(remoteMessage, mBuilder);

            notifyBySpecifyManager(remoteMessage, pushId, mBuilder.build());
        }
    }

    private void route(RemoteMessage remoteMsg, NotificationCompat.Builder notificationBuilder) {

        if (canNotify(remoteMsg)) {
            notificationBuilder.setContentIntent(getBasePendingIntent(remoteMsg));
        }
    }

    private PendingIntent getBasePendingIntent(RemoteMessage remoteMsg) {
        Intent baseIntent = new Intent(context, ChatActivity.class);
        baseIntent.putExtra(Chats.FIELD_CHAT_ID, remoteMsg.getData().get(Chats.FIELD_CHAT_ID));
        baseIntent.putExtra(Chats.FIELD_DISPLAY_NAMES, remoteMsg.getData().get("title"));
        return PendingIntent.getActivity(context, pushId, baseIntent, PendingIntent.FLAG_ONE_SHOT);
    }

    @Override
    public void onReceive(Intent intent) {
        Log.e("$$$", "NEW INTENT");
        this.intent = intent;
    }
}