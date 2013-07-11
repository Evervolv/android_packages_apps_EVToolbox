package com.evervolv.toolbox.updates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.activities.subactivities.UpdatesFragment;
import com.evervolv.toolbox.updates.db.DatabaseManager;
import com.evervolv.toolbox.updates.misc.Constants;
import com.evervolv.toolbox.updates.services.UpdateManifestService;

public class UpdatesNightly extends UpdatesFragment implements OnPreferenceChangeListener {

    private static final String AVAILABLE_UPDATES_CAT = "pref_updates_nightly_category_available_updates";

    private ListPreference mCheckUpdates;
    private PreferenceScreen mPrefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.updates_nightlies);

        mPrefSet = getPreferenceScreen();

        mCheckUpdates = (ListPreference) mPrefSet
                .findPreference(Constants.PREF_UPDATE_SCHEDULE_NIGHTLY);
        mCheckUpdates.setSummary(mCheckUpdates.getEntry());
        mCheckUpdates.setOnPreferenceChangeListener(this);

        mDbType = DatabaseManager.NIGHTLIES;
        mAvailableCategory = (PreferenceCategory) mPrefSet
                .findPreference(AVAILABLE_UPDATES_CAT);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCheckUpdates) {
            int value = Integer.valueOf((String) newValue);
            mCheckUpdates.setSummary(getFriendlyUpdateInterval(value));
            SharedPreferences sPrefs = getActivity().getSharedPreferences(Constants.APP_NAME, Context.MODE_MULTI_PROCESS);
            sPrefs.edit().putInt(Constants.PREF_UPDATE_SCHEDULE_NIGHTLY,
                    value).apply();
            startUpdateManifestService(true);
            return true;
        }
        return false;
    }

    @Override
    protected String getUpdateCheckAction() {
        return UpdateManifestService.ACTION_UPDATE_CHECK_NIGHTLY;
    }

}
