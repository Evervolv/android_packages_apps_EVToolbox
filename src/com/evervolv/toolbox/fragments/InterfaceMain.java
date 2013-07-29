package com.evervolv.toolbox.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.custom.NumberPickerPreference;
import com.evervolv.toolbox.misc.CMDProcessor;
import com.evervolv.toolbox.misc.FileUtil;

public class InterfaceMain extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EVToolbox";

    private static final String DENSITY_PICKER_PREF = "pref_interface_density_picker";
    private static final String TRACKBALL_WAKE_TOGGLE = "pref_trackball_wake_toggle";
    private static final String VOLUME_WAKE_TOGGLE = "pref_volume_wake_toggle";
    private static final String LOCKSCREEN_MUSIC_CTRL_VOLBTN = "pref_lockscreen_music_controls_volbtn";

    private static final int MIN_DENSITY_VALUE = 100;
    private static final int MAX_DENSITY_VALUE = 400; // This may need to change for future devices

    private static final int DIALOG_DENSITY_CHANGE_REQ = 0;
    private static final int DIALOG_REBOOT_REQ = 1;

    private CheckBoxPreference mTrackballWake;
    private CheckBoxPreference mVolumeWake;
    private CheckBoxPreference mMusicCtrlVolBtn;
    private NumberPickerPreference mDensityPicker;
    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private int mRecommendedDpi;
    private int mDefaultDpi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_main);

        mPrefSet = getPreferenceScreen();
        mCr = getActivity().getContentResolver();

        /* Trackball wake pref */
        mTrackballWake = (CheckBoxPreference) mPrefSet.findPreference(
                TRACKBALL_WAKE_TOGGLE);
        mTrackballWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.TRACKBALL_WAKE_SCREEN, 1) == 1);

        /* Remove mTrackballWake on devices without trackballs */
        if (!getResources().getBoolean(R.bool.has_trackball)) {
            mPrefSet.removePreference(mTrackballWake);
        }

        /* Volume wake pref */
        mVolumeWake = (CheckBoxPreference) mPrefSet.findPreference(VOLUME_WAKE_TOGGLE);
        mVolumeWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        /* Volume button music controls */
        mMusicCtrlVolBtn = (CheckBoxPreference) mPrefSet.findPreference(LOCKSCREEN_MUSIC_CTRL_VOLBTN);
        mMusicCtrlVolBtn.setChecked(Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN, 1) == 1);

        /* Density picker */
        int currentDensity = Integer.valueOf(SystemProperties.get("ro.sf.lcd_density"));
        mRecommendedDpi = Integer.valueOf(getActivity().getApplicationContext().getString(
                R.string.config_recommendedTabletDpi));
        mDefaultDpi = Integer.valueOf(getActivity().getApplicationContext().getString(
                R.string.config_defaultDpi));
        mDensityPicker = (NumberPickerPreference) mPrefSet.findPreference(DENSITY_PICKER_PREF);
        mDensityPicker.setOnPreferenceChangeListener(this);
        mDensityPicker.setSummary(String.format(getActivity().getApplicationContext().getString(
                R.string.pref_interface_density_picker_summary,
                currentDensity, mRecommendedDpi, mDefaultDpi)));
        mDensityPicker.setMinValue(MIN_DENSITY_VALUE);
        mDensityPicker.setMaxValue(MAX_DENSITY_VALUE);
        mDensityPicker.setCurrentValue(currentDensity);
    }

    @Override
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
            return true;
        } else if (preference == mMusicCtrlVolBtn) {
            value = mMusicCtrlVolBtn.isChecked();
            Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN,
                    value ? 1 : 0);
            return true;
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
            mDensityPicker.setSummary(String.format(getActivity().getApplicationContext().getString(
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
//                        mTabletMode.setChecked(!mTabletMode.isChecked());
                        dialog.dismiss();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                        getString(R.string.reboot),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PowerManager power = (PowerManager) getActivity().getApplicationContext()
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
                        PowerManager power = (PowerManager) getActivity().getApplicationContext()
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

}
