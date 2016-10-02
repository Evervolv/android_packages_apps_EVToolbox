/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.evervolv.toolbox.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PerformanceManager;
import android.os.PerformanceProfile;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.perf.KernelTuner;
import com.evervolv.toolbox.system.SystemPreferences;
import com.evervolv.toolbox.utils.FileUtil;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    private static final String CPU_SETTINGS_PROP = "sys.cpufreq.restored";
    private static final String IOSCHED_SETTINGS_PROP = "sys.iosched.restored";
    private static final String KSM_SETTINGS_PROP = "sys.ksm.restored";
    private static final String SSHD_SETTINGS_PROP = "sys.sshd.restored";

    private static final String ENCRYPTED_STATE = "1";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                && !ENCRYPTED_STATE.equals(SystemProperties.get("vold.decrypt"))) {
            if (!Toolbox.isEnabled(ctx)) {
                return;
            }

            final PowerManager pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
            final PerformanceManager perf = PerformanceManager.getInstance(ctx);
            final SortedSet<PerformanceProfile> profiles = perf.getPowerProfiles();

            if (profiles.size() == 0) {
                if (SystemProperties.getBoolean(CPU_SETTINGS_PROP, false) == false) {
                    SystemProperties.set(CPU_SETTINGS_PROP, "true");
                    configureCPU(ctx);
                } else {
                    SystemProperties.set(CPU_SETTINGS_PROP, "false");
                }
            }

            if (SystemProperties.getBoolean(IOSCHED_SETTINGS_PROP, false) == false) {
                SystemProperties.set(IOSCHED_SETTINGS_PROP, "true");
                configureIOSched(ctx);
            } else {
                SystemProperties.set(IOSCHED_SETTINGS_PROP, "false");
            }

            if (FileUtil.fileExists(KernelTuner.KSM_RUN_FILE)) {
                if (!SystemProperties.getBoolean(KSM_SETTINGS_PROP, false)) {
                    SystemProperties.set(KSM_SETTINGS_PROP, "true");
                    configureKSM(ctx);
                } else {
                    SystemProperties.set(KSM_SETTINGS_PROP, "false");
                }
            }

            if (!SystemProperties.getBoolean(SSHD_SETTINGS_PROP, false)) {
                maybeEnableSshd(ctx);
            }

        }

    }

    private void configureCPU(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (prefs.getBoolean(KernelTuner.SOB_PREF, false) == false) {
            Log.i(TAG, "CPU restore disabled by user preference.");
            return;
        }

        String governor = prefs.getString(KernelTuner.GOV_PREF, null);
        String minFrequency = prefs.getString(KernelTuner.FREQ_MIN_PREF, null);
        String maxFrequency = prefs.getString(KernelTuner.FREQ_MAX_PREF, null);
        String availableFrequenciesLine = FileUtil.fileReadOneLine(KernelTuner.FREQ_LIST_FILE);
        String availableGovernorsLine = FileUtil.fileReadOneLine(KernelTuner.GOV_LIST_FILE);
        boolean noSettings = ((availableGovernorsLine == null) || (governor == null)) &&
                             ((availableFrequenciesLine == null) || ((minFrequency == null) && (maxFrequency == null)));
        List<String> frequencies = null;
        List<String> governors = null;

        if (noSettings) {
            Log.d(TAG, "No CPU settings saved. Nothing to restore.");
        } else {
            if (availableGovernorsLine != null){
                governors = Arrays.asList(availableGovernorsLine.split(" "));
            }
            if (availableFrequenciesLine != null){
                frequencies = Arrays.asList(availableFrequenciesLine.split(" "));
            }
            if (maxFrequency != null && frequencies != null && frequencies.contains(maxFrequency)) {
                FileUtil.fileWriteOneLine(KernelTuner.FREQ_MAX_FILE, maxFrequency);
            }
            if (minFrequency != null && frequencies != null && frequencies.contains(minFrequency)) {
                FileUtil.fileWriteOneLine(KernelTuner.FREQ_MIN_FILE, minFrequency);
            }
            if (governor != null && governors != null && governors.contains(governor)) {
                FileUtil.fileWriteOneLine(KernelTuner.GOV_FILE, governor);
            }
            Log.d(TAG, "CPU settings restored.");
        }
    }

    private void configureIOSched(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (prefs.getBoolean(KernelTuner.SOB_PREF, false) == false) {
            Log.i(TAG, "IOSched restore disabled by user preference.");
            return;
        }

        String ioscheduler = prefs.getString(KernelTuner.IOSCHED_PREF, null);
        String availableIOSchedulersFile = KernelTuner.findIoScheduler();
        String availableIOSchedulersLine = FileUtil.fileReadOneLine(availableIOSchedulersFile);
        boolean noSettings = ((availableIOSchedulersLine == null) || (ioscheduler == null));
        List<String> ioschedulers = null;

        if (noSettings) {
            Log.d(TAG, "No I/O scheduler settings saved. Nothing to restore.");
        } else {
            if (availableIOSchedulersLine != null){
                ioschedulers = Arrays.asList(availableIOSchedulersLine.replace("[", "").replace("]", "").split(" "));
            }
            if (ioscheduler != null && ioschedulers != null && ioschedulers.contains(ioscheduler)) {
                FileUtil.fileWriteOneLine(availableIOSchedulersFile, ioscheduler);
            }
            Log.d(TAG, "I/O scheduler settings restored.");
        }
    }

    private void configureKSM(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean ksm = prefs.getBoolean(KernelTuner.KSM_PREF, ActivityManager.isLowRamDeviceStatic());

        FileUtil.fileWriteOneLine(KernelTuner.KSM_RUN_FILE, ksm ? "1" : "0");
        Log.d(TAG, "KSM settings restored.");
    }

    private void maybeEnableSshd(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (prefs.getBoolean(SystemPreferences.PREF_SSHD, false)) {
            SystemProperties.set("ctl.start", "sshd");
            SystemProperties.set(SSHD_SETTINGS_PROP, "true");
        } else {
            SystemProperties.set(SSHD_SETTINGS_PROP, "false");
        }
    }

}
