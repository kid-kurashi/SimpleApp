package com.coal.projects.chat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.google.firebase.messaging.RemoteMessage;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class ChatNotificationHelper {

    private final Context context;

    private final String N_CHANNEL_ID;
    private final String N_CHANNEL_NAME;
    private final Uri uriSound;
    private NotificationManager nManager;

    private NotificationManagerCompat nManagerCompat;
    private int pushId = 1;

    public ChatNotificationHelper(Context context) {

        this.context = context;

        N_CHANNEL_ID = context.getString(R.string.channel_id);
        N_CHANNEL_NAME = N_CHANNEL_ID + "NAME";

        uriSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotificationManager();
        } else {
            nManagerCompat = NotificationManagerCompat.from(context);
        }

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


    private void notifyBySpecifyManager(int lPushId, Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nManager.notify(lPushId, notification);
        } else {
            nManagerCompat.notify(lPushId, notification);
        }
    }

    public void remoteMessageReceived(RemoteMessage remoteMessage) {

        NotificationCompat.Builder mBuilder = createNewBuilder();

        if (remoteMessage.getNotification() != null) {

            mBuilder.setContentText(remoteMessage.getNotification().getBody());
            mBuilder.setContentTitle(remoteMessage.getNotification().getTitle());

            route(remoteMessage, mBuilder);

            notifyBySpecifyManager(pushId, mBuilder.build());
        }
    }

    private void route(RemoteMessage remoteMsg, NotificationCompat.Builder notificationBuilder) {

        String type = remoteMsg.getData().get(Consts.TYPE);
        if (type != null)
            switch (Integer.valueOf(type)) {
                case Consts.SOME_TYPE: {

                }
                break;
                default:
                    break;
            }

//                notificationBuilder.setContentIntent(getBasePendingIntent());
//
//                notificationBuilder.setContentIntent(getCardDetailsPendingIntent(remoteMsg));

    }

//    private PendingIntent getCardDetailsPendingIntent(RemoteMessage remoteMessage) {
//        Intent intent = new Intent(context, PreviewActivity.class);
//        intent.putExtra(Consts.KEY_ID, Integer.valueOf(remoteMessage.getData().get(Consts.POSTCARD_ID)));
//        intent.putExtra(Consts.KEY_REDIRECT_BACK, true);
//        return PendingIntent.getActivity(context, pushId, intent, PendingIntent.FLAG_ONE_SHOT);
//    }
//
//    private PendingIntent getBasePendingIntent() {
//        Intent baseIntent = new Intent(context, LoginActivity.class);
//        baseIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        return PendingIntent.getActivity(context, pushId, baseIntent, PendingIntent.FLAG_ONE_SHOT);
//    }

    public static class Consts {
        static final String TYPE = "type";
        public static final int SOME_TYPE = 1;

    }
}