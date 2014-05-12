package com.evervolv.toolbox.fragments;

import android.content.Context;
import android.preference.PreferenceFragment;

public abstract class DeviceSettingFragment extends PreferenceFragment {
    public abstract void restore(Context context, boolean toolboxEnabled);
}
