package com.evervolv.toolbox.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.custom.GalleryPickerPreference;

public class StatusbarMain extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUSBAR_SIXBAR_SIGNAL = "pref_statusbar_sixbar_signal";
    private static final String STATUSBAR_BATTERY_STYLE = "pref_statusbar_batt_style";

    private static final int BATT_STYLE_DEFAULT = 1;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mUseSixbaricons;
    private ListPreference mBattStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_main);

        mCr = getActivity().getContentResolver();
        mPrefSet = getPreferenceScreen();

        /* Sixbar Signal Icons */
        mUseSixbaricons = (CheckBoxPreference) mPrefSet.findPreference(
                STATUSBAR_SIXBAR_SIGNAL);
        mUseSixbaricons.setChecked(Settings.System.getInt(mCr,
                Settings.System.STATUSBAR_6BAR_SIGNAL, 1) == 1);

        /* Remove mUseSixbaricons on devices without mobile radios */
        if (!getResources().getBoolean(R.bool.config_has_mobile_radio)) {
            mPrefSet.removePreference(mUseSixbaricons);

        }

        /* Battery Icon Style */
        mBattStyle = (ListPreference) mPrefSet.findPreference(STATUSBAR_BATTERY_STYLE);
        mBattStyle.setValue(Integer.toString(Settings.System.getInt(mCr,
                Settings.System.STATUSBAR_BATT_STYLE, BATT_STYLE_DEFAULT)));
        mBattStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBattStyle) {
            Settings.System.putInt(mCr, Settings.System.STATUSBAR_BATT_STYLE,
                    Integer.valueOf((String) newValue));
            return true;
        }
        return false;
    }

    @Override
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
