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
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;

import com.evervolv.toolbox.categories.InterfaceCategory;
import com.evervolv.toolbox.categories.LockscreenCategory;
import com.evervolv.toolbox.categories.PerformanceCategory;
import com.evervolv.toolbox.categories.StatusbarCategory;
import com.evervolv.toolbox.categories.SuperuserCategory;
import com.evervolv.toolbox.categories.SystemCategory;
import com.evervolv.toolbox.custom.DrawerLayoutAdapter;
import com.evervolv.toolbox.fragments.BugReport;

import java.util.ArrayList;
import java.util.List;

public class Toolbox extends Activity {

    private Context mContext;
    private List<DisabledListener> mCallbacks = new ArrayList<DisabledListener>();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayoutAdapter mDrawerAdapter;

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

        mContext = this;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerAdapter = new DrawerLayoutAdapter(this);

        String[] navItems = getResources().getStringArray(R.array.drawer_tabs);
        for (String title: navItems) {
            /* Don't assign icons just yet for nav items
             * there's a little bit more we need to do to make this
             * a little cleaner and more efficient
             */
            mDrawerAdapter.addNavItem(title, -1);
        }

        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                mDrawerLayout.closeDrawer(mDrawerList);
                navigateCategory(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
                ) {
            final CharSequence drawerTitle = getResources().getString(R.string.app_name);
            CharSequence fragmentTitle;

            public void onDrawerClosed(View view) {
                if (fragmentTitle != null &&
                        // Fragments set title so if it changed, don't replace
                        getActionBar().getTitle().equals(drawerTitle)) {
                    getActionBar().setTitle(fragmentTitle);
                }
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                // Save old title in case we don't switch fragments
                fragmentTitle = getActionBar().getTitle();
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.openDrawer(mDrawerList);

        final ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setTitle(R.string.app_name);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbox_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_switch);

        Switch toolboxSwitch = (Switch) item.getActionView();
        toolboxSwitch.setChecked(isEnabled(mContext));
        toolboxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.DISABLE_TOOLBOX, isChecked ? 0 : 1);
                // Inform children of state change
                for (DisabledListener cb: mCallbacks) {
                    cb.onToolboxDisabled(isChecked);
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // TODO clean this up
    private void navigateCategory(int position) {
        Log.d("Toolbox", "Selected item=" + position);
        switch (position) {
            case 0:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new InterfaceCategory()).commit();
                break;
            case 1:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new LockscreenCategory()).commit();
                break;
            case 2:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new PerformanceCategory()).commit();
                break;
            case 3:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new StatusbarCategory()).commit();
                break;
            case 4:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new SuperuserCategory()).commit();
                break;
            case 5:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new SystemCategory()).commit();
                break;
            case 6:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new BugReport()).commit();
                break;
        }
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
     * Checks if toolbox is enabled
     */
    public static boolean isEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.DISABLE_TOOLBOX, 0) != 1;
    }

}
