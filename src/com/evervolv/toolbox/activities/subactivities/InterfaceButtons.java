package com.evervolv.toolbox.activities.subactivities;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;

public class InterfaceButtons extends SettingsFragment {

    private static final String TRACKBALL_WAKE_TOGGLE = "pref_trackball_wake_toggle";
    private static final String BUTTON_CATEGORY = "pref_category_button_settings";
    private static final String VOLUME_WAKE_TOGGLE = "pref_volume_wake_toggle";
    private static final String LOCKSCREEN_MUSIC_CTRL_VOLBTN = "pref_lockscreen_music_controls_volbtn";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mTrackballWake;
    private CheckBoxPreference mVolumeWake;
    private CheckBoxPreference mMusicCtrlVolBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.interface_buttons);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        PreferenceCategory buttonCategory = (PreferenceCategory) mPrefSet
                .findPreference(BUTTON_CATEGORY);

        /* Trackball wake pref */
        mTrackballWake = (CheckBoxPreference) mPrefSet.findPreference(
                TRACKBALL_WAKE_TOGGLE);
        mTrackballWake.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.TRACKBALL_WAKE_SCREEN, 1) == 1);

        /* Volume wake pref */
        mVolumeWake = (CheckBoxPreference) mPrefSet.findPreference(VOLUME_WAKE_TOGGLE);
        mVolumeWake.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        /* Volume button music controls */
        mMusicCtrlVolBtn = (CheckBoxPreference) mPrefSet.findPreference(LOCKSCREEN_MUSIC_CTRL_VOLBTN);
        mMusicCtrlVolBtn.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN, 1) == 1);

        /* Remove mTrackballWake on devices without trackballs */
        if (!getResources().getBoolean(R.bool.has_trackball)) {
            buttonCategory.removePreference(mTrackballWake);
        }

    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mTrackballWake) {
            value = mTrackballWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.TRACKBALL_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mVolumeWake) {
            value = mVolumeWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.VOLUME_WAKE_SCREEN,
                    value ? 1 : 0);
            if (mVolumeWake.isChecked()) {
                mMusicCtrlVolBtn.setChecked(false);
                Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN, 0);
            }
            return true;
        } else if (preference == mMusicCtrlVolBtn) {
            value = mMusicCtrlVolBtn.isChecked();
            Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN,
                    value ? 1 : 0);
            if (mMusicCtrlVolBtn.isChecked()) {
                mVolumeWake.setChecked(false);
                Settings.System.putInt(mCr, Settings.System.VOLUME_WAKE_SCREEN, 0);
            }
            return true;
        }
        return false;
    }

}
