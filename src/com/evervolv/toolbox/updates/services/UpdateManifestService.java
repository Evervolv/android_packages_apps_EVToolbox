package com.evervolv.toolbox.updates.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.evervolv.toolbox.updates.db.DatabaseManager;
import com.evervolv.toolbox.updates.misc.Constants;
import com.evervolv.toolbox.updates.misc.Utils;

public class UpdateManifestService extends IntentService {

    private static final String TAG = Constants.TAG;

    /* Intent extra fields */
    public static final String EXTRA_MANIFEST_ERROR = "manifest_error";
    public static final String EXTRA_UPDATE_NON_INTERACTIVE = "update_non_interactive";
    public static final String EXTRA_SCHEDULE_UPDATE = "update_schedule";

    /* Intent actions */
    public static final String ACTION_UPDATE_CHECK_NIGHTLY = "com.evervolv.updates.actions.UPDATE_CHECK_NIGHTLY";
    public static final String ACTION_UPDATE_CHECK_RELEASE = "com.evervolv.updates.actions.UPDATE_CHECK_RELEASE";
    public static final String ACTION_UPDATE_CHECK_TESTING = "com.evervolv.updates.actions.UPDATE_CHECK_TESTING";
    public static final String ACTION_UPDATE_CHECK_GAPPS = "com.evervolv.updates.actions.UPDATE_CHECK_GAPPS";
    public static final String ACTION_CHECK_FINISHED = "com.evervolv.updates.actions.UPDATE_CHECK_FINISHED";
    public static final String ACTION_BOOT_COMPLETED = "com.evervolv.updates.actions.BOOT_COMPLETED";

    private SharedPreferences preferences;
    private DatabaseManager databaseManager;

