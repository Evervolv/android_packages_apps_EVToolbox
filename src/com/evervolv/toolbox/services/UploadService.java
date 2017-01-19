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
package com.evervolv.toolbox.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.SystemProperties;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.utils.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadService extends Service {

    private static final String TAG = "UploadService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Boolean plaintext = intent.getBooleanExtra(Constants.EXTRA_PLAINTEXT, false);
        final File logcatFile = getFileExtra(intent, Constants.EXTRA_LOGCAT);
        String data;
        try {
            data = FileUtils.readTextFile(logcatFile, 0, null);
        } catch (IOException e) {
            e.printStackTrace();
            notifyFailed();
            notifyFragment(null);
            return START_NOT_STICKY;
        }
        notifyStart();
        new UploadLogcat().execute(logcatFile.getParentFile(), plaintext, data);
        return START_NOT_STICKY;
    }

    private class UploadLogcat extends AsyncTask<Object, Void, String> {
        protected String doInBackground(Object... params){
            File logcatFolder = (File) params[0];
            Boolean plaintext = (Boolean) params[1];
            String data = (String) params[2];
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(Constants.PASTE_URL);
            String result = null;
            try {
                // First cleanup old logcats
                FileUtils.deleteOlderFiles(logcatFolder, 2, DateUtils.DAY_IN_MILLIS);
                // Build post
                List<NameValuePair> valuePairList = new ArrayList<NameValuePair>();
                valuePairList.add(new BasicNameValuePair("name",
                        getResources().getString(R.string.logcat_post_author)));
                valuePairList.add(new BasicNameValuePair("title",
                        String.format(getResources().getString(R.string.logcat_post_title),
                                SystemProperties.get("ro.evervolv.version"))));
                if (!plaintext) {
                    valuePairList.add(new BasicNameValuePair("lang", "logcat"));
                }
                valuePairList.add(new BasicNameValuePair("text", data));
                post.setEntity(new UrlEncodedFormEntity(valuePairList));
                // Send post
                HttpResponse response = client.execute(post);
                // Read response
                result = EntityUtils.toString(response.getEntity());
                Log.d(TAG, "Created: " + result);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return result;
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                //notifySuccess(result);
            } else {
                notifyFailed();
            }
            notifyFragment(result);
            stopSelf();
        }

    }

    private void notifyStart() {
        Toast.makeText(this, R.string.logcat_toast_start, Toast.LENGTH_SHORT).show();
    }

    private void notifySuccess(String url) {
        Notification n = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_toolbox)
                .setTicker(getResources().getString(R.string.logcat_notification_title))
                .setContentTitle(getResources().getString(R.string.logcat_notification_title))
                .setContentText(url)
                .setContentIntent(PendingIntent.getActivity(this,
                        0, new Intent(Intent.ACTION_VIEW, Uri.parse(url)), 0)
                )
                .setAutoCancel(true)
                .build();
        NotificationManager nm =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(555,n);
    }

    private void notifyFailed() {
        Toast.makeText(this, R.string.logcat_toast_upload_failed, Toast.LENGTH_SHORT).show();
    }

    private void notifyFragment(String url) {
        Intent finished = new Intent(Constants.ACTION_UPLOAD_FINISHED);
        finished.putExtra(Constants.EXTRA_URL, url);
        sendBroadcast(finished, Constants.PERMISSION_DUMPLOGCAT);
    }

    private static File getFileExtra(Intent intent, String key) {
        final String path = intent.getStringExtra(key);
        Log.d(TAG, "Uploading logcat " + path);
        if (path != null) {
            return new File(path);
        }
        return null;
    }
}
