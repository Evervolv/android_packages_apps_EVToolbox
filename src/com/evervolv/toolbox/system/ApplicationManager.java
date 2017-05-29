/*
 * Copyright (C) 2013-2017 The Evervolv Project
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
package com.evervolv.toolbox.system;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.utils.ImageCache;

public class ApplicationManager extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.system_app_disabler, container, false);
        final List<String> appWhiteList = new ArrayList<String>(
                Arrays.asList((getActivity().getResources()
                .getStringArray(R.array.disable_apps_whitelist))));
        final ListView listView = (ListView) view.findViewById(R.id.apps_list);
        final AppListAdapter appAdapter = new AppListAdapter(getActivity(),
                R.layout.system_app_disabler_list_item);
        loadSystemApps(appAdapter, appWhiteList);
        listView.setAdapter(appAdapter);

        return view;
    }

    private void loadSystemApps(AppListAdapter adapter, List<String> whiteList) {
        final PackageManager pm = getActivity().getPackageManager();
        final List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (int i = 0; i < apps.size(); i++) {
            if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                String pkg = apps.get(i).packageName;
                for (String app: whiteList) {
                    if (app.split(":")[0].equals(pkg)) {
                        final String name = pm.getApplicationLabel(
                                apps.get(i)).toString();
                        final boolean enabled = apps.get(i).enabled;
                        final String intent;
                        if (app.split(":").length > 1) {
                            intent = app.split(":")[1];
                        } else {
                            intent = "";
                        }
                        adapter.addItem(new App(name, pkg, intent, enabled));
                    }
                }
            }
        }
    }

    private static class AppListAdapter extends ArrayAdapter<App> {

        private final ArrayList<App> mAppList = new ArrayList<App>();
        private final Context mContext;
        private final int mLayoutId;

        public AppListAdapter(Context context, int layoutId) {
            super(context, layoutId, 0);
            mContext = context;
            mLayoutId = layoutId;
        }

        public void addItem(App app) {
            mAppList.add(app);
        }

        @Override
        public App getItem(int position) {
            return mAppList.get(position);
        }

        @Override
        public int getCount() {
            return mAppList.size();
        }

        private static Drawable loadPackageIcon(Context context, String pn) {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = context.getPackageManager().getPackageInfo(pn, PackageManager.GET_PERMISSIONS);
                Drawable ret = ImageCache.getInstance().get(pn);
                if (ret != null)
                    return ret;
                ImageCache.getInstance().put(pn, ret = pi.applicationInfo.loadIcon(pm));
                return ret;
            }
                catch (Exception ex) {
            }
            return null;
        }

        @Override
        public View getView(final int position, final View convertView,
                final ViewGroup parent) {
            View row = convertView;
            AppHolder holder = null;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(mLayoutId, parent, false);

                holder = new AppHolder();
                holder.appIcon = (ImageView) row.findViewById(R.id.app_icon);
                holder.appName = (TextView) row.findViewById(R.id.app_name);
                holder.appPkg = (TextView) row.findViewById(R.id.app_pkg);
                holder.appAction = (CheckBox) row.findViewById(R.id.app_action);
                row.setTag(holder);
            } else {
                holder = (AppHolder) row.getTag();
            }

            final App app = getItem(position);
            final AppListAdapter adapter = this;
            final PackageManager pm = getContext().getPackageManager();
            holder.appName.setText(app.name);
            holder.appPkg.setText(app.pkg);
            holder.appIcon.setImageDrawable(loadPackageIcon(mContext, app.pkg));
            holder.appAction.setChecked(!app.enabled);
            holder.appAction.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox disabled = (CheckBox) v.findViewById(R.id.app_action);
                    if (app.enabled) {
                        if (!app.intent.isEmpty()) {
                            Intent intent = null;
                            try {
                                intent = new Intent(Intent.parseUri(app.intent, 0));
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            if (getContext().getPackageManager().queryIntentActivities(
                                    intent, 0).size() < 2) {
                                AlertDialog dialog = new AlertDialog.Builder(
                                        getContext()).create();
                                dialog.setTitle(app.pkg);
                                dialog.setMessage(getContext().getString(
                                        R.string.system_disable_apps_dialog_message));
                                dialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                                        getContext().getString(com.android.internal.R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });;
                                dialog.show();
                                disabled.setChecked(false);
                                return;
                            }
                        }
                        pm.setApplicationEnabledSetting(app.pkg,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                        app.enabled = false;
                    } else {
                        pm.setApplicationEnabledSetting(app.pkg,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
                        app.enabled = true;
                    }
                }
            });
            return row;
        }

        static class AppHolder {
            ImageView appIcon;
            TextView appName;
            TextView appPkg;
            CheckBox appAction;
        }

    }

    private class App {
        public String name;
        public String pkg;
        public String intent;
        public boolean enabled;

        public App(String name, String pkg, String intent, boolean enabled) {
            this.name = name;
            this.pkg = pkg;
            this.intent = intent;
            this.enabled = enabled;
        }

    }

}