    public UpdateManifestService() {
        super("UpdateManifestService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(Constants.APP_NAME, Context.MODE_MULTI_PROCESS);
        databaseManager = new DatabaseManager(this);
        databaseManager.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseManager.close();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long now = System.currentTimeMillis();
        boolean error = false;
        String action = intent.getAction();
        boolean nonInteractive = intent.getBooleanExtra(EXTRA_UPDATE_NON_INTERACTIVE, false);
        boolean schedule = intent.getBooleanExtra(EXTRA_SCHEDULE_UPDATE, false);
        int updateFreq;
        long lastCheck;
        if (action.equals(ACTION_UPDATE_CHECK_NIGHTLY)) {
            preferences.edit().putLong(Constants.PREF_LAST_UPDATE_CHECK_NIGHTLY, now).commit();
            if (schedule) {
                updateFreq = preferences.getInt(Constants.PREF_UPDATE_SCHEDULE_NIGHTLY,
                        Constants.UPDATE_DEFAULT_NIGHTLY);
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_NIGHTLY, updateFreq, now, false);
                return;
            }
            error = handleManifest(Constants.API_URL_NIGHTLY, DatabaseManager.NIGHTLIES);
        } else if (action.equals(ACTION_UPDATE_CHECK_RELEASE)) {
            preferences.edit().putLong(Constants.PREF_LAST_UPDATE_CHECK_RELEASE, now).commit();
            if (schedule) {
                updateFreq = preferences.getInt(Constants.PREF_UPDATE_SCHEDULE_RELEASE,
                        Constants.UPDATE_DEFAULT_RELEASE);
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_RELEASE, updateFreq, now, false);
                return;
            }
            error = handleManifest(Constants.API_URL_RELEASE, DatabaseManager.RELEASES);
        } else if (action.equals(ACTION_UPDATE_CHECK_TESTING)) {
            preferences.edit().putLong(Constants.PREF_LAST_UPDATE_CHECK_TESTING, now).commit();
            if (schedule) {
                updateFreq = preferences.getInt(Constants.PREF_UPDATE_SCHEDULE_TESTING,
                        Constants.UPDATE_DEFAULT_TESTING);
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_TESTING, updateFreq, now, false);
                return;
            }
            error = handleManifest(Constants.API_URL_TESTING, DatabaseManager.TESTING);
        } else if (action.equals(ACTION_UPDATE_CHECK_GAPPS)) {
            error = handleManifest(Constants.API_URL_GAPPS, DatabaseManager.GAPPS);
        } else if (action.equals(ACTION_BOOT_COMPLETED)) {
            /* Nightlies */
            updateFreq = preferences.getInt(Constants.PREF_UPDATE_SCHEDULE_NIGHTLY,
                    Constants.UPDATE_DEFAULT_NIGHTLY);
            lastCheck = preferences.getLong(
                    Constants.PREF_LAST_UPDATE_CHECK_NIGHTLY, now);

            if (updateFreq > Constants.UPDATE_CHECK_ONBOOT ) {
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_NIGHTLY, updateFreq, lastCheck, false);
            } else if (updateFreq == Constants.UPDATE_CHECK_ONBOOT) {
                /* Schedule check 2 mins from now to give radio time to connect */
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_NIGHTLY, 2 * 60, now, true);
            }
            /* Releases */
            updateFreq = preferences.getInt(Constants.PREF_UPDATE_SCHEDULE_RELEASE,
                    Constants.UPDATE_DEFAULT_RELEASE);
            lastCheck = preferences.getLong(
                    Constants.PREF_LAST_UPDATE_CHECK_RELEASE, now);

            if (updateFreq > Constants.UPDATE_CHECK_ONBOOT ) {
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_RELEASE, updateFreq, lastCheck, false);
            } else if (updateFreq == Constants.UPDATE_CHECK_ONBOOT) {
                /* Schedule check 2 mins from now to give radio time to connect */
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_RELEASE, 2 * 60, now, true);
            }
            /* Testing */
            updateFreq = preferences.getInt(Constants.PREF_UPDATE_SCHEDULE_TESTING,
                    Constants.UPDATE_DEFAULT_TESTING);
            lastCheck = preferences.getLong(
                    Constants.PREF_LAST_UPDATE_CHECK_TESTING, now);

            if (updateFreq > Constants.UPDATE_CHECK_ONBOOT) {
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_TESTING, updateFreq, lastCheck, false);
            } else if (updateFreq == Constants.UPDATE_CHECK_ONBOOT) {
                /* Schedule check 2 mins from now to give radio time to connect */
                scheduleUpdateCheck(ACTION_UPDATE_CHECK_TESTING, 2 * 60, now, true);
            }
            return;
        } else {
            Log.e(TAG, "Unimplemented: " + action);
            return;
        }
        if (!nonInteractive) {
            Intent checkIntent = new Intent();
            checkIntent.setAction(ACTION_CHECK_FINISHED);
            checkIntent.putExtra(EXTRA_MANIFEST_ERROR, error);
            sendBroadcast(checkIntent);
        }
    }

    private boolean handleManifest(String url, int updateType) {
        boolean error = false;
        String jsonString;
        try {
            jsonString = fetchManifest(url);
            processManifest(jsonString, updateType);
        } catch (Exception e) {
            e.printStackTrace();
            error = true;
        }
        return error;
    }

    private String fetchManifest(String url) throws IOException, HttpException {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(url + Utils.getDevice());

        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == 200) {
            InputStream content = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } else {
            throw new HttpException("Failed to fetch manifest");
        }
        return builder.toString();
    }

    private void processManifest(String jsonString, int updateType)
            throws JSONException, SQLiteException {
        JSONArray entries = new JSONArray(jsonString);
        databaseManager.updateManifest(updateType, entries);
        for (int i=0; i<entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            if (Utils.isNewerThanInstalled(entry.getString("date"))) {
                Log.i(TAG, "Found new update");
                /* TODO FEATURE:
                 * Notify user of new update found.
                 */
                break;
            }
        }
    }

    private void scheduleUpdateCheck(String action, int interval, long lastCheck, boolean oneshot) {

        Intent updateCheck = new Intent(this, UpdateManifestService.class);
        updateCheck.setAction(action);
        updateCheck.putExtra(UpdateManifestService.EXTRA_UPDATE_NON_INTERACTIVE, true);
        PendingIntent pi = PendingIntent.getService(this, 0, updateCheck,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        long intervalMillis = ((long)interval) * 1000;
        long now = System.currentTimeMillis();
        if (!oneshot) {
            if (interval > Constants.UPDATE_CHECK_ONBOOT) {
                am.setRepeating(AlarmManager.RTC, lastCheck + intervalMillis, intervalMillis, pi);
                Log.i(TAG, "Scheduled update check for "
                        + (((lastCheck + intervalMillis) - now) / 1000) + " seconds from now"
                        + " repeating every " + interval + " seconds");
            }
        } else {
            am.set(AlarmManager.RTC, lastCheck + intervalMillis, pi);
            Log.i(TAG, "Scheduled update check for "
                    + (((lastCheck + intervalMillis) - now) / 1000) + " seconds from now");
        }
    }

}
