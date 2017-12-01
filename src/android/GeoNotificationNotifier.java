package com.cowbell.cordova.geofence;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class GeoNotificationNotifier {
    private NotificationManager notificationManager;
    private Context context;
    private BeepHelper beepHelper;
    private Logger logger;

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
            String packageName = context.getPackageName();
            Intent resultIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
            //reuse activity if any
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            if (notification.data != null) {
                resultIntent.putExtra("geofence.notification.data", notification.getDataJson());
            }

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                notification.id, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }
        notificationManager.notify(notification.id, mBuilder.build());
        logger.log(Log.DEBUG, notification.toString());
    }
}
