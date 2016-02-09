package com.evervolv.toolbox.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.custom.PinEntryPreference;
import com.evervolv.toolbox.superuser.PinViewHelper;
import com.evervolv.toolbox.superuser.SuperuserUtils;
import com.evervolv.toolbox.superuser.SuperuserAppActivity;
import com.evervolv.toolbox.superuser.SuperuserLogsActivity;

public class Superuser extends PreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_SUPERUSER_AUTO_RESPONSE = "pref_superuser_auto_response";
    private static final String PREF_SUPERUSER_MULTIUSER_POLICY = "pref_superuser_multiuser_policy";
    private static final String PREF_SUPERUSER_SUPERUSER_ACCESS = "pref_superuser_superuser_access";
    private static final String PREF_SUPERUSER_DECLARED_PERMISSION = "pref_superuser_declared_permission";
    private static final String PREF_SUPERUSER_PIN_ENTRY = "pref_superuser_pin_entry";
    private static final String PREF_SUPERUSER_REQUEST_TIMEOUT = "pref_superuser_request_timeout";
    private static final String PREF_SUPERUSER_LOGGING = "pref_superuser_logging";
    private static final String PREF_SUPERUSER_NOTIFICATIONS = "pref_superuser_notifications";

    private PreferenceScreen mPrefSet;
    private Context mContext;

    private ListPreference mAutoResponse;
    private ListPreference mMultiuserPolicy;
    private ListPreference mRequestTimeout;
    private ListPreference mNotifications;
    private SwitchPreference mSuperuserAccess;
    private SwitchPreference mSuperuserPermission;
    private SwitchPreference mLogging;
    private PinEntryPreference mPin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.superuser);

        mContext = getActivity();
        mPrefSet = getPreferenceScreen();

        mSuperuserAccess = (SwitchPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_SUPERUSER_ACCESS);
        mSuperuserAccess.setChecked(SuperuserUtils.getSuperuserAccess());

        mLogging = (SwitchPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_LOGGING);
        mLogging.setChecked(SuperuserUtils.getLogging(mContext));

        mMultiuserPolicy = (ListPreference) mPrefSet.findPreference(PREF_SUPERUSER_MULTIUSER_POLICY);
        mMultiuserPolicy.setOnPreferenceChangeListener(this);

        if (SuperuserUtils.getMultiuserMode(mContext) == SuperuserUtils.MULTIUSER_MODE_NONE) {
            mPrefSet.removePreference(mMultiuserPolicy);
        } else {
            int mode = SuperuserUtils.getMultiuserMode(mContext);
            setMultiuserModeSummary(mode);
            mMultiuserPolicy.setValueIndex(mode);
        }

        mAutoResponse = (ListPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_AUTO_RESPONSE);
        mAutoResponse.setOnPreferenceChangeListener(this);
        mAutoResponse.setValueIndex(SuperuserUtils.getAutomaticResponse(mContext));
        setAutoResponseSummary(SuperuserUtils.getAutomaticResponse(mContext));

        mSuperuserPermission = (SwitchPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_DECLARED_PERMISSION);
        mSuperuserPermission.setChecked(SuperuserUtils.getRequirePermission(mContext));

        mPin = (PinEntryPreference) mPrefSet.findPreference(PREF_SUPERUSER_PIN_ENTRY);
        mPin.setOnPreferenceChangeListener(this);

        mRequestTimeout = (ListPreference) mPrefSet.findPreference(PREF_SUPERUSER_REQUEST_TIMEOUT);
        mRequestTimeout.setOnPreferenceChangeListener(this);
        mRequestTimeout.setSummary(getString(
                R.string.pref_superuser_request_timeout_summary,
                SuperuserUtils.getRequestTimeout(mContext)));
        mRequestTimeout.setValueIndex(getRequestTimeoutIndex(
                SuperuserUtils.getRequestTimeout(mContext)));

        mNotifications = (ListPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_NOTIFICATIONS);
        mNotifications.setValueIndex(SuperuserUtils.getNotificationType(mContext));
        mNotifications.setOnPreferenceChangeListener(this);
        setNotificationTypeSummary(SuperuserUtils.getNotificationType(mContext));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(
                getResources().getString(R.string.tab_title_superuser));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.superuser_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_app_policies:
                Intent applist = new Intent(getActivity(), SuperuserAppActivity.class);
                startActivity(applist);
                return true;
            case R.id.menu_logs:
                Intent logs = new Intent(getActivity(), SuperuserLogsActivity.class);
                startActivity(logs);
                return true;
           default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSuperuserAccess) {
            int val = mSuperuserAccess.isChecked() ?
                    SuperuserUtils.SUPERUSER_ACCESS_APPS_AND_ADB :
                    SuperuserUtils.SUPERUSER_ACCESS_DISABLED;
            SuperuserUtils.setSuperuserAccess(val);
            return true;
        } else if (preference == mLogging) {
            SuperuserUtils.setLogging(mContext, mLogging.isChecked());
            return true;
        } else if (preference == mSuperuserPermission) {
            SuperuserUtils.setRequirePermission(mContext,
                    mSuperuserPermission.isChecked());
            return true;
        } else if (preference == mPin) {
            if (SuperuserUtils.isPinProtected(mContext)) {
                final Dialog dlg = new Dialog(mContext);
                dlg.setTitle(R.string.verify_pin);
                dlg.setContentView(new PinViewHelper((LayoutInflater)
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                        null, null) {

                    public void onEnter(String password) {
                        super.onEnter(password);
                        if (SuperuserUtils.checkPin(mContext, password)) {
                            super.onEnter(password);
                            dlg.dismiss();
                            return;
                        }
                        Toast.makeText(mContext, getString(R.string.incorrect_pin),
                                Toast.LENGTH_SHORT).show();
                    };

                    public void onCancel() {
                        super.onCancel();
                        dlg.dismiss();
                        mPin.getDialog().dismiss();
                    };

                }.getView(), new ViewGroup.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                dlg.show();
            }
        }
        return false;   
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAutoResponse) {
            int response = Integer.valueOf((String) newValue);
            setAutoResponseSummary(response);
            SuperuserUtils.setAutomaticResponse(mContext, response);
            return true;
        } else if (preference == mPin) {
            if (!newValue.equals("")) {
                SuperuserUtils.setPin(mContext, newValue.toString());
            } else {
                SuperuserUtils.setPin(mContext, null);
            }
            return true;
        } else if (preference == mMultiuserPolicy) {
            int mode = Integer.valueOf((String) newValue);
            setMultiuserModeSummary(mode);
            SuperuserUtils.setMultiuserMode(mContext, mode);
            return true;
        } else if (preference == mRequestTimeout) {
            int timeout = Integer.valueOf((String) newValue);
            SuperuserUtils.setTimeout(mContext, timeout);
            mRequestTimeout.setSummary(getString(
                    R.string.pref_superuser_request_timeout_summary, timeout));
            /* TODO: We shouldn't have to do this */
            mRequestTimeout.setValueIndex(getRequestTimeoutIndex(timeout));
        } else if (preference == mNotifications) {
            int notifType = Integer.valueOf((String) newValue);
            SuperuserUtils.setNotificationType(mContext, notifType);
            setNotificationTypeSummary(notifType);
            return true;
        }
        return false;
    }

    private void setNotificationTypeSummary(int type) {
        switch (type) {
            case SuperuserUtils.NOTIFICATION_TYPE_NONE:
                mNotifications.setSummary(
                        R.string.pref_superuser_notifications_no_notification_summary);
                break;
            case SuperuserUtils.NOTIFICATION_TYPE_TOAST:
                mNotifications.setSummary(getString(
                        R.string.pref_superuser_notifications_summary,
                        getString(R.string.pref_superuser_notifications_toast).toLowerCase()));
                break;
            case SuperuserUtils.NOTIFICATION_TYPE_NOTIFICATION:
                mNotifications.setSummary(getString(
                        R.string.pref_superuser_notifications_summary,
                        getString(R.string.pref_superuser_notifications_notification).toLowerCase()));
                break;
        }
    }

    private void setMultiuserModeSummary(int mode) {
        switch (mode) {
            case SuperuserUtils.MULTIUSER_MODE_OWNER_MANAGED:
                mMultiuserPolicy.setSummary(R.string.pref_superuser_multiuser_owner_managed_summary);
                break;
            case SuperuserUtils.MULTIUSER_MODE_OWNER_ONLY:
                mMultiuserPolicy.setSummary(R.string.pref_superuser_multiuser_owner_only_summary);
                break;
            case SuperuserUtils.MULTIUSER_MODE_USER:
                mMultiuserPolicy.setSummary(R.string.pref_superuser_multiuser_user_summary);
                break;
        }
    }

    private void setAutoResponseSummary(int response) {
        switch (response) {
            case SuperuserUtils.AUTOMATIC_RESPONSE_PROMPT:
                mAutoResponse.setSummary(R.string.pref_superuser_auto_response_prompt_summary);
                break;
            case SuperuserUtils.AUTOMATIC_RESPONSE_ALLOW:
                mAutoResponse.setSummary(R.string.pref_superuser_auto_response_allow_summary);
                break;
            case SuperuserUtils.AUTOMATIC_RESPONSE_DENY:
                mAutoResponse.setSummary(R.string.pref_superuser_auto_response_deny_summary);
                break;
        }
    }

    private int getRequestTimeoutIndex(int timeout) {
        switch (timeout) {
            case SuperuserUtils.REQUEST_TIMEOUT_TEN:
                return 0;
            case SuperuserUtils.REQUEST_TIMEOUT_TWENTY:
                return 1;
            case SuperuserUtils.REQUEST_TIMEOUT_THIRTY:
                return 2;
            case SuperuserUtils.REQUEST_TIMEOUT_SIXTY:
                return 3;
        }
        return 2;
    }
}
