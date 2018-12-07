package com.yuleshchenko.weather.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.yuleshchenko.weather.R;
import com.yuleshchenko.weather.services.ServiceDataLoader;
import com.yuleshchenko.weather.services.ServiceNotification;

public class FragmentSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_SERVICE = "pref_service";
    public static final String PREF_CITY = "pref_city";

    public FragmentSettings() {
        /* Required empty public constructor */
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        // Read the city from settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String city = sharedPref.getString(FragmentSettings.PREF_CITY, "Beijing");
        // Set city as summary
        Preference pref = findPreference(PREF_CITY);
        pref.setSummary(city);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_SERVICE)) {
            Intent intentNotification = new Intent(getActivity(), ServiceNotification.class);
            if (sharedPreferences.getBoolean(PREF_SERVICE, false)) {
                getActivity().startService(intentNotification);
            } else {
                getActivity().stopService(intentNotification);
            }
        }
        if (key.equals(PREF_CITY)) {
            Preference pref = findPreference(key);
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(etp.getText());
            // Updating the local db in background
            Intent intentUpdate = new Intent(getActivity(), ServiceDataLoader.class);
            getActivity().startService(intentUpdate);
        }
    }
}
