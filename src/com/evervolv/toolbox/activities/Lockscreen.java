package com.evervolv.toolbox.activities;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.subactivities.LockscreenStyle;

public class Lockscreen extends SettingsFragment implements OnPreferenceChangeListener {

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";
    private static final String LOCKSCREEN_TRANSPARENT_PREF = "pref_lockscreen_transparent";
    private static final String LOCKSCREEN_MESSAGE_PREF = "pref_lockscreen_message";

    private static final String TAG = "EVToolbox";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private PreferenceScreen mLockStyle;
    private EditTextPreference mLockMessage;
    private CheckBoxPreference mLockTransparent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mLockStyle = (PreferenceScreen) mPrefSet.findPreference(
                LOCKSCREEN_STYLE_PREF);

        mLockMessage = (EditTextPreference) mPrefSet.findPreference(
                LOCKSCREEN_MESSAGE_PREF);
        mLockMessage.setOnPreferenceChangeListener(this);
        String message = Settings.System.getString(mCr,
                Settings.System.LOCKSCREEN_MESSAGE);
        setLockMessageSummaryAndText(message);

        mLockTransparent = (CheckBoxPreference) mPrefSet.findPreference(
                LOCKSCREEN_TRANSPARENT_PREF);
        // unimplemented yet
        mPrefSet.removePreference(mLockTransparent);
    }

    private void setLockMessageSummaryAndText(String message) {
        if (message == null || message.equals("")) {
            mLockMessage.setText("");
            mLockMessage.setSummary(R.string
                    .pref_lockscreen_message_summary);
        } else {
            mLockMessage.setSummary(message);
            mLockMessage.setText(message);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mLockStyle) {
            startPreferencePanel(mLockStyle.getFragment(),
                    null, mLockStyle.getTitleRes(), null, null, -1);
            return true;
        } else if (preference == mLockTransparent) {
            // unimplemented yet
            //value = mLockTransparent.isChecked();
            //Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_TRANSPARENT,
            //        value ? 1 : 0);
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockMessage) {
            String value = newValue.toString();
            setLockMessageSummaryAndText(value);
            if (value.equals("")) {
                Settings.System.putString(mCr, Settings.System.LOCKSCREEN_MESSAGE,
                        null);
            } else {
                Settings.System.putString(mCr, Settings.System.LOCKSCREEN_MESSAGE,
                        value);
            }
        }
        return false;
    }

    public static class LockStyle extends LockscreenStyle { }
}
