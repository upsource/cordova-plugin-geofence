package com.cowbell.cordova.geofence;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import java.util.Random;

public class GeoNotificationNotifier {
    private NotificationManager notificationManager;
    private Context context;
    private BeepHelper beepHelper;
    private Logger logger;
    private final Random random = new Random();

    public GeoNotificationNotifier(NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;
        this.beepHelper = new BeepHelper();
        this.logger = Logger.getLogger();
    }

    public void notify(Notification notification) {
        notification.setContext(context);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, GeofencePlugin.DEFAULT_CHANNEL_ID)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND)
            .setVibrate(notification.getVibrate())
            .setSmallIcon(notification.getSmallIcon())
            .setLargeIcon(notification.getLargeIcon())
            .setAutoCancel(true)
            .setContentTitle(notification.getTitle())
            .setContentText(notification.getText());

        if (notification.openAppOnClick) {

            Intent intent = new Intent(context, ClickReceiver.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            if (notification.data != null) {
                intent.putExtra("geofence.notification.data", notification.getDataJson());
            }

            int reqCode = random.nextInt();

            PendingIntent contentIntent = PendingIntent.getActivity(
                    context, reqCode, intent, FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(contentIntent);
        }
        notificationManager.notify(notification.id, mBuilder.build());
        logger.log(Log.DEBUG, notification.toString());
    }
}
