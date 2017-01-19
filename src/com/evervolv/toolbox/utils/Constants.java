package com.evervolv.toolbox.utils;

public class Constants {
    public static final String APP_NAME = "EVToolbox";

    public static final String PASTE_URL = "http://paste.evervolv.com/api/create";
    public static final String ACTION_UPLOAD_FINISHED = "com.evervolv.toolbox.action.UPLOAD_FINISED";
    public static final String ACTION_DUMPLOGCAT_FINISHED = "com.evervolv.toolbox.action.DUMPLOGCAT_FINISHED";
    public static final String PERMISSION_DUMPLOGCAT = "com.evervolv.toolbox.permission.DUMPLOGCAT";
    public static final String EXTRA_LOGCAT = "com.evervolv.toolbox.intent.extra.LOGCAT";
    public static final String EXTRA_PLAINTEXT = "com.evervolv.toolbox.intent.extra.PLAINTEXT";
    public static final String EXTRA_URL = "com.evervolv.toolbox.intet.extra.URL";
    public static final String DUMPLOGCAT_SERVICE = "dumplogcat:";
    public static final String DUMPLOGCAT_SERVICE_DEFAULT_ARGS = "-D -B -o ";
    public static final String LOGCAT_FILE_PREFIX = "logcat";
    public static final String MAIN_BUFFER_ARG = " -m";
    public static final String RADIO_BUFFER_ARG = " -r";
    public static final String DMESG_ARG = " -d";
    public static final String LAST_KMSG_ARG = " -k";
}
