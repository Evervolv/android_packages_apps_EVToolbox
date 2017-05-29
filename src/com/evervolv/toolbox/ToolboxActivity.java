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

package com.evervolv.toolbox;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.support.v14.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;

import com.evervolv.toolbox.hardware.ButtonPreferences;
import com.evervolv.toolbox.perf.KernelTuner;
import com.evervolv.toolbox.perf.PerformancePreferences;
import com.evervolv.toolbox.system.ApplicationManager;
import com.evervolv.toolbox.system.InterfacePreferences;
import com.evervolv.toolbox.system.StatusbarPreferences;
import com.evervolv.toolbox.system.SystemPreferences;
import com.evervolv.toolbox.support.BugReport;
import com.evervolv.toolbox.support.Preferences;

public class ToolboxActivity extends AppCompatActivity implements
        PreferenceFragment.OnPreferenceStartFragmentCallback,
        PreferenceFragment.OnPreferenceStartScreenCallback {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private String mInitialTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbox);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(mNavigationItemSelectedListener);

        final String action = getIntent().getAction();
        final Fragment fragment;
        boolean openDrawer = false;
        if ("com.evervolv.toolbox.action.KERNEL_TUNER".equals(action)) {
            fragment = new KernelTuner();
        } else if ("com.evervolv.toolbox.action.BUGREPORT".equals(action)) {
            fragment = new BugReport();
        } else if ("com.evervolv.toolbox.action.APP_MANAGER".equals(action)) {
            fragment = new ApplicationManager();
        } else {
            fragment = (PreferenceFragment) new Preferences();
            openDrawer = true;
        }

        if (openDrawer) {
            // Default options
            mDrawerLayout.openDrawer(GravityCompat.START);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();

        mInitialTitle = String.valueOf(getSupportActionBar().getTitle());
    }

    private NavigationView.OnNavigationItemSelectedListener mNavigationItemSelectedListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            if (handleNavigationItemSelected(item)) {
                mDrawerLayout.closeDrawers();
                return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } else {
            getSupportActionBar().setTitle(mInitialTitle);
        }
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_interface) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new InterfacePreferences()).commit();
        } else if (id == R.id.nav_statusbar) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new StatusbarPreferences()).commit();
        } else if (id == R.id.nav_system) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new SystemPreferences()).commit();
        } else if (id == R.id.nav_hardware) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new ButtonPreferences()).commit();
        } else if (id == R.id.nav_performance) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new PerformancePreferences()).commit();
        } else if (id == R.id.nav_settings) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new Preferences()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        try {
            Class<?> cls = Class.forName(pref.getFragment());
            Fragment fragment = (Fragment) cls.newInstance();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            getActionBar().setTitle(pref.getTitle());
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack("PreferenceFragment");
            transaction.commit();
            return true;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Log.d("TunerActivity", "Problem launching fragment", e);
            return false;
        }
    }

    private boolean startPreferenceScreen(PreferenceFragment caller, String key, boolean backStack) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SubSettingsFragment fragment = new SubSettingsFragment();
        final Bundle b = new Bundle(1);
        b.putString(PreferenceFragment.ARG_PREFERENCE_ROOT, key);
        fragment.setArguments(b);
        fragment.setTargetFragment(caller, 0);
        transaction.replace(R.id.container, fragment);
        if (backStack) {
            transaction.addToBackStack("PreferenceFragment");
        }
        transaction.commit();

        return true;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
        return startPreferenceScreen(caller, pref.getKey(), true);
    }

    public static class SubSettingsFragment extends ToolboxPreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceScreen p = (PreferenceScreen) ((PreferenceFragment) getTargetFragment())
                    .getPreferenceScreen().findPreference(rootKey);
            setPreferenceScreen(p);
            getActivity().getActionBar().setTitle(p.getTitle());
        }
    }
}
