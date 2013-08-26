package com.evervolv.toolbox.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.custom.PinEntryPreference;
import com.evervolv.toolbox.superuser.PinViewHelper;
import com.evervolv.toolbox.superuser.util.Settings;


public class SuperuserSecurity extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String PREF_SUPERUSER_AUTO_RESPONSE = "pref_superuser_auto_response";
    private static final String PREF_SUPERUSER_MULTIUSER_POLICY = "pref_superuser_multiuser_policy";
    private static final String PREF_SUPERUSER_SUPERUSER_ACCESS = "pref_superuser_superuser_access";
    private static final String PREF_SUPERUSER_DECLARED_PERMISSION = "pref_superuser_declared_permission";
    private static final String PREF_SUPERUSER_PIN_ENTRY = "pref_superuser_pin_entry";
    private static final String PREF_SUPERUSER_REQUEST_TIMEOUT = "pref_superuser_request_timeout";

    private PreferenceScreen mPrefSet;
    private Context mContext;

    private ListPreference mAutoResponse;
    private ListPreference mMultiuserPolicy;
    private ListPreference mRequestTimeout;
    private CheckBoxPreference mSuperuserAccess;
    private CheckBoxPreference mSuperuserPermission;
    private PinEntryPreference mPin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.superuser_security);

        mContext = getActivity();
        mPrefSet = getPreferenceScreen();

        mSuperuserAccess = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_SUPERUSER_ACCESS);
        mSuperuserAccess.setChecked(Settings.getSuperuserAccess());

        mMultiuserPolicy = (ListPreference) mPrefSet.findPreference(PREF_SUPERUSER_MULTIUSER_POLICY);
        mMultiuserPolicy.setOnPreferenceChangeListener(this);

        if (Settings.getMultiuserMode(mContext) == Settings.MULTIUSER_MODE_NONE) {
            mPrefSet.removePreference(mMultiuserPolicy);
        } else {
            int mode = Settings.getMultiuserMode(mContext);
            setMultiuserModeSummary(mode);
            mMultiuserPolicy.setValueIndex(mode);
        }

        mAutoResponse = (ListPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_AUTO_RESPONSE);
        mAutoResponse.setOnPreferenceChangeListener(this);
        mAutoResponse.setValueIndex(Settings.getAutomaticResponse(mContext));
        setAutoResponseSummary(Settings.getAutomaticResponse(mContext));

        mSuperuserPermission = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_SUPERUSER_DECLARED_PERMISSION);
        mSuperuserPermission.setChecked(Settings.getRequirePermission(mContext));

        mPin = (PinEntryPreference) mPrefSet.findPreference(PREF_SUPERUSER_PIN_ENTRY);
        mPin.setOnPreferenceChangeListener(this);

        mRequestTimeout = (ListPreference) mPrefSet.findPreference(PREF_SUPERUSER_REQUEST_TIMEOUT);
        mRequestTimeout.setOnPreferenceChangeListener(this);
        mRequestTimeout.setSummary(getString(
                R.string.pref_superuser_request_timeout_summary,
                Settings.getRequestTimeout(mContext)));
        mRequestTimeout.setValueIndex(getRequestTimeoutIndex(
                Settings.getRequestTimeout(mContext)));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSuperuserAccess) {
            int val = mSuperuserAccess.isChecked() ?
                    Settings.SUPERUSER_ACCESS_APPS_AND_ADB :
                    Settings.SUPERUSER_ACCESS_DISABLED;
            Settings.setSuperuserAccess(val);
            return true;
        } else if (preference == mSuperuserPermission) {
            Settings.setRequirePermission(mContext,
                    mSuperuserPermission.isChecked());
            return true;
        } else if (preference == mPin) {
            if (Settings.isPinProtected(mContext)) {
                final Dialog dlg = new Dialog(mContext);
                dlg.setTitle(R.string.verify_pin);
                dlg.setContentView(new PinViewHelper((LayoutInflater)
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                        null, null) {

                    public void onEnter(String password) {
                        super.onEnter(password);
                        if (Settings.checkPin(mContext, password)) {
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
            Settings.setAutomaticResponse(mContext, response);
            return true;
        } else if (preference == mPin) {
            if (!newValue.equals("")) {
                Settings.setPin(mContext, newValue.toString());
            } else {
                Settings.setPin(mContext, null);
            }
            return true;
        } else if (preference == mMultiuserPolicy) {
            int mode = Integer.valueOf((String) newValue);
            setMultiuserModeSummary(mode);
            Settings.setMultiuserMode(mContext, mode);
            return true;
        } else if (preference == mRequestTimeout) {
            int timeout = Integer.valueOf((String) newValue);
            Settings.setTimeout(mContext, timeout);
            mRequestTimeout.setSummary(getString(
                    R.string.pref_superuser_request_timeout_summary, timeout));
            /* TODO: We shouldn't have to do this */
            mRequestTimeout.setValueIndex(getRequestTimeoutIndex(timeout));
        }
        return false;
    }

    private void setMultiuserModeSummary(int mode) {
        switch (mode) {
            case Settings.MULTIUSER_MODE_OWNER_MANAGED:
                mMultiuserPolicy.setSummary(R.string.pref_superuser_multiuser_owner_managed_summary);
                break;
            case Settings.MULTIUSER_MODE_OWNER_ONLY:
                mMultiuserPolicy.setSummary(R.string.pref_superuser_multiuser_owner_only_summary);
                break;
            case Settings.MULTIUSER_MODE_USER:
                mMultiuserPolicy.setSummary(R.string.pref_superuser_multiuser_user_summary);
                break;
        }
    }

    private void setAutoResponseSummary(int response) {
        switch (response) {
            case Settings.AUTOMATIC_RESPONSE_PROMPT:
                mAutoResponse.setSummary(R.string.pref_superuser_auto_response_prompt_summary);
                break;
            case Settings.AUTOMATIC_RESPONSE_ALLOW:
                mAutoResponse.setSummary(R.string.pref_superuser_auto_response_allow_summary);
                break;
            case Settings.AUTOMATIC_RESPONSE_DENY:
                mAutoResponse.setSummary(R.string.pref_superuser_auto_response_deny_summary);
                break;
        }
    }

    private int getRequestTimeoutIndex(int timeout) {
        switch (timeout) {
            case Settings.REQUEST_TIMEOUT_TEN:
                return 0;
            case Settings.REQUEST_TIMEOUT_TWENTY:
                return 1;
            case Settings.REQUEST_TIMEOUT_THIRTY:
                return 2;
            case Settings.REQUEST_TIMEOUT_SIXTY:
                return 3;
        }
        return 2;
    }

}
