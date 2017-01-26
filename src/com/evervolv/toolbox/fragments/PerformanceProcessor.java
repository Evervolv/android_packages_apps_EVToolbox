/*
 * Copyright (C) 2013 The Evervolv Project
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

package com.evervolv.toolbox.fragments;

public class PerformanceProcessor {

    public static final String IOSCHED_PREF = "pref_cpu_io_sched";
    public static final String GOV_PREF = "pref_cpu_gov";
    public static final String FREQ_CUR_PREF = "pref_cpu_freq_cur";
    public static final String FREQ_MIN_PREF = "pref_cpu_freq_min";
    public static final String FREQ_MAX_PREF = "pref_cpu_freq_max";
    public static final String SOB_PREF = "pref_cpu_set_on_boot";

    public static boolean freqCapFilesInitialized = false;
    public static final String CPU_ONLINE = "/sys/devices/system/cpu/cpu0/online";
    public static final String SCALE_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String FREQINFO_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";
    public static final String GOV_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static String FREQ_MAX_FILE = null;
    public static String FREQ_MIN_FILE = null;
    public static final String IOSCHED_LIST_FILE = "/sys/block/mmcblk0/queue/scheduler";

}
