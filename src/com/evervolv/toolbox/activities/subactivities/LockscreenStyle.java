package com.evervolv.toolbox.activities.subactivities;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.utils.ShortcutPickHelper;

public class LockscreenStyle extends SettingsFragment implements
        OnPreferenceChangeListener, ShortcutPickHelper.OnPickListener {

    private static final String TAG = "EVToolbox";

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";
    private static final String LOCKSCREEN_STYLE_MULITWAVEVIEW_3WAY_TOGGLE = "pref_lockscreen_style_multiwaveview_3way_toggle";
    private static final String LOCKSCREEN_STYLE_MULTIWAVEVIEW_SILENTMODE_TOGGLE = "pref_lockscreen_style_multiwaveview_silentmode_toggle";
    private static final String LOCKSCREEN_CUSTOM_APP = "pref_lockscreen_custom_app";

    private static final String CATEGORY_ICS = "pref_category_ics_style";

    private static final int LOCK_STYLE_GB   = 1;
    private static final int LOCK_STYLE_ECLAIR = 2;
    private static final int LOCK_STYLE_ICS = 3;

    private ListPreference mLockscreenStyle;
    private CheckBoxPreference mLockscreenStyleIcs3way;
    private CheckBoxPreference mLockscreenStyleIcsSilentToggle;
    private Preference mCustomAppPref;
    private PreferenceCategory mCatIcs;
    private ShortcutPickHelper mPicker;
    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private int mCurrLockscreen = Settings.System.getInt(mCr,
            Settings.System.LOCKSCREEN_STYLE , 3);
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        addPreferencesFromResource(R.xml.lockscreen_style);
        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        mCatIcs= (PreferenceCategory) mPrefSet.findPreference(CATEGORY_ICS);


        /* Lockscreen style */
        mLockscreenStyle = (ListPreference) mPrefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        mLockscreenStyle.setOnPreferenceChangeListener(this);

        mLockscreenStyleIcs3way = (CheckBoxPreference) mPrefSet
                .findPreference(LOCKSCREEN_STYLE_MULITWAVEVIEW_3WAY_TOGGLE);
        mLockscreenStyleIcs3way.setChecked(Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_STYLE_MULITWAVEVIEW_3WAY, 1) == 1);

        mLockscreenStyleIcsSilentToggle = (CheckBoxPreference) mPrefSet
                .findPreference(LOCKSCREEN_STYLE_MULTIWAVEVIEW_SILENTMODE_TOGGLE);
        mLockscreenStyleIcsSilentToggle.setChecked(Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_STYLE_MULTIWAVEVIEW_SILENTMODE, 1) == 1);

        mCustomAppPref = mPrefSet
                .findPreference(LOCKSCREEN_CUSTOM_APP);
        if (mCurrLockscreen != 3) {
            mPrefSet.removePreference(mCatIcs);
        }

        //removing this until we use it!
        mCatIcs.removePreference(mLockscreenStyleIcsSilentToggle);

        mPicker = new ShortcutPickHelper(this, this);
        setCustomAppSummary();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenStyle) {
            Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_STYLE,
                    Integer.valueOf((String) newValue));
            mCurrLockscreen = Integer.valueOf((String) newValue);
            Log.d("LOCK", "Lockscren=" + mCurrLockscreen);
            if (mCurrLockscreen != 3) {
                mPrefSet.removePreference(mCatIcs);
            } else {
                mPrefSet.addPreference(mCatIcs);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mLockscreenStyleIcs3way) {
            value = mLockscreenStyleIcs3way.isChecked();
            Settings.System.putInt(mCr,
                    Settings.System.LOCKSCREEN_STYLE_MULITWAVEVIEW_3WAY, value ? 1 : 0);
            return true;
        } else if (preference == mLockscreenStyleIcsSilentToggle) {
            value = mLockscreenStyleIcsSilentToggle.isChecked();
            Settings.System.putInt(mCr,
                    Settings.System.LOCKSCREEN_STYLE_MULTIWAVEVIEW_SILENTMODE, value ? 1 : 0);
            return true;
        } else if (preference == mCustomAppPref) {
            mPicker.pickShortcut();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName,
            boolean isApplication) {
        Settings.System.putString(mCr,
                Settings.System.LOCKSCREEN_STYLE_MULTIWAVEVIEW_CUSTOMAPP, uri);
        setCustomAppSummary();
    }

    public void setCustomAppSummary() {
        String uri = Settings.System.getString(mCr, Settings.System.
                LOCKSCREEN_STYLE_MULTIWAVEVIEW_CUSTOMAPP);

        if (uri != null) {
            mCustomAppPref.setSummary(mPicker.getFriendlyNameForUri(uri));
        }
    }

}
