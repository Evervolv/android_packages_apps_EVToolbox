package com.evervolv.toolbox.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.subactivities.NotificationToolbox;

public class Statusbar extends SettingsFragment {

    private static final String TAG = "EVToolbox";

    private static final String NOTIFICATION_TOOLBOX_PREF = "pref_notification_toolbox";

    private PreferenceScreen mPrefSet;
    private PreferenceScreen mNotifToolbox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_settings);

        mPrefSet = getPreferenceScreen();

        mNotifToolbox = (PreferenceScreen) mPrefSet.findPreference(
                NOTIFICATION_TOOLBOX_PREF);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mNotifToolbox) {
            startPreferencePanel(mNotifToolbox.getFragment(),
                    null, mNotifToolbox.getTitleRes(), null, null, -1);
            return true;
        }
        return false;
    }

    public static class NotifToolbox extends NotificationToolbox { }

}
