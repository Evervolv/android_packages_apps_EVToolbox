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

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.evervolv.toolbox.categories.HardwareCategory;
import com.evervolv.toolbox.categories.InterfaceCategory;
import com.evervolv.toolbox.categories.PerformanceCategory;
import com.evervolv.toolbox.categories.StatusbarCategory;
import com.evervolv.toolbox.categories.SystemCategory;
import com.evervolv.toolbox.support.BugReport;
import com.evervolv.toolbox.support.Preferences;

public class ToolboxActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

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
        } else if (id == R.id.nav_hardware) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new HardwareCategory()).commit();
        } else if (id == R.id.nav_performance) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new PerformanceCategory()).commit();
        } else if (id == R.id.nav_bugreport) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new BugReport()).commit();
        } else if (id == R.id.nav_settings) {
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new Preferences()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
