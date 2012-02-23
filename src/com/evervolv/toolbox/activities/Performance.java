package com.evervolv.toolbox.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.subactivities.MemoryManagement;
import com.evervolv.toolbox.activities.subactivities.Processor;

public class Performance extends SettingsFragment {

    private static final String TAG = "EVToolbox";

    private static final String MEM_MANAGEMENT = "pref_memory_management";
    private static final String PROCESSOR_PREF = "pref_processor";

    private PreferenceScreen mPrefSet;
    private PreferenceScreen mMemoryManagement;
    private PreferenceScreen mProcessor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.performance_settings);

        mPrefSet = getPreferenceScreen();

        mMemoryManagement = (PreferenceScreen) mPrefSet.findPreference(MEM_MANAGEMENT);

        mProcessor = (PreferenceScreen) mPrefSet.findPreference(PROCESSOR_PREF);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mMemoryManagement) {
            startPreferencePanel(mMemoryManagement.getFragment(),
                    null, mMemoryManagement.getTitleRes(), null, null, -1);
            return true;
        } else if (preference == mProcessor) {
            startPreferencePanel(mProcessor.getFragment(),
                    null, mProcessor.getTitleRes(), null, null, -1);
            return true;
        }
        return false;
    }

    public static class MemMgr extends MemoryManagement { }
    public static class Cpu extends Processor { }

}
