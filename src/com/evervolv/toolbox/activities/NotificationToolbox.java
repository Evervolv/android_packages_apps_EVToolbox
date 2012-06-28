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
import com.evervolv.toolbox.utils.ToolboxEnabler;
import com.evervolv.toolbox.utils.WidgetManagerDialog;

public class NotificationToolbox extends SettingsFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EVToolbox";

    private static final String NOTIFICATION_TOOLBOX_DROPDOWN = "pref_notification_toolbox_dropdown";
    private static final String NOTIFICATION_TOOLBOX_WIDGETS = "pref_notification_toolbox_manage_widgets";
    private static final String TOOLBOX_MAX_WIDGETS_PER_LINE = "pref_toolbox_max_widgets_per_line";
    private static final int MAX_WIDGETS_DEFAULT = 3;

    private Preference mManageWidgets;
    private WidgetManagerDialog mWidgetDialog;
    private CheckBoxPreference mToolboxDropdown;
    private ListPreference mMaxWidgets;
    private ToolboxEnabler mToolboxEnabler;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_toolbox);

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

        mToolboxEnabler = new ToolboxEnabler(activity, actionBarSwitch);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        mToolboxDropdown = (CheckBoxPreference) mPrefSet.findPreference(NOTIFICATION_TOOLBOX_DROPDOWN);
        mToolboxDropdown.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_DROPDOWN_VIEW, 0) == 1);

        mMaxWidgets = (ListPreference) mPrefSet
                .findPreference(TOOLBOX_MAX_WIDGETS_PER_LINE);
        mMaxWidgets.setValue(Integer.toString(Settings.System.getInt(mCr,
                Settings.System.MAX_WIDGETS_PER_LINE, MAX_WIDGETS_DEFAULT)));
        mMaxWidgets.setOnPreferenceChangeListener(this);

        mManageWidgets = (Preference) mPrefSet
                .findPreference(NOTIFICATION_TOOLBOX_WIDGETS);
    }


    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mToolboxDropdown) {
            value = mToolboxDropdown.isChecked();
            Settings.System.putInt(mCr, Settings.System.NOTIFICATION_DROPDOWN_VIEW,
                    value ? 1 : 0);
            return true;
        } else if (preference == mManageWidgets) {

            mWidgetDialog = new WidgetManagerDialog(preferenceScreen.getContext());
            mWidgetDialog.setTitle(R.string.dialog_title_notification_toolbox_manage_widgets);
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
