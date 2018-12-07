package com.yuleshchenko.weather.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.yuleshchenko.weather.activities.ActivityMain;
import com.yuleshchenko.weather.R;
import com.yuleshchenko.weather.activities.fragments.FragmentList;
import com.yuleshchenko.weather.settings.FragmentSettings;
import com.yuleshchenko.weather.util.DataLoader;

public class ServiceDataLoader extends Service {

    private boolean isConnected;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;

    public ServiceDataLoader() {}

    @Override
    public void onCreate() {
        super.onCreate();
        isConnected = isNetworkConnected();  // check if internet connected
        if (!isConnected) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
            stopSelf();  // !!! stop service
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isConnected) {
            // Show progress bar in notification aria
            final boolean flag = intent.getBooleanExtra(ServiceNotification.STARTED_FROM_NOTIFICATION, false);
            if (flag) {
                // Setting notification fields
                Intent intentStartApp = new Intent(this, ActivityMain.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntentApp = PendingIntent
                        .getActivity(this, 0, intentStartApp, PendingIntent.FLAG_UPDATE_CURRENT);
                // Notification construction
                builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setAutoCancel(true)  // Dismiss when touched
                        .setSmallIcon(R.drawable.ic_weather_notification)  // App icon
                        .setContentTitle(this.getResources().getString(R.string.app_name))  // App name
                        .setContentText("Updating ...")
                        .setProgress(0, 0, true)
                        .setContentIntent(pendingIntentApp);  // Do when notification clicked
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(ServiceNotification.NOTIFICATION_ID, builder.build());
            }

            DataLoader dataLoader = new DataLoader() {
                @Override
                protected void onPostExecute(Void aVoid) {
                    // Notify the RecyclerView fragment about DB update
                    Intent backIntent = new Intent(FragmentList.ResponseReceiver.BROADCAST_ACTION);
                    backIntent.putExtra(FragmentList.ResponseReceiver.STATUS, true);
                    // Broadcast the STATUS = true
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(backIntent);
                    if (flag) {
                        // Remove progress bar in notification aria
                        builder.setProgress(0, 0, false)
                                .setContentText("Download complete.");
                        mNotificationManager.notify(ServiceNotification.NOTIFICATION_ID, builder.build());
                    }
                }
            };
            // Read the city from settings
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String city = sharedPref.getString(FragmentSettings.PREF_CITY, "Beijing");
            dataLoader.execute(city);
        }

        stopSelf();  // !!! stop service
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html#DetermineConnection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
