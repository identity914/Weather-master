package com.yuleshchenko.weather.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.yuleshchenko.weather.activities.ActivityMain;
import com.yuleshchenko.weather.R;

import java.util.Timer;
import java.util.TimerTask;

public class ServiceNotification extends Service {

    public static final int NOTIFICATION_ID = 0;
    public static final String STARTED_FROM_NOTIFICATION = "com.yuleshchenko.weather.services.StartedFromNotification";

    private android.support.v7.app.NotificationCompat.Builder builder;
    private NotificationManager mNotificationManager;
    private Timer timer;

    public ServiceNotification() {}

    @Override
    public void onCreate() {
        super.onCreate();
        // Initial setup:
        Intent intentDownload = new Intent(this, ServiceDataLoader.class);
        intentDownload.putExtra(STARTED_FROM_NOTIFICATION, true); // this is for progressbar
        PendingIntent pendingIntentDownLoad = PendingIntent
                .getService(this, 0, intentDownload, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intentStartApp = new Intent(this, ActivityMain.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntentApp = PendingIntent
                .getActivity(this, 0, intentStartApp, PendingIntent.FLAG_UPDATE_CURRENT);
        // Notification construction
        builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_weather_notification)  // App icon
                .setContentTitle(this.getResources().getString(R.string.app_name))  // App name
                .setContentText(getString(R.string.new_update_available))
                .setContentIntent(pendingIntentApp)  // Do when notification clicked
                .setAutoCancel(true)  // Dismiss when touched
                .addAction(R.drawable.ic_loop_white_48dp, "Update", pendingIntentDownLoad);  // Do when action clicked
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Starting timer ... after 5 sec., repeating every 60 sec.
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            synchronized public void run() {
                mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                Log.v(ActivityMain.MY_LOG, "New update ...");
            }
        }, 1000, 60*1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return Service.START_STICKY; }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  // Not implemented
    }

    @Override
    public void onDestroy() {
        mNotificationManager.cancel(NOTIFICATION_ID);
        timer.cancel();
        super.onDestroy();
    }
}
