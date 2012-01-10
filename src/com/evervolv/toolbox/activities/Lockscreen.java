package com.evervolv.toolbox.activities;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.subactivities.LockscreenStyle;
import com.evervolv.toolbox.activities.subactivities.NotificationToolbox;
import com.evervolv.toolbox.utils.ShortcutPickHelper;

public class Lockscreen extends SettingsFragment {

    private static final String LOCKSCREEN_MUSIC_CTRL_VOLBTN = "pref_lockscreen_music_controls_volbtn";
    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";

    private static final String TAG = "EVToolbox";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private CheckBoxPreference mLockscreenMusicCtrlVolBtnPref;
    private PreferenceScreen mLockStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mLockStyle = (PreferenceScreen) mPrefSet.findPreference(
                LOCKSCREEN_STYLE_PREF);
        
        /* Volume button music controls */
        mLockscreenMusicCtrlVolBtnPref = (CheckBoxPreference) mPrefSet.findPreference(LOCKSCREEN_MUSIC_CTRL_VOLBTN);
        mLockscreenMusicCtrlVolBtnPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN, 1) == 1);
    }

    public static class LockStyle extends LockscreenStyle { }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        Log.d(TAG, "ps: " + preference.getKey());
        if (preference == mLockscreenMusicCtrlVolBtnPref) {
            value = mLockscreenMusicCtrlVolBtnPref.isChecked();
            Settings.System.putInt(mCr,
                    Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN, value ? 1 : 0);
            return true;
        } else if (preference == mLockStyle) {
            startPreferencePanel(mLockStyle.getFragment(),
                    null, 0, null, null, -1);
        }
        return false;
    }
}
