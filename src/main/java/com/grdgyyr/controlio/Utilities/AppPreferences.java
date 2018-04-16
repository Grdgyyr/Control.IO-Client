package com.grdgyyr.controlio.Utilities;

import android.content.SharedPreferences;

import com.grdgyyr.controlio.Fragments.SettingsActivity;


public class AppPreferences {

    SharedPreferences preferences;

    public AppPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getServerBaseUrl() {
        String baseUrl = preferences.getString(SettingsActivity.KEY_PREF_SERVER, null);
        if (baseUrl != null && !baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        return baseUrl;
    }
}
