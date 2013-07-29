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

package com.evervolv.toolbox.tabs;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v13.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evervolv.toolbox.fragments.InterfaceMain;
import com.evervolv.toolbox.fragments.InterfacePowerMenu;
import com.evervolv.toolbox.fragments.InterfaceRotation;

public class InterfaceTab extends PreferenceFragment {

    private FragmentTabHost mTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), container.getId());

        mTabHost.addTab(mTabHost.newTabSpec("main").setIndicator("Main"),
                InterfaceMain.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("rotation").setIndicator("Rotation"),
                InterfaceRotation.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("power_menu").setIndicator("Power menu"),
                InterfacePowerMenu.class, null);

        return mTabHost;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

}
