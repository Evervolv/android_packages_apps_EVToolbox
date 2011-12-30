package com.evervolv.toolbox.activities;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;

public class Lockscreen extends SettingsFragment implements
        OnPreferenceChangeListener {

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_settings_style";

    private static final int LOCK_STYLE_GB   = 1;
    private static final int LOCK_STYLE_ECLAIR = 2;
    private static final int LOCK_STYLE_ICS = 3;
    private ListPreference mLockscreenStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);
        
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver cr = getContentResolver();
        
        
        /* Lockscreen style */
        mLockscreenStyle = (ListPreference) prefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        mLockscreenStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenStyle) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_STYLE,
                    Integer.valueOf((String) newValue));
            return true;
        }
        return false;
    }
}
