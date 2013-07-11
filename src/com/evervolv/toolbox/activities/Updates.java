package com.evervolv.toolbox.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.updates.UpdatesGapps;
import com.evervolv.toolbox.updates.UpdatesNightly;
import com.evervolv.toolbox.updates.UpdatesRelease;
import com.evervolv.toolbox.updates.UpdatesTesting;

public class Updates extends SettingsFragment {

    private static final String NIGHTLY_UPDATES_PREF = "pref_nightly_updates";
    private static final String RELEASE_UPDATES_PREF = "pref_release_updates";
    private static final String TEST_UPDATES_PREF = "pref_test_updates";
    private static final String GAPPS_UPDATES_PREF = "pref_gapps_updates";

    private PreferenceScreen mPrefSet;
    private PreferenceScreen mNightlyPref;
    private PreferenceScreen mReleasePref;
    private PreferenceScreen mTestPref;
    private PreferenceScreen mGappsPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.updates);

        mPrefSet = getPreferenceScreen();

        mNightlyPref = (PreferenceScreen) mPrefSet.findPreference(
                NIGHTLY_UPDATES_PREF);
        mReleasePref = (PreferenceScreen) mPrefSet.findPreference(
                RELEASE_UPDATES_PREF);
        mTestPref = (PreferenceScreen) mPrefSet.findPreference(
                TEST_UPDATES_PREF);
        mGappsPref = (PreferenceScreen) mPrefSet.findPreference(
                GAPPS_UPDATES_PREF);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mNightlyPref) {
            startPreferencePanel(mNightlyPref.getFragment(),
                    null, mNightlyPref.getTitleRes(), null, null, -1);
            return true;
        } else if (preference == mReleasePref) {
            startPreferencePanel(mReleasePref.getFragment(),
                    null, mReleasePref.getTitleRes(), null, null, -1);
        } else if (preference == mTestPref) {
            startPreferencePanel(mTestPref.getFragment(),
                    null, mTestPref.getTitleRes(), null, null, -1);
        } else if (preference == mGappsPref) {
            startPreferencePanel(mGappsPref.getFragment(),
                    null, mGappsPref.getTitleRes(), null, null, -1);
        }
        return false;
    }

    public static class Nightly extends UpdatesNightly { }
    public static class Release extends UpdatesRelease { }
    public static class Test extends UpdatesTesting { }
    public static class Gapps extends UpdatesGapps { }
}
