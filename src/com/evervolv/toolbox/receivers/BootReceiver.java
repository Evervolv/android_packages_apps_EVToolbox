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

    private SharedPreferences mSharedPreferences;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                && !ENCRYPTED_STATE.equals(SystemProperties.get("vold.decrypt"))) {

            // Skip restore if the toolbox has been disabled
            if (!Toolbox.isEnabled(ctx)) {
                return;
            }

            // Check if the user has selected to set values on boot
            if (mSharedPreferences.getBoolean(KernelTuner.SOB_PREF, false) == true) {
                final PowerManager pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
                final PerformanceManager perf = PerformanceManager.getInstance(ctx);
                final SortedSet<PerformanceProfile> profiles = perf.getPowerProfiles();

                // Skip cpu restore if performance profiles are present
                if (profiles.size() == 0) {
                    // CPU preferences
                    restoreCpuPrefs(mSharedPreferences);
                } else {
                    Log.i(TAG, "Performance profile active, skipping CPU restore");
                }

                // IO Sched preferences
                restoreSchedPrefs(mSharedPreferences);

                // KSM
                if (FileUtil.fileExists(KernelTuner.KSM_RUN_FILE)) {
                    boolean enabled = mSharedPreferences.getBoolean(KernelTuner.KSM_PREF, false);
                    FileUtil.fileWriteOneLine(KernelTuner.KSM_RUN_FILE, enabled ? "1" : "0");
                } else {
                    Log.i(TAG, "KSM file is not found, skipping restore");
                }
            } else {
                Log.i(TAG, "Restore disabled by user preference.");
                return;
            }

            // SSH Daemon
            if (mSharedPreferences.getBoolean(SystemPreferences.PREF_SSHD, false)) {
                SystemProperties.set("ctl.start", "sshd");
            }
        }

    }

    private void restoreCpuPrefs(SharedPreferences prefs) {
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

    private void restoreSchedPrefs(SharedPreferences prefs) {
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
}
