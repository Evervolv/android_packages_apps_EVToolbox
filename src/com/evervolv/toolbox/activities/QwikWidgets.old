package com.evervolv.toolbox.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Switch;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.utils.QwikWidgetsEnabler;
import com.evervolv.toolbox.utils.QwikWidgetsUtil;
import com.evervolv.toolbox.utils.WidgetManagerDialog;

public class QwikWidgets extends SettingsFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EVToolbox";

    private static final String NOTIFICATION_DROPDOWN = "pref_notification_dropdown";
    private static final String QWIK_WIDGETS_MANAGE_WIDGETS = "pref_qwik_widgets_manage_widgets";
    private static final String QWIK_WIDGETS_MAX_PER_LINE = "pref_qwik_widgets_max_per_line";
    private static final int MAX_WIDGETS_DEFAULT = 3;

    private Preference mManageWidgets;
    private WidgetManagerDialog mWidgetDialog;
    private CheckBoxPreference mNotifDropdown;
    private ListPreference mMaxWidgets;
    private QwikWidgetsEnabler mQwikWidgetsEnabler;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.qwik_widgets);

        final Activity activity = getActivity();

        Switch actionBarSwitch = new Switch(activity);

        if (activity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
            if (!preferenceActivity.onIsMultiPane()) {
                actionBarSwitch.setPadding(0, 0, 16, 0);
                activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getActionBar().setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.RIGHT));
            }
        }

        mQwikWidgetsEnabler = new QwikWidgetsEnabler(activity, actionBarSwitch);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        mNotifDropdown = (CheckBoxPreference) mPrefSet.findPreference(NOTIFICATION_DROPDOWN);
        mNotifDropdown.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_DROPDOWN_VIEW, 0) == 1);

        mMaxWidgets = (ListPreference) mPrefSet
                .findPreference(QWIK_WIDGETS_MAX_PER_LINE);
        mMaxWidgets.setValue(Integer.toString(Settings.System.getInt(mCr,
                Settings.System.MAX_WIDGETS_PER_LINE, MAX_WIDGETS_DEFAULT)));
        mMaxWidgets.setOnPreferenceChangeListener(this);

        mManageWidgets = (Preference) mPrefSet
                .findPreference(QWIK_WIDGETS_MANAGE_WIDGETS);
    }


    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mNotifDropdown) {
            value = mNotifDropdown.isChecked();
            Settings.System.putInt(mCr, Settings.System.NOTIFICATION_DROPDOWN_VIEW,
                    value ? 1 : 0);
            return true;
        } else if (preference == mManageWidgets) {

            mWidgetDialog = new WidgetManagerDialog(preferenceScreen.getContext());
            mWidgetDialog.setTitle(R.string.title_qwik_widgets_manage_widgets);
            mWidgetDialog.show();
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mMaxWidgets) {
            Settings.System.putInt(mCr, Settings.System.MAX_WIDGETS_PER_LINE,
                    Integer.valueOf((String) newValue));
            return true;
        }
        return false;
    }
}
