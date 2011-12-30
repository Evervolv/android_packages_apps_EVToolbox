package com.evervolv.toolbox.activities;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;

public class Lockscreen extends SettingsFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);
        
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver cr = getContentResolver();
        
        
    }
}
