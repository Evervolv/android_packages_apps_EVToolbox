package com.evervolv.toolbox.activities.subactivities;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;

public class StatusbarIcons extends SettingsFragment {

    private static final String STATUSBAR_SIXBAR_SIGNAL = "pref_statusbar_sixbar_signal";
    
    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    
    private CheckBoxPreference mUseSixbaricons;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbar_icons);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        
        /* Volume button music controls */
        mUseSixbaricons = (CheckBoxPreference) mPrefSet.findPreference(
                STATUSBAR_SIXBAR_SIGNAL);
        mUseSixbaricons.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUSBAR_6BAR_SIGNAL, 1) == 1);
        
    }
    
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mUseSixbaricons) {
            value = mUseSixbaricons.isChecked();
            Settings.System.putInt(mCr, Settings.System.STATUSBAR_6BAR_SIGNAL,
                    value ? 1 : 0);
            return true;   
        }
        return false;
    }
}
