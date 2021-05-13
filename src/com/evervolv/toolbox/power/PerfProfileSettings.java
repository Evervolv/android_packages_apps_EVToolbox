/*
 * Copyright (C) 2016 The CyanogenMod Project
 *               2017 The LineageOS Project
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

package com.evervolv.toolbox.power;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings.Global;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.android.settingslib.widget.FooterPreference;

import com.evervolv.internal.graphics.drawable.StopMotionVectorDrawable;

import com.evervolv.toolbox.PartsUpdater;
import com.evervolv.toolbox.R;
import com.evervolv.toolbox.widget.SeekBarPreference;
import com.evervolv.toolbox.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

import evervolv.power.PerformanceManager;
import evervolv.power.PerformanceProfile;
import evervolv.provider.EVSettings;

import static evervolv.power.PerformanceManager.PROFILE_POWER_SAVE;

public class PerfProfileSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_PERF_SEEKBAR     = "perf_seekbar";
    private static final String KEY_PERF_FOOTER      = "perf_footer";

    private SeekBarPreference        mPerfSeekBar;
    private StopMotionVectorDrawable mPerfDrawable;
    private PerfIconAnimator         mIconAnimator;
    private FooterPreference         mPerfFooter;

    private PowerManager       mPowerManager;
    private PerformanceManager mPerf;

    private int mLastSliderValue = 0;

    private List<PerformanceProfile> mProfiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.perf_profile_settings);

        mPerfSeekBar = (SeekBarPreference) findPreference(KEY_PERF_SEEKBAR);
        mPerfFooter = (FooterPreference) findPreference(KEY_PERF_FOOTER);

        mPowerManager = getActivity().getSystemService(PowerManager.class);
        mPerf = PerformanceManager.getInstance(getActivity());

        mProfiles = new ArrayList<>(mPerf.getPowerProfiles());

        mPerfDrawable = new StopMotionVectorDrawable(
                (AnimatedVectorDrawable) getActivity().getDrawable(
                        R.drawable.ic_perf_profile_avd));
        mPerfSeekBar.setIconDrawable(mPerfDrawable);
        mIconAnimator = new PerfIconAnimator(getActivity(), mPerfDrawable);

        mPerfSeekBar.setMax(mProfiles.size() - 1);
        mPerfSeekBar.setOnPreferenceChangeListener(this);
        updatePerfSettings();

        watch(EVSettings.Secure.getUriFor(EVSettings.Secure.PERFORMANCE_PROFILE));
    }


    private static class PerfIconAnimator {

        private final Context mContext;
        private final StopMotionVectorDrawable mDrawable;

        private final ValueAnimator mGradient;
        private final AnimatorSet   mAnimatorSet = new AnimatorSet();

        public PerfIconAnimator(Context context, StopMotionVectorDrawable drawable) {
            mContext = context;
            mDrawable = drawable;
            TypedValue colorAccent = new TypedValue();
            mContext.getTheme().resolveAttribute(com.android.internal.R.attr.colorAccent,
                    colorAccent, true);
            mGradient = ValueAnimator.ofArgb(
                    mContext.getResources().getColor(R.color.perf_cold),
                    mContext.getResources().getColor(colorAccent.resourceId),
                    mContext.getResources().getColor(R.color.perf_hot));
            mAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        }

        private int getColorAt(float fraction) {
            mGradient.setCurrentFraction(fraction);
            return (Integer) mGradient.getAnimatedValue();
        }

        public void animateRange(float from, float to) {
            mAnimatorSet.cancel();
            mAnimatorSet.removeAllListeners();

            final ValueAnimator scale = ValueAnimator.ofFloat(from, to);
            final ValueAnimator color = ValueAnimator.ofArgb(
                    getColorAt(from), getColorAt(from + ((to - from) / 2)), getColorAt(to));

            scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mDrawable.setCurrentFraction(
                            (Float) valueAnimator.getAnimatedValue());
                }
            });
            color.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mDrawable.setColorFilter(
                            (Integer) valueAnimator.getAnimatedValue(),
                            PorterDuff.Mode.SRC_IN);
                }
            });

            mAnimatorSet.play(scale).with(color);
            mAnimatorSet.start();
        }
    }

    private void updatePerfSettings() {
        if (mPerfSeekBar == null) {
            return;
        }

        PerformanceProfile profile = mPowerManager.isPowerSaveMode() ?
                mPerf.getPowerProfile(PROFILE_POWER_SAVE) : mPerf.getActivePowerProfile();
        mPerfSeekBar.setProgress(mProfiles.indexOf(profile));

        if (mPerfDrawable != null) {
            final float start = mProfiles.get(mLastSliderValue).getWeight();
            final float end = profile.getWeight();
            mIconAnimator.animateRange(start, end);
        }

        if (mPerfFooter != null) {
            String summary = null;
            if (profile != null) {
                summary = getResources().getString(
                        R.string.perf_profile_overview_summary,
                        profile.getName());
                summary += "\n\n" + profile.getDescription();
            }
            mPerfFooter.setTitle(summary.replace("\\n", System.getProperty("line.separator")));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPerfSeekBar) {
            mLastSliderValue = mPerfSeekBar.getProgress();
            int index = (Integer) newValue;
            if (!mPerf.setPowerProfile(mProfiles.get(index).getId())) {
                // Don't just fail silently, inform the user as well
                Toast.makeText(getActivity(),
                        R.string.perf_profile_fail_toast, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onSettingsChanged(Uri contentUri) {
        super.onSettingsChanged(contentUri);
        updatePerfSettings();
    }

    public static final SummaryProvider SUMMARY_PROVIDER = new SummaryProvider() {
        @Override
        public String getSummary(Context context, String key) {
            return context.getString(R.string.perf_profile_settings_summary);
        }
    };
}
