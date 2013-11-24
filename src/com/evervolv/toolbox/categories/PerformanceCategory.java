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

package com.evervolv.toolbox.categories;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.fragments.PerformanceMemory;
import com.evervolv.toolbox.fragments.PerformanceProcessor;

public class PerformanceCategory extends CategoryFragment {

    private RelativeLayout mBottomActionBar;
    private TextView mBottomBarText;

    public PerformanceCategory() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategoryAdapter.setPageTitles(getResources().getStringArray(R.array.performance_nav));
        mCategoryAdapter.addFragment(new PerformanceProcessor());
        mCategoryAdapter.addFragment(new PerformanceMemory());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mBottomActionBar = (RelativeLayout) v.findViewById(R.id.bottom_action_bar);
        mBottomBarText = (TextView) v.findViewById(R.id.boot_text);
        mBottomBarText.setText("Restore performance settings on boot");
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext());

        Switch bootSwitch = (Switch) v.findViewById(R.id.boot_switch);
        bootSwitch.setChecked(prefs.getBoolean(PerformanceMemory.SOB_PREF, false));
        bootSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                prefs.edit().putBoolean(PerformanceMemory.SOB_PREF,
                        isChecked).commit();
            }
        });
        mBottomActionBar.setVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(getResources().getString(R.string.tab_title_performance));
    }
}
