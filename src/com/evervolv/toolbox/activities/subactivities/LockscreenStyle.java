package com.evervolv.toolbox.activities.subactivities;

import java.net.URISyntaxException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.evervolv.toolbox.activities.Lockscreen;
import com.evervolv.toolbox.utils.ShortcutPickHelper;

public class LockscreenStyle extends SettingsFragment implements
        OnPreferenceChangeListener, ShortcutPickHelper.OnPickListener {

    private static final String TAG = "EVToolbox";

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";
    private static final String LOCKSCREEN_STYLE_MULITWAVEVIEW_3WAY_TOGGLE = "pref_lockscreen_style_multiwaveview_3way_toggle";
    private static final String LOCKSCREEN_STYLE_MULTIWAVEVIEW_SILENTMODE_TOGGLE = "pref_lockscreen_style_multiwaveview_silentmode_toggle";
    private static final String LOCKSCREEN_CUSTOM_APP_ONE = "pref_lockscreen_custom_app_one";
    private static final String LOCKSCREEN_CUSTOM_APP_TWO = "pref_lockscreen_custom_app_two";
    private static final String LOCKSCREEN_CUSTOM_APP_THREE = "pref_lockscreen_custom_app_three";

    private static final String CATEGORY_ICS = "pref_category_ics_style";
    private static final String CATEGORY_CUSTOM_APP_ONE = "pref_lockscreen_category_custom_app_one";
    private static final String CATEGORY_CUSTOM_APP_TWO = "pref_lockscreen_category_custom_app_two";
    private static final String CATEGORY_CUSTOM_APP_THREE = "pref_lockscreen_category_custom_app_three";

    private static final int LOCK_STYLE_GB   = 1;
    private static final int LOCK_STYLE_ECLAIR = 2;
    private static final int LOCK_STYLE_ICS = 3;

    private ListPreference mLockscreenStyle;
    private CheckBoxPreference mLockscreenStyleIcs3way;
    private PreferenceCategory mCatIcs;
    private ShortcutPickHelper mPicker;
    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private PreferenceCategory mCatAppOne;
    private PreferenceCategory mCatAppTwo;
    private PreferenceCategory mCatAppThree;
    private ListPreference[] mCustApp;
    private int mWhichApp = -1;
    
    private int mMaxCustomApps = Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES.length;
    
    private int mCurrLockscreen = Settings.System.getInt(mCr,
            Settings.System.LOCKSCREEN_STYLE , 3);
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_style);
        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        mCatIcs = (PreferenceCategory) mPrefSet.findPreference(CATEGORY_ICS);


        /* Lockscreen style */
        mLockscreenStyle = (ListPreference) mPrefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        mLockscreenStyle.setOnPreferenceChangeListener(this);

        mLockscreenStyleIcs3way = (CheckBoxPreference) mPrefSet
                .findPreference(LOCKSCREEN_STYLE_MULITWAVEVIEW_3WAY_TOGGLE);
        mLockscreenStyleIcs3way.setChecked(Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_STYLE_MULITWAVEVIEW_3WAY, 1) == 1);
        
        mCatAppOne = (PreferenceCategory) mPrefSet.findPreference(
                CATEGORY_CUSTOM_APP_ONE);
        mCatAppTwo = (PreferenceCategory) mPrefSet.findPreference(
                CATEGORY_CUSTOM_APP_TWO);
        mCatAppThree = (PreferenceCategory) mPrefSet.findPreference(
                CATEGORY_CUSTOM_APP_THREE);
        
        mCustApp = new ListPreference[3];
        mCustApp[0] = (ListPreference) mPrefSet.findPreference(
                LOCKSCREEN_CUSTOM_APP_ONE);
        mCustApp[0].setOnPreferenceChangeListener(this);
        mCustApp[0].setLayoutResource(R.layout.app_preference);
        
        mCustApp[1] = (ListPreference) mPrefSet.findPreference(
                LOCKSCREEN_CUSTOM_APP_TWO);
        mCustApp[1].setOnPreferenceChangeListener(this);
        mCustApp[1].setLayoutResource(R.layout.app_preference);
        
        mCustApp[2] = (ListPreference) mPrefSet.findPreference(
                LOCKSCREEN_CUSTOM_APP_THREE);
        mCustApp[2].setOnPreferenceChangeListener(this);
        mCustApp[2].setLayoutResource(R.layout.app_preference);
        
        if (mCurrLockscreen != 3) {
            mPrefSet.removePreference(mCatIcs);
            mPrefSet.removePreference(mCatAppOne);
            mPrefSet.removePreference(mCatAppTwo);
            mPrefSet.removePreference(mCatAppThree);
        }

        if (!mLockscreenStyleIcs3way.isChecked()) {
            mPrefSet.removePreference(mCatAppTwo);
        } else {
            mPrefSet.addPreference(mCatAppTwo);
        }
        
        mPicker = new ShortcutPickHelper(this, this);
        setCustomAppSummaries();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenStyle) {
            Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_STYLE,
                    Integer.valueOf((String) newValue));
            mCurrLockscreen = Integer.valueOf((String) newValue);
            if (mCurrLockscreen != 3) {
                mPrefSet.removePreference(mCatIcs);
                mPrefSet.removePreference(mCatAppOne);
                mPrefSet.removePreference(mCatAppTwo);
                mPrefSet.removePreference(mCatAppThree);
            } else {
                mPrefSet.addPreference(mCatIcs);
                mPrefSet.addPreference(mCatAppOne);
                mPrefSet.addPreference(mCatAppTwo);
                mPrefSet.addPreference(mCatAppThree);
            }
            return true;
        } else if (preference == mCustApp[0]) {
            mWhichApp = 0;
            if (newValue.equals("1")) {
                mPicker.pickShortcut();
            } else if (newValue.equals("2")) {
                shortcutPicked("**unlock**", null, false);
            } else if (newValue.equals("3")) {
                shortcutPicked("**sound**", null, false);
            }
            return true;
        } else if (preference == mCustApp[1]) {
            mWhichApp = 1;
            if (newValue.equals("1")) {
                mPicker.pickShortcut();
            } else if (newValue.equals("2")) {
                shortcutPicked("**unlock**", null, false);
            } else if (newValue.equals("3")) {
                shortcutPicked("**sound**", null, false);
            }
            return true;
        } else if (preference == mCustApp[2]) {
            mWhichApp = 2;
            if (newValue.equals("1")) {
                mPicker.pickShortcut();
            } else if (newValue.equals("2")) {
                shortcutPicked("**unlock**", null, false);
            } else if (newValue.equals("3")) {
                shortcutPicked("**sound**", null, false);
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
            if (!value) {
                mPrefSet.removePreference(mCatAppTwo);
            } else {
                mPrefSet.addPreference(mCatAppTwo);
            }
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
                Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[mWhichApp], uri);
        setCustomAppSummaries();
    }

    public void setCustomAppSummaries() {
        PackageManager pm = getContext().getPackageManager();
        for (int i = 0; i < mMaxCustomApps; i++) {
            String uri = Settings.System.getString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[i]);
            Log.d(TAG, "uri: " + uri);
            if (uri != null) {
                if (uri.equals("**unlock**")) {
                    mCustApp[i].setSummary(R.string
                            .pref_lockscreen_style_custom_app_unlock);
                    mCustApp[i].setIcon(R.drawable.lockscreen_unlock);
                } else if (uri.equals("**sound**")) {
                    mCustApp[i].setSummary(R.string
                            .pref_lockscreen_style_custom_app_sound);
                    mCustApp[i].setIcon(R.drawable.lockscreen_sound);
                } else {
                    mCustApp[i].setSummary(mPicker.getFriendlyNameForUri(uri));
                    try {
                        Intent intent = Intent.parseUri(uri, 0);
                        mCustApp[i].setIcon(pm.getActivityIcon(intent));
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "NameNotFoundException: [" + uri + "]");
                    } catch (URISyntaxException e) {
                        Log.e(TAG, "URISyntaxException: [" + uri + "]");
                    }
                }
            } else {
                mCustApp[i].setIcon(R.drawable.lockscreen_arrow);
                mCustApp[i].setSummary(R.string
                        .pref_lockscreen_style_custom_app_pick);
            }
        }
    }

}
