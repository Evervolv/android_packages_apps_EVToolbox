package com.evervolv.toolbox.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.superuser.util.Settings;

public class SuperuserSettings extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String PREF_SUPERUSER_LOGGING = "pref_superuser_logging";
    private static final String PREF_SUPERUSER_NOTIFICATIONS = "pref_superuser_notifications";

    private PreferenceScreen mPrefSet;
    private Context mContext;

    private CheckBoxPreference mLogging;
    private ListPreference mNotifications;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.superuser_settings);

        mContext = getActivity();
        mPrefSet = getPreferenceScreen();

        mLogging = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_LOGGING);
        mLogging.setChecked(Settings.getLogging(mContext));

        mNotifications = (ListPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_NOTIFICATIONS);
        mNotifications.setValueIndex(Settings.getNotificationType(mContext));
        mNotifications.setOnPreferenceChangeListener(this);
        setNotificationTypeSummary(Settings.getNotificationType(mContext));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLogging) {
            Settings.setLogging(mContext, mLogging.isChecked());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNotifications) {
            int notifType = Integer.valueOf((String) newValue);
            Settings.setNotificationType(mContext, notifType);
            setNotificationTypeSummary(notifType);
            return true;
        }
        return false;
    }

    private void setNotificationTypeSummary(int type) {
        switch (type) {
            case Settings.NOTIFICATION_TYPE_NONE:
                mNotifications.setSummary(
                        R.string.pref_superuser_notifications_no_notification_summary);
                break;
            case Settings.NOTIFICATION_TYPE_TOAST:
                mNotifications.setSummary(getString(
                        R.string.pref_superuser_notifications_summary,
                        getString(R.string.pref_superuser_notifications_toast).toLowerCase()));
                break;
            case Settings.NOTIFICATION_TYPE_NOTIFICATION:
                mNotifications.setSummary(getString(
                        R.string.pref_superuser_notifications_summary,
                        getString(R.string.pref_superuser_notifications_notification).toLowerCase()));
                break;
        }
    }

}
