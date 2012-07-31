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
import android.view.Gravity;
import android.widget.Toast;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.Lockscreen;
import com.evervolv.toolbox.utils.GalleryPickerPreference;
//import com.evervolv.toolbox.utils.ShortcutPickHelper;

public class LockscreenStyle extends SettingsFragment implements
        OnPreferenceChangeListener {
    //, ShortcutPickHelper.OnPickListener
    private static final String TAG = "EVToolbox";

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_picker";
    private static final String LOCKSCREEN_CUSTOM_APP_ONE = "pref_lockscreen_custom_app_one";
    private static final String LOCKSCREEN_CUSTOM_APP_TWO = "pref_lockscreen_custom_app_two";
    private static final String LOCKSCREEN_CUSTOM_APP_THREE = "pref_lockscreen_custom_app_three";

    private static final String CATEGORY_ICS = "pref_category_ics_style";
    private static final String CATEGORY_CUSTOM_APP_ONE = "pref_lockscreen_category_custom_app_one";
    private static final String CATEGORY_CUSTOM_APP_TWO = "pref_lockscreen_category_custom_app_two";
    private static final String CATEGORY_CUSTOM_APP_THREE = "pref_lockscreen_category_custom_app_three";

    private static final int LOCK_STYLE_JB = 0;
    private static final int LOCK_STYLE_ICS = 1;
    private static final int LOCK_STYLE_HC = 2;
    private static final int LOCK_STYLE_GB   = 3;
    private static final int LOCK_STYLE_ECLAIR = 4;

    private static final int LOCK_STYLE_DEFAULT = LOCK_STYLE_JB;

    private GalleryPickerPreference mLockscreenStyle;
    private PreferenceCategory mCatIcs;
    //private ShortcutPickHelper mPicker;
    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private PreferenceCategory mCatAppOne;
    private PreferenceCategory mCatAppTwo;
    private PreferenceCategory mCatAppThree;
    private ListPreference[] mCustApp;
    private int mWhichApp = -1;
    
    private int mMaxCustomApps;
    
    private int mCurrLockscreen;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_style);
        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        mCatIcs = (PreferenceCategory) mPrefSet.findPreference(CATEGORY_ICS);

        mCurrLockscreen = Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_STYLE , LOCK_STYLE_DEFAULT);
        //mMaxCustomApps = Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES.length;

        /* Lockscreen style */
        String position = Settings.System.getString(mCr,
                Settings.System.LOCKSCREEN_STYLE);
        mLockscreenStyle = (GalleryPickerPreference) mPrefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        mLockscreenStyle.setCurrPos(position == null ? 0 : Integer.valueOf(position));
        mLockscreenStyle.setSharedPrefs(mPrefSet.getSharedPreferences());
        mLockscreenStyle.setOnPreferenceChangeListener(this);

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
/*
        if (mCurrLockscreen != LOCK_STYLE_ICS) {
            mPrefSet.removePreference(mCatIcs);
            mPrefSet.removePreference(mCatAppOne);
            mPrefSet.removePreference(mCatAppTwo);
            mPrefSet.removePreference(mCatAppThree);
        }

        if (mCurrLockscreen == LOCK_STYLE_ICS_3WAY) {
            mPrefSet.addPreference(mCatAppTwo);
        } else {
            mPrefSet.removePreference(mCatAppTwo);
        }
*/
        // Remove once customizable targets are added.
        mPrefSet.removePreference(mCatIcs);
        mPrefSet.removePreference(mCatAppOne);
        mPrefSet.removePreference(mCatAppTwo);
        mPrefSet.removePreference(mCatAppThree);
        
        //mPicker = new ShortcutPickHelper(this, this);
        //setCustomAppSummaries();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenStyle) {
            int value = Integer.parseInt((String) newValue);
            switch (value) {
                //case LOCK_STYLE_ICS:
                //    mPrefSet.removePreference(mCatAppTwo);
                //    mPrefSet.addPreference(mCatIcs);
                //    mPrefSet.addPreference(mCatAppOne);
                //    mPrefSet.addPreference(mCatAppThree);
                //    break;
                case LOCK_STYLE_ECLAIR:
                    if (!getResources().getBoolean(R.bool.config_allow_eclair_lock)) {
                        Toast toast = Toast.makeText(getContext(), R.string
                                .pref_lockscreen_style_no_eclair_toast,
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return true;
                    }
                case LOCK_STYLE_HC:
                case LOCK_STYLE_GB:
                default:
                    //mPrefSet.removePreference(mCatIcs);
                    //mPrefSet.removePreference(mCatAppOne);
                    //mPrefSet.removePreference(mCatAppTwo);
                    //mPrefSet.removePreference(mCatAppThree);
            }
            Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_STYLE, value);
            mCurrLockscreen = value;
            return true;
        //} else if (preference == mCustApp[0]) {
        //    return processPick(0, newValue);
        //} else if (preference == mCustApp[1]) {
        //    return processPick(1, newValue);
        //} else if (preference == mCustApp[2]) {
        //    return processPick(2, newValue);
        }
        return false;
    }
/*
    private boolean processPick(int index, Object value) {
        mWhichApp = index;
        Toast toast = Toast.makeText(getContext(), R.string
                .pref_lockscreen_category_custom_app_unlock_toast,Toast.LENGTH_LONG);
        if (value.equals("1")) {
            if (checkForUnlock(index)) {
                mPicker.pickShortcut();
            } else {
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                return false;
            }
        } else if (value.equals("2")) {
            shortcutPicked("**unlock**", null, false);
        } else if (value.equals("3")) {
            if (checkForUnlock(index)) {
                shortcutPicked("**sound**", null, false);
            } else {
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                return false;
            }
        }
        return true;
    }

    private boolean checkForUnlock(int currIndex) {
        for (int i = 0; i < mMaxCustomApps; i++) {
            String uri = Settings.System.getString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[i]);
            if (uri == null) uri = getDefaultUri(currIndex);
            if (i == currIndex) continue;
            if (uri.equals("**unlock**")) return true;
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

    private String getDefaultUri(int index) {
        switch (index) {
            case 0:
                return "**unlock**";
            case 1:
                return "**sound**";
            case 2:
                return getString(com.android.internal.R.string
                        .lockscreen_custom_app_default);
        }
        return null;
    }
*/
}
