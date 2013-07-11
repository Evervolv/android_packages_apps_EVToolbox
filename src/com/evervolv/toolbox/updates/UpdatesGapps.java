package com.evervolv.toolbox.updates;

import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.activities.subactivities.UpdatesFragment;
import com.evervolv.toolbox.updates.db.DatabaseManager;
import com.evervolv.toolbox.updates.services.UpdateManifestService;

public class UpdatesGapps extends UpdatesFragment {

    private static final String AVAILABLE_UPDATES_CAT = "pref_updates_gapps_category_available_updates";

    private PreferenceScreen mPrefSet;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.updates_gapps);

        mPrefSet = getPreferenceScreen();

        mDbType = DatabaseManager.GAPPS;
        mAvailableCategory = (PreferenceCategory) mPrefSet
                .findPreference(AVAILABLE_UPDATES_CAT);
    }

    @Override
    protected String getUpdateCheckAction() {
        return UpdateManifestService.ACTION_UPDATE_CHECK_GAPPS;
    }
}
