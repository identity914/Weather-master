package com.yuleshchenko.weather;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yuleshchenko.weather.services.ServiceNotification;
import com.yuleshchenko.weather.settings.FragmentSettings;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class WeatherApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Configure Realm
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name("weather.db")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded() // Drop the db if migration is needed
                .build();
        Realm.setDefaultConfiguration(config);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);  // set default settings

        // Read from settings if notification service should be start
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isStartService = sharedPref.getBoolean(FragmentSettings.PREF_SERVICE, true);
        if (isStartService) {
            // Starting the notification service
            Intent intentNotification = new Intent(this, ServiceNotification.class);
            this.startService(intentNotification);
        }
    }
}
