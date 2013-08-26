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
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.custom.DrawerLayoutAdapter;
import com.evervolv.toolbox.custom.PagerFragment;
import com.evervolv.toolbox.fragments.PerformanceMemory;
import com.evervolv.toolbox.tabs.InterfaceTab;
import com.evervolv.toolbox.tabs.LockscreenTab;
import com.evervolv.toolbox.tabs.PerformanceTab;
import com.evervolv.toolbox.tabs.StatusbarTab;
import com.evervolv.toolbox.tabs.SuperuserTab;

import java.util.ArrayList;

public class Toolbox extends FragmentActivity implements PagerFragment.OnTabChangeListener {

    private ViewPager mViewPager;
    private MainSettingsAdapter mSettingsAdapter;
    private ContentResolver mCr;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayoutAdapter mDrawerAdapter;
    private RelativeLayout mBottomActionBar;
    private ArrayList<int[]> mPositionsList = new ArrayList<int[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbox);

        mCr = getApplicationContext().getContentResolver();
        mViewPager = (ViewPager) findViewById(R.id.pager_frame);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerAdapter = new DrawerLayoutAdapter(this);

        TypedArray bundles = getResources().obtainTypedArray(R.array.nav_bundles);
        int count = bundles.length();

        for (int i = 0; i < count; i++) {
            int bundleId = bundles.getResourceId(i, -1);
            String[] navItems = getResources().getStringArray(bundleId);
            for (int j = 0; j < navItems.length; j++) {
                //assume first entry is the header
                if (j == 0) {
                    mDrawerAdapter.addHeader(navItems[j], R.drawable.ic_header);
                } else {
                    /* TODO: Don't assign icons just yet for nav items
                     * there's a little bit more we need to do to make this
                     * a little cleaner and more efficient
                     */
                    mDrawerAdapter.addNavItem(navItems[j], -1);
                }
                mPositionsList.add(new int[]{i, j});
            }
        }

        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                mDrawerLayout.closeDrawer(mDrawerList);
                setTab(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.openDrawer(mDrawerList);

        final ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE,
                ActionBar.DISPLAY_SHOW_TITLE);
        bar.setTitle(R.string.app_name);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mSettingsAdapter = new MainSettingsAdapter(this);
        mSettingsAdapter.addFragment(new LockscreenTab(getResources().getString(
                R.string.tab_title_lockscreen), mSettingsAdapter.getCount()));
        mSettingsAdapter.addFragment(new InterfaceTab(getResources().getString(
                R.string.tab_title_interface), mSettingsAdapter.getCount()));
        mSettingsAdapter.addFragment(new StatusbarTab(getResources().getString(
                R.string.tab_title_statusbar), mSettingsAdapter.getCount()));
        mSettingsAdapter.addFragment(new PerformanceTab(getResources().getString(
                R.string.tab_title_performance), mSettingsAdapter.getCount()));
        mSettingsAdapter.addFragment(new SuperuserTab(getResources().getString(
                R.string.tab_title_superuser), mSettingsAdapter.getCount()));

        mBottomActionBar = (RelativeLayout) findViewById(R.id.bottom_action_bar);
        TextView bottomTextView = (TextView) findViewById(R.id.boot_text);
        bottomTextView.setText("Restore performance settings on boot");

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        Switch bootSwitch = (Switch) findViewById(R.id.boot_switch);
        bootSwitch.setChecked((prefs.getBoolean(PerformanceMemory.SOB_PREF,
                false) == true));
        bootSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                prefs.edit().putBoolean(PerformanceMemory.SOB_PREF,
                        isChecked).commit();
            }
        });

        mViewPager.setAdapter(mSettingsAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) { }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) { }

            @Override
            public void onPageSelected(int position) {
                if (mSettingsAdapter.getItem(position).getClass()
                        == PerformanceTab.class) {
                    mBottomActionBar.setVisibility(View.VISIBLE);
                } else {
                    mBottomActionBar.setVisibility(View.GONE);
                }

                mDrawerList.setItemChecked(getDrawerItem(position,
                        mSettingsAdapter.getItem(position).getCurrentTab()), true);
            }
        });
    }

    private int getDrawerItem(int pagePosition, int tabPosition) {
        for (int i = 0; i < mPositionsList.size(); i++) {
            int[] item = mPositionsList.get(i);
            if (item[0] == pagePosition && item[1] == (tabPosition + 1)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .cancelAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbox_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_switch);

        Switch toolboxSwitch = (Switch) item.getActionView();
        toolboxSwitch.setChecked(Settings.System.getInt(mCr,
                Settings.System.DISABLE_TOOLBOX, 0) == 0);
        toolboxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                /* TODO: Make the ViewPager disabled ( grey'd out, disabled )
                 *  Most likely requires a custom class.
                 */     
                Settings.System.putInt(mCr, Settings.System.DISABLE_TOOLBOX,
                        isChecked ? 0 : 1);
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

    private void setTab(int position) {
        int[] list = mPositionsList.get(position);
        mViewPager.setCurrentItem(list[0]);
        PagerFragment child = mSettingsAdapter.getItem(list[0]);
        child.tabScrollTo(list[1]-1);
    }

    private class MainSettingsAdapter extends FragmentPagerAdapter {

        private final ArrayList<PagerFragment> mFragmentList = new ArrayList<PagerFragment>();

        public MainSettingsAdapter(Activity activity) {
            super(activity.getFragmentManager());
        }

        public void addFragment(PagerFragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public PagerFragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentList.get(position).getTitle();
        }
    }

    @Override
    public void onTabChanged(int pagePosition, int tabPosition) {
        mDrawerList.setItemChecked(getDrawerItem(pagePosition,
                tabPosition), true);
    }

}
