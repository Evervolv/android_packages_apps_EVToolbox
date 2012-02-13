package com.evervolv.toolbox.activities.subactivities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.utils.ToolboxUtil;

public class NotificationToolbox extends SettingsFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EVToolbox";

    private static final String NOTIFICATION_TOOLBOX_DROPDOWN = "pref_notification_toolbox_dropdown";
    private static final String NOTIFICATION_TOOLBOX_WIDGETS = "pref_notification_toolbox_widgets";
    private static final String TOOLBOX_MAX_WIDGETS_PER_LINE = "pref_toolbox_max_widgets_per_line";
    private static final String WIDGETS_CATEGORY = "pref_category_statusbar_widgets";
    private static final String SELECT_BUTTON_KEY_PREFIX = "pref_button_";

    private static final int DIALOG_ADD_WIDGET = 0;
    private static final int DIALOG_WIDGET_OPTIONS = 1;

    private Preference mAddWidgets;
    private Context mContext;
    private AlertDialog.Builder mAlertDialog;

    private PreferenceCategory prefWidgets;
    private CheckBoxPreference mToolboxDropdown;
    private ListPreference mMaxWidgets;

    private String clickedPref;
    private String clickedPrefKey;

    private HashMap<Preference, String> mWidgetPrefs = new HashMap<Preference, String>();

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_toolbox);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        mContext = getContext();

        mToolboxDropdown = (CheckBoxPreference) mPrefSet.findPreference(NOTIFICATION_TOOLBOX_DROPDOWN);
        mToolboxDropdown.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_DROPDOWN_VIEW, 0) == 1);

        mMaxWidgets = (ListPreference) mPrefSet
                .findPreference(TOOLBOX_MAX_WIDGETS_PER_LINE);
        mMaxWidgets.setOnPreferenceChangeListener(this);

        prefWidgets = (PreferenceCategory) mPrefSet
                .findPreference(WIDGETS_CATEGORY);

        mAddWidgets = (Preference) mPrefSet
                .findPreference(NOTIFICATION_TOOLBOX_WIDGETS);

        loadWidgetPrefs();

    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mToolboxDropdown) {
            value = mToolboxDropdown.isChecked();
            Settings.System.putInt(mCr, Settings.System.NOTIFICATION_DROPDOWN_VIEW,
                    value ? 1 : 0);
            return true;
        } else if (preference == mAddWidgets) {

            final String[] widgets = new String[ToolboxUtil.WIDGETS.size()];
            final String[] widgetsTitle = new String[ToolboxUtil.WIDGETS.size()];
            int i = 0;
            for(ToolboxUtil.WidgetInfo widget : ToolboxUtil.WIDGETS.values()) {
                widgets[i] = widget.getId();
                widgetsTitle[i] = getResources().getString(widget.getTitleResId());
                i++;
            }

            mAlertDialog = new AlertDialog.Builder(preferenceScreen.getContext());
            mAlertDialog.setTitle(R.string.dialog_title_notification_toolbox_widgets);
            mAlertDialog.setItems(widgetsTitle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    addWidget(widgets[which]);
                }
            });
            mAlertDialog.create().show();
        }

        for(Entry<Preference, String> entry : mWidgetPrefs.entrySet()) {
            if(preference == entry.getKey()) {
                clickedPrefKey = entry.getKey().getKey();
                clickedPref = entry.getKey().getKey().substring(
                        SELECT_BUTTON_KEY_PREFIX.length());

                mAlertDialog = new AlertDialog.Builder(preferenceScreen.getContext());
                mAlertDialog.setTitle(clickedPref);
                mAlertDialog.setMessage(R.string
                        .dialog_notification_toolbox_widgets);

                mAlertDialog.setPositiveButton(R.string.dialog_remove,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        removeWidget(clickedPref, clickedPrefKey);
                    }

                });
                mAlertDialog.setNegativeButton(R.string.dialog_cancel
                        , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //do nada!
                    }

                });
                mAlertDialog.create().show();
            }
        }
        return true;
    }

    public void addWidget(String widget) {
        if (!mWidgetPrefs.containsValue(widget)) {
            ToolboxUtil.saveCurrentWidgets(mContext, ToolboxUtil.
                    appendWidgetString(widget, ToolboxUtil.getCurrentWidgets(mContext)));
            loadWidgetPrefs();
        }
    }

    public void removeWidget(String widget, String prefKey) {

        ArrayList<String> widgetList = new ArrayList<String>();

        mWidgetPrefs.remove(prefWidgets.findPreference(prefKey));
        prefWidgets.removePreference(prefWidgets.findPreference(prefKey));

        for(Entry<Preference, String> entry : mWidgetPrefs.entrySet()) {
            widgetList.add(entry.getKey().getKey().substring(
                    SELECT_BUTTON_KEY_PREFIX.length()));
        }

        Log.d(TAG, "widgetListString: " + ToolboxUtil.getWidgetStringFromList(widgetList));

        ToolboxUtil.saveCurrentWidgets(mContext, ToolboxUtil.
                getWidgetStringFromList(widgetList));
        loadWidgetPrefs();
    }

    public void loadWidgetPrefs() {

        ArrayList<String> widgetList = ToolboxUtil
                .getWidgetListFromString(ToolboxUtil.getCurrentWidgets(mContext));

        prefWidgets.removeAll();
        prefWidgets.setOrderingAsAdded(false);

        mWidgetPrefs.clear();

        for(ToolboxUtil.WidgetInfo widget : ToolboxUtil.WIDGETS.values()) {
            if (widgetList.contains(widget.getId())) {
                Preference pref = new Preference(mContext);
                pref.setIcon(widget.getIconResId());
                pref.setKey(SELECT_BUTTON_KEY_PREFIX + widget.getId());
                pref.setTitle(widget.getTitleResId());
                mWidgetPrefs.put(pref, widget.getId());
                prefWidgets.addPreference(pref);
            }
        }
        Log.d(TAG, "loadWidgetPrefs: " + ToolboxUtil.getCurrentWidgets(mContext));
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
