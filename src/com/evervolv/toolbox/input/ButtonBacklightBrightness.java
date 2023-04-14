/*
 * Copyright (C) 2013 The CyanogenMod Project
 *               2017-2022 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evervolv.toolbox.input;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.evervolv.settingslib.widget.CustomDialogPreferenceExt;
import com.evervolv.toolbox.utils.DeviceUtils;
import com.evervolv.toolbox.R;

import evervolv.provider.EVSettings;

public class ButtonBacklightBrightness extends CustomDialogPreferenceExt implements
        SeekBar.OnSeekBarChangeListener {
    private static final int BUTTON_BRIGHTNESS_TOGGLE_MODE_ONLY = 1;
    private static final int DEFAULT_BUTTON_TIMEOUT = 5;

    public static final String KEY_BUTTON_BACKLIGHT = "pre_navbar_button_backlight";

    private BrightnessControl mButtonBrightness;
    private BrightnessControl mActiveControl;

    private ViewGroup mTimeoutContainer;
    private SeekBar mTimeoutBar;
    private TextView mTimeoutValue;

    private ContentResolver mResolver;

    private int mOriginalTimeout;

    public ButtonBacklightBrightness(Context context, AttributeSet attrs) {
        super(context, attrs);

        mResolver = context.getContentResolver();

        setDialogLayoutResource(R.layout.button_backlight);

        if (DeviceUtils.hasButtonBacklightSupport(context)) {
            final boolean isSingleValue = BUTTON_BRIGHTNESS_TOGGLE_MODE_ONLY ==
                    context.getResources().getInteger(com.evervolv.platform.internal.R.integer
                            .config_deviceSupportsButtonBrightnessControl);

            float defaultBrightness = context.getResources().getFloat(
                    com.evervolv.platform.internal.R.dimen.config_buttonBrightnessSettingDefaultFloat);

            mButtonBrightness = new BrightnessControl(
                    EVSettings.System.BUTTON_BRIGHTNESS, isSingleValue, defaultBrightness);
            mActiveControl = mButtonBrightness;
        }

        updateSummary();
    }

    @Override
    protected void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        updateBrightnessPreview();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder,
            DialogInterface.OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        builder.setNeutralButton(R.string.reset, null);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.dlg_ok, null);
    }

    @Override
    protected boolean onDismissDialog(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_NEUTRAL) {
            mTimeoutBar.setProgress(DEFAULT_BUTTON_TIMEOUT);
            applyTimeout(DEFAULT_BUTTON_TIMEOUT);
            if (mButtonBrightness != null) {
                mButtonBrightness.reset();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mTimeoutContainer = (ViewGroup) view.findViewById(R.id.timeout_container);
        mTimeoutBar = (SeekBar) view.findViewById(R.id.timeout_seekbar);
        mTimeoutValue = (TextView) view.findViewById(R.id.timeout_value);
        mTimeoutBar.setMax(30);
        mTimeoutBar.setOnSeekBarChangeListener(this);
        mOriginalTimeout = getTimeout();
        mTimeoutBar.setProgress(mOriginalTimeout);
        handleTimeoutUpdate(mTimeoutBar.getProgress());

        ViewGroup buttonContainer = (ViewGroup) view.findViewById(R.id.button_container);
        if (mButtonBrightness != null) {
            mButtonBrightness.init(buttonContainer);
        } else {
            buttonContainer.setVisibility(View.GONE);
            mTimeoutContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (!positiveResult) {
            applyTimeout(mOriginalTimeout);
            return;
        }

        if (mButtonBrightness != null) {
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit()
                    .putFloat(KEY_BUTTON_BACKLIGHT, mButtonBrightness.getBrightness(false))
                    .apply();
        }

        applyTimeout(mTimeoutBar.getProgress());
        if (mButtonBrightness != null) {
            mButtonBrightness.applyBrightness();
        }

        updateSummary();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return superState;
        }

        // Save the dialog state
        final SavedState myState = new SavedState(superState);
        myState.timeout = mTimeoutBar.getProgress();
        if (mButtonBrightness != null) {
            myState.button = mButtonBrightness.getBrightness(false);
        }

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        mTimeoutBar.setProgress(myState.timeout);
        if (mButtonBrightness != null) {
            mButtonBrightness.setBrightness(myState.button);
        }
    }

    public void updateSummary() {
        if (mButtonBrightness != null) {
            float buttonBrightness = mButtonBrightness.getBrightness(true);
            int timeout = getTimeout();

            if (buttonBrightness == 0.0f) {
                setSummary(R.string.backlight_summary_disabled);
            } else if (timeout == 0) {
                setSummary(R.string.backlight_timeout_unlimited);
            } else {
                setSummary(getContext().getString(R.string.backlight_summary_enabled_with_timeout,
                        getTimeoutString(timeout)));
            }
        } else {
            setSummary(R.string.backlight_summary_disabled);
        }
    }

    private String getTimeoutString(int timeout) {
        return getContext().getResources().getQuantityString(
                R.plurals.backlight_timeout_time, timeout, timeout);
    }

    private int getTimeout() {
        return EVSettings.System.getInt(mResolver,
                EVSettings.System.BUTTON_BACKLIGHT_TIMEOUT, DEFAULT_BUTTON_TIMEOUT * 1000) / 1000;
    }

    private void applyTimeout(int timeout) {
        EVSettings.System.putInt(mResolver,
                EVSettings.System.BUTTON_BACKLIGHT_TIMEOUT, timeout * 1000);
    }

    private void updateBrightnessPreview() {
        if (getDialog() == null || getDialog().getWindow() == null) {
            return;
        }
        Window window = getDialog().getWindow();
        LayoutParams params = window.getAttributes();
        if (mActiveControl != null) {
            params.buttonBrightness = mActiveControl.getBrightness(false);
        } else {
            params.buttonBrightness = -1.0f;
        }
        window.setAttributes(params);
    }

    private void updateTimeoutEnabledState() {
        float buttonBrightness = mButtonBrightness != null
                ? mButtonBrightness.getBrightness(false) : 0.0f;
        int count = mTimeoutContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            mTimeoutContainer.getChildAt(i).setEnabled(buttonBrightness != 0.0f);
        }
    }

    private void handleTimeoutUpdate(int timeout) {
        if (timeout == 0) {
            mTimeoutValue.setText(R.string.backlight_timeout_unlimited);
        } else {
            mTimeoutValue.setText(getTimeoutString(timeout));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        handleTimeoutUpdate(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Do nothing here
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        applyTimeout(seekBar.getProgress());
    }

    private static class SavedState extends BaseSavedState {
        int timeout;
        float button;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            timeout = source.readInt();
            button = source.readFloat();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(timeout);
            dest.writeFloat(button);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private class BrightnessControl implements
            SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {
        private String mSetting;
        private boolean mIsSingleValue;
        private float mDefaultBrightness;
        private CheckBox mCheckBox;
        private SeekBar mSeekBar;
        private TextView mValue;

        public BrightnessControl(String setting, boolean singleValue, float defaultBrightness) {
            mSetting = setting;
            mIsSingleValue = singleValue;
            mDefaultBrightness = defaultBrightness;
        }

        public BrightnessControl(String setting, boolean singleValue) {
            this(setting, singleValue, 1.0f);
        }

        public void init(ViewGroup container) {
            float brightness = getBrightness(true);

            if (mIsSingleValue) {
                container.findViewById(R.id.seekbar_container).setVisibility(View.GONE);
                mCheckBox = (CheckBox) container.findViewById(R.id.backlight_switch);
                mCheckBox.setChecked(brightness != 0.0f);
                mCheckBox.setOnCheckedChangeListener(this);
            } else {
                container.findViewById(R.id.checkbox_container).setVisibility(View.GONE);
                mSeekBar = (SeekBar) container.findViewById(R.id.seekbar);
                mValue = (TextView) container.findViewById(R.id.value);

                mSeekBar.setMax(100);
                mSeekBar.setProgress((int)(brightness * 100.0f));
                mSeekBar.setOnSeekBarChangeListener(this);
            }

            handleBrightnessUpdate((int)(brightness * 100.0f));
        }

        public float getBrightness(boolean persisted) {
            if (mCheckBox != null && !persisted) {
                return mCheckBox.isChecked() ? mDefaultBrightness : 0.0f;
            } else if (mSeekBar != null && !persisted) {
                return mSeekBar.getProgress() / 100.0f;
            }
            return EVSettings.System.getFloat(mResolver, mSetting, mDefaultBrightness);
        }

        public void applyBrightness() {
            EVSettings.System.getFloat(mResolver, mSetting, getBrightness(false));
        }

        /* Behaviors when it's a seekbar */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            handleBrightnessUpdate(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mActiveControl = this;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Do nothing here
        }

        /* Behaviors when it's a plain checkbox */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mActiveControl = this;
            updateBrightnessPreview();
            updateTimeoutEnabledState();
        }

        public void setBrightness(float value) {
            if (mIsSingleValue) {
                mCheckBox.setChecked(value != 0.0f);
            } else {
                mSeekBar.setProgress((int)(value * 100.0f));
            }
        }

        public void reset() {
            setBrightness(mDefaultBrightness);
        }

        private void handleBrightnessUpdate(int brightness) {
            updateBrightnessPreview();
            if (mValue != null) {
                mValue.setText(String.format("%d%%", brightness));
            }
            updateTimeoutEnabledState();
        }
    }
}
