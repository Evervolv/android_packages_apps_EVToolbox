package com.evervolv.toolbox;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
    }

    public ContentResolver getContentResolver() {
         return getActivity().getContentResolver();
    }

    public Context getContext() {
        return getActivity().getApplicationContext();
    }
    
    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes,
            CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        PreferenceActivity pa = (PreferenceActivity) this.getActivity();
        pa.startPreferencePanel(fragmentClass, args, titleRes, titleText,
                resultTo, resultRequestCode);
    }
}

