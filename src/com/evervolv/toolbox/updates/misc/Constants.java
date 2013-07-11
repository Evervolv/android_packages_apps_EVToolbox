package com.evervolv.toolbox.updates.misc;

public class Constants {

    public static final String TAG = "EVUpdates";

    public static final String APP_NAME = "EVToolbox";

    public static final String DOWNLOAD_DIRECTORY = "EVUpdates/";

    public static final String API_URL = "http://evervolv.com/api/v1/list/";
    public static final String FETCH_URL = "http://evervolv.com/get/";

    public static final String API_URL_NIGHTLY = API_URL + "n/";
    public static final String API_URL_RELEASE = API_URL + "r/";
    public static final String API_URL_TESTING = API_URL + "t/";
    public static final String API_URL_GAPPS   = API_URL + "g/";

    public static final String PREF_LAST_UPDATE_CHECK_NIGHTLY = "pref_last_update_check_nightly";
    public static final String PREF_LAST_UPDATE_CHECK_RELEASE = "pref_last_update_check_release";
    public static final String PREF_LAST_UPDATE_CHECK_TESTING = "pref_last_update_check_testing";

    public static final String PREF_UPDATE_SCHEDULE_NIGHTLY = "pref_updates_nightly_schedule";
    public static final String PREF_UPDATE_SCHEDULE_RELEASE = "pref_updates_release_schedule";
    public static final String PREF_UPDATE_SCHEDULE_TESTING = "pref_updates_testing_schedule";

    public static final int UPDATE_CHECK_NEVER   = -2;
    public static final int UPDATE_CHECK_ONBOOT  = -1;
    public static final int UPDATE_CHECK_DAILY   = 86400;
    public static final int UPDATE_CHECK_WEEKLY  = 604800;
    public static final int UPDATE_CHECK_MONTHLY = 2419200;

    public static final int UPDATE_DEFAULT_NIGHTLY = UPDATE_CHECK_DAILY;
    public static final int UPDATE_DEFAULT_RELEASE = UPDATE_CHECK_MONTHLY;
    public static final int UPDATE_DEFAULT_TESTING = UPDATE_CHECK_NEVER;

    public static final String BUILD_TYPE_RELEASE   = "release";
    public static final String BUILD_TYPE_NIGHTLIES = "nightly";
    public static final String BUILD_TYPE_TESTING   = "testing";
    public static final String BUILD_TYPE_GAPPS     = "gapps";
}
