package com.evervolv.toolbox.activities;

import android.os.Bundle;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.subactivities.NotificationToolbox;

public class Statusbar extends SettingsFragment {

    private static final String TAG = "EVToolbox";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_settings);
    }

    public static class NotifToolbox extends NotificationToolbox { }

}
