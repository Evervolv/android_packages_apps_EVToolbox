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

package com.evervolv.toolbox;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.evervolv.toolbox.categories.*;
import com.evervolv.toolbox.utils.*;

import java.util.ArrayList;
import java.util.List;

public class Toolbox extends AppCompatActivity {

    private Context mContext;
    private List<DisabledListener> mCallbacks = new ArrayList<DisabledListener>();

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    /**
     * Implemented by child preferences affected by DISABLE_TOOLBOX
     */
    public interface DisabledListener {
        public void onToolboxDisabled(boolean enabled);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbox);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = this;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(mNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            // Default options
            mDrawerLayout.openDrawer(GravityCompat.START);
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new InterfaceCategory()).commit();
        }
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_interface) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new InterfaceCategory()).commit();
        } else if (id == R.id.nav_statusbar) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new StatusbarCategory()).commit();
        } else if (id == R.id.nav_system) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new SystemCategory()).commit();
        } else if (id == R.id.nav_performance) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new PerformanceCategory()).commit();
        } else if (id == R.id.nav_bugreport) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new BugReport()).commit();
        } else if (id == R.id.nav_settings) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new ToolboxSettings()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Register self for DisabledListener interface
     */
    public void registerCallback(DisabledListener cb) {
        mCallbacks.add(cb);
    }

    /**
     * UnRegister self for DisabledListener interface
     */
    public void unRegisterCallback(DisabledListener cb) {
        mCallbacks.remove(cb);
    }

    /**
     * Inform children of state change
     */
    public void updateListeners(boolean isChecked) {
        for (DisabledListener cb: mCallbacks) {
            cb.onToolboxDisabled(isChecked);
        }
    }

    /**
     * Checks if toolbox is enabled
     */
    public static boolean isEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.DISABLE_TOOLBOX, 0) != 1;
    }
}
