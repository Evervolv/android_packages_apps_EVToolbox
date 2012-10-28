package com.evervolv.toolbox.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.subactivities.InterfacePowerMenu;
import com.evervolv.toolbox.activities.subactivities.InterfaceRotation;
import com.evervolv.toolbox.utils.CMDProcessor;
import com.evervolv.toolbox.utils.FileUtil;
import com.evervolv.toolbox.utils.NumberPickerPreference;

public class Interface extends SettingsFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EVToolbox";

    private static final String BUTTONS_CATEGORY = "pref_interface_category_buttons";
    private static final String TABLETDPI_CATEGORY = "pref_interface_category_tabletdpi";

    private static final String ROTATION_PREF = "pref_interface_rotation";
    private static final String POWER_MENU_PREF = "pref_interface_power_menu";
    private static final String SENSE4_RECENT_APPS_PREF = "pref_interface_sense4_recent_apps";
    private static final String TABLET_MODE_PREF = "pref_interface_tablet_mode";
    private static final String DENSITY_PICKER_PREF = "pref_interface_density_picker";
    private static final String TRACKBALL_WAKE_TOGGLE = "pref_trackball_wake_toggle";
    private static final String VOLUME_WAKE_TOGGLE = "pref_volume_wake_toggle";
    private static final String LOCKSCREEN_MUSIC_CTRL_VOLBTN = "pref_lockscreen_music_controls_volbtn";
    private static final String KILL_APP_LONGPRESS_BACK = "pref_kill_app_longpress_back";

    private static final int MIN_DENSITY_VALUE = 100;
    private static final int MAX_DENSITY_VALUE = 400; // This may need to change for future devices

    private static final int DIALOG_DENSITY_CHANGE_REQ = 0;
    private static final int DIALOG_REBOOT_REQ = 1;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private PreferenceScreen mRotation;
    private PreferenceScreen mPowerMenu;
    private CheckBoxPreference mSense4RecentApps;
    private CheckBoxPreference mTabletMode;
    private NumberPickerPreference mDensityPicker;
    private CheckBoxPreference mTrackballWake;
    private CheckBoxPreference mVolumeWake;
    private CheckBoxPreference mMusicCtrlVolBtn;
    private CheckBoxPreference mKillAppBackBtn;
    private PreferenceCategory mButtons;
    private PreferenceCategory mTabletDpi;

    private int mRecommendedDpi;
    private int mDefaultDpi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_settings);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        
        //categories
        mButtons = (PreferenceCategory) mPrefSet.findPreference(BUTTONS_CATEGORY);
        mTabletDpi = (PreferenceCategory) mPrefSet.findPreference(TABLETDPI_CATEGORY);
        //preferences
        mRotation = (PreferenceScreen) mPrefSet.findPreference(ROTATION_PREF);
        mPowerMenu = (PreferenceScreen) mPrefSet.findPreference(POWER_MENU_PREF);
        mSense4RecentApps = (CheckBoxPreference) mPrefSet.findPreference(SENSE4_RECENT_APPS_PREF);
        mTabletMode = (CheckBoxPreference) mPrefSet.findPreference(TABLET_MODE_PREF);

        /* Trackball wake pref */
        mTrackballWake = (CheckBoxPreference) mPrefSet.findPreference(
                TRACKBALL_WAKE_TOGGLE);
        mTrackballWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.TRACKBALL_WAKE_SCREEN, 1) == 1);

        /* Remove mTrackballWake on devices without trackballs */
        if (!getResources().getBoolean(R.bool.has_trackball)) {
            mButtons.removePreference(mTrackballWake);
        }
        
        /* Volume wake pref */
        mVolumeWake = (CheckBoxPreference) mPrefSet.findPreference(VOLUME_WAKE_TOGGLE);
        mVolumeWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        /* Volume button music controls */
        mMusicCtrlVolBtn = (CheckBoxPreference) mPrefSet.findPreference(LOCKSCREEN_MUSIC_CTRL_VOLBTN);
        mMusicCtrlVolBtn.setChecked(Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN, 1) == 1);

        /* Kill app long press pack */
        //mKillAppBackBtn = (CheckBoxPreference) mPrefSet.findPreference(KILL_APP_LONGPRESS_BACK);
        //mKillAppBackBtn.setChecked(Settings.Secure.getInt(mCr,
        //        Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) == 1);
        
        int currentDensity = Integer.valueOf(SystemProperties.get("ro.sf.lcd_density"));
        mRecommendedDpi = Integer.valueOf(getContext().getString(
                R.string.config_recommendedTabletDpi));
        mDefaultDpi = Integer.valueOf(getContext().getString(
                R.string.config_defaultDpi));
        mDensityPicker = (NumberPickerPreference) mPrefSet.findPreference(DENSITY_PICKER_PREF);
        mDensityPicker.setOnPreferenceChangeListener(this);
        mDensityPicker.setSummary(String.format(getContext().getString(
                R.string.pref_interface_density_picker_summary,
                currentDensity, mRecommendedDpi, mDefaultDpi)));
        mDensityPicker.setMinValue(MIN_DENSITY_VALUE);
        mDensityPicker.setMaxValue(MAX_DENSITY_VALUE);
        mDensityPicker.setCurrentValue(currentDensity);

        if (getResources().getBoolean(R.bool.config_disableTabletForce)) {
            mTabletDpi.removePreference(mTabletMode);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mButtons) {
            startPreferencePanel(mButtons.getFragment(),
                    null, mButtons.getTitleRes(), null, null, -1);
            return true;
        } else if (preference == mRotation) {
            startPreferencePanel(mRotation.getFragment(),
                    null, mRotation.getTitleRes(), null, null, -1);
            return true;
        } else if (preference == mPowerMenu) {
            startPreferencePanel(mPowerMenu.getFragment(),
                    null, mPowerMenu.getTitleRes(), null, null, -1);
            return true;
        } else if (preference == mSense4RecentApps) {
            value = mSense4RecentApps.isChecked();
            Settings.System.putInt(mCr, Settings.System.SENSE4_RECENT_APPS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mTabletMode) {
            value = mTabletMode.isChecked();
            Settings.System.putInt(mCr, Settings.System.TABLET_MODE,
                    value ? 1:0);
            if (value) {
                if (mDensityPicker.getValue() != mRecommendedDpi) {
                    showDialog(DIALOG_DENSITY_CHANGE_REQ);
                } else {
                    showDialog(DIALOG_REBOOT_REQ);
                }
            } else {
                if (mDensityPicker.getValue() != mDefaultDpi) {
                    showDialog(DIALOG_DENSITY_CHANGE_REQ);
                } else {
                    showDialog(DIALOG_REBOOT_REQ);
                }
            }
            return true;
        } else if (preference == mTrackballWake) {
            value = mTrackballWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.TRACKBALL_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mVolumeWake) {
            value = mVolumeWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.VOLUME_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mMusicCtrlVolBtn) {
            value = mMusicCtrlVolBtn.isChecked();
            Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN,
                    value ? 1 : 0);
            return true;
        //} else if (preference == mKillAppBackBtn) {
        //    value = mKillAppBackBtn.isChecked();
        //    Settings.Secure.putInt(mCr, Settings.Secure.KILL_APP_LONGPRESS_BACK,
        //            value ? 1 : 0);
        //    return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDensityPicker) {
            int value = Integer.parseInt(newValue.toString());
            FileUtil.getMount("rw");
            new CMDProcessor().su.runWaitFor("busybox sed -i 's|ro.sf.lcd_density=.*|"
                    + "ro.sf.lcd_density" + "=" + value + "|' " + "/system/build.prop");
            FileUtil.getMount("ro");
            mDensityPicker.setSummary(String.format(getContext().getString(
                    R.string.pref_interface_density_picker_summary,
                    value, mRecommendedDpi, mDefaultDpi)));
            showDialog(DIALOG_REBOOT_REQ);
        }
        return false;
    }

    public void showDialog(int dialogId) {
        AlertDialog dialog = new AlertDialog.Builder(
                getActivity()).create();
        switch (dialogId) {
            case DIALOG_DENSITY_CHANGE_REQ:
                dialog.setTitle(R.string
                        .pref_interface_density_change_recommended_title);
                dialog.setMessage(getString(R.string
                        .pref_interface_density_change_recommended_text));
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTabletMode.setChecked(!mTabletMode.isChecked());
                        dialog.dismiss();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                        getString(R.string.reboot),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PowerManager power = (PowerManager) getContext()
                                .getSystemService(Context.POWER_SERVICE);
                        power.reboot("UI Change");
                    }
                });
                dialog.show();
                break;
            case DIALOG_REBOOT_REQ:
                dialog.setTitle(R.string
                        .pref_interface_reboot_required_title);
                dialog.setMessage(getString(R.string
                        .pref_interface_reboot_required_text));
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(R.string.reboot),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PowerManager power = (PowerManager) getContext()
                                .getSystemService(Context.POWER_SERVICE);
                        power.reboot("UI Change");
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.later),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
        }
    }

    public static class Rotation extends InterfaceRotation { }
    public static class PowerMenu extends InterfacePowerMenu { }
}
