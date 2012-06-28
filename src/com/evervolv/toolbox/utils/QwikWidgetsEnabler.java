package com.evervolv.toolbox.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

public class QwikWidgetsEnabler implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "QwikWidgetsEnabler";

    private final Context mContext;
    private Switch mSwitch;

    private SettingsObserver mObserver;
    private Handler mHandler = new Handler();

    public QwikWidgetsEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        setupSettingsObserver(mHandler);
        mSwitch.setOnCheckedChangeListener(this);

        ContentResolver resolver = mContext.getContentResolver();
        boolean enabled = (Settings.System.getInt(resolver, Settings
                .System.USE_QWIK_WIDGETS, 1) == 1);
        mSwitch.setChecked(enabled);
    }

    public void resume() {
        mObserver.unobserve();
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mObserver.observe();
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void setupSettingsObserver(Handler handler) {
        if(mObserver == null) {
            mObserver = new SettingsObserver(handler);
            mObserver.observe();
        }
    }

    private class SettingsObserver extends ContentObserver  {

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            ContentResolver resolver = mContext.getContentResolver();

            resolver.registerContentObserver(Settings.System.getUriFor(Settings
                    .System.USE_QWIK_WIDGETS), false, this);
        }

        public void unobserve() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChangeUri(Uri uri, boolean selfChange) {
            ContentResolver resolver = mContext.getContentResolver();
            Log.d(TAG, "onChangeUri");
            boolean enabled = (Settings.System.getInt(resolver, Settings
                    .System.USE_QWIK_WIDGETS, 1) == 1);
            mSwitch.setChecked(enabled);
        }
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) return;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);

        ContentResolver resolver = mContext.getContentResolver();
        boolean enabled = (Settings.System.getInt(resolver, Settings
                .System.USE_QWIK_WIDGETS, 1) == 1);
        mSwitch.setChecked(enabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckChanged");
        ContentResolver resolver = mContext.getContentResolver();
        boolean value;
        value = isChecked;
        Settings.System.putInt(resolver, Settings.System
                .USE_QWIK_WIDGETS, value ? 1 : 0);
    }

}
