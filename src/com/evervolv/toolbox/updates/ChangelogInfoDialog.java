package com.evervolv.toolbox.updates;

import java.io.File;

import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.updates.db.ManifestEntry;
import com.evervolv.toolbox.updates.misc.Constants;
import com.evervolv.toolbox.updates.misc.MD5;

public class ChangelogInfoDialog extends DialogFragment {

    private final ManifestEntry mEntry;
    private final String mStorageDir;
    private DownloadPreference mParent;
    private TextView mMd5SumLocal;
    private TextView mMd5SumServer;

    public ChangelogInfoDialog(ManifestEntry entry, DownloadPreference parent) {
        mEntry = entry;
        mParent = parent;
        mStorageDir = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + Constants.DOWNLOAD_DIRECTORY + mEntry.getType() + "/";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Resources res = getResources();

        getDialog().setTitle(res.getString(R.string.changelog_info_dialog_title));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        View v = inflater.inflate(R.layout.update_info_changelog, container, false);

        boolean showFlashButton = (new File(mStorageDir + mEntry.getName()).exists()
                && !mParent.getBuildType().equals(Constants.BUILD_TYPE_GAPPS));

        Button okayButton = (Button) v.findViewById(R.id.okay);
        okayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button flashButton = (Button) v.findViewById(R.id.flash);
        flashButton.setVisibility(showFlashButton ? View.VISIBLE : View.GONE);
        flashButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mParent.getReadyToFlash();
            }
        });

        setupTabs(v, res);
        setupInfo(v, res);
        return v;
    }

    /* TODO FEATURE:
     * Use "Message" for testing and gapps downloads, so we can display the message
     * given by the maintainer or any message necessary for gapps downloads.
     * Might even need to change the WebView to something else, unless
     * we device to format it.
     */
    private void setupTabs(View view, Resources res) {
        String url = Constants.FETCH_URL + "changelog-" + mEntry.getDate() + ".html";

        WebView wv = (WebView) view.findViewById(R.id.tab_changelog);
        wv.getSettings().setTextZoom(res.getInteger(R.integer.updates_webview_text_zoom));
        wv.loadUrl(url);

        wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return false;
           }
        });

        TabHost tabHost = (TabHost) view.findViewById(R.id.tabhost);
        tabHost.setup();

        TabSpec tabChangelog = tabHost.newTabSpec("Changelog");
        tabChangelog.setIndicator(res.getString(R.string.changelog_info_dialog_tab_title_changelog));
        tabChangelog.setContent(R.id.tab_changelog);
        tabHost.addTab(tabChangelog);

        TabSpec tabInfo = tabHost.newTabSpec("Info");
        tabInfo.setIndicator(res.getString(R.string.changelog_info_dialog_tab_title_info));
        tabInfo.setContent(R.id.tab_info);
        tabHost.addTab(tabInfo);
    }

    private void setupInfo(View view, Resources res) {
        TextView txtDate = (TextView) view.findViewById(R.id.text_date);
        txtDate.setText(mEntry.getDate());

        TextView txtSize = (TextView) view.findViewById(R.id.text_size);
        txtSize.setText(mEntry.getFriendlySize());

        TextView txtFilename = (TextView) view.findViewById(R.id.text_filename);
        txtFilename.setText(mEntry.getName());

        mMd5SumServer = (TextView) view.findViewById(R.id.text_md5sum_server);
        mMd5SumServer.setText(mEntry.getMd5sum());

        File file = new File(mStorageDir + mEntry.getName());
        mMd5SumLocal = (TextView) view.findViewById(R.id.text_md5sum_local);
        if (file.exists()) {
            new CalcMd5Sum().execute(mStorageDir + mEntry.getName());
        } else {
            mMd5SumLocal.setText(R.string.changelog_info_dialog_tab_info_md5sum_local_not_exist);
        }
    }

    private class CalcMd5Sum extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMd5SumLocal.setText(R.string.changelog_info_dialog_tab_info_md5sum_calculate);
        }

        @Override
        protected String doInBackground(String... param) {

            File file = new File(param[0]);
            String md5 = "";
            try {
                md5 = MD5.calculateMD5(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return md5;
        }

        @Override
        protected void onPostExecute(String md5) {
            super.onPostExecute(md5);
            if (md5.equals(mEntry.getMd5sum())) {
                mMd5SumLocal.setTextColor(Color.GREEN);
            } else {
                mMd5SumLocal.setTextColor(Color.RED);
            }
            mMd5SumLocal.setText(md5);
        }
    }
}
