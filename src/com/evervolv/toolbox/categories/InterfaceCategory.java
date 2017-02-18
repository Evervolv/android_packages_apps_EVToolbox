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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.ToolboxCategory;
import com.evervolv.toolbox.fragments.InterfacePowerMenu;
import com.evervolv.toolbox.fragments.InterfaceRotation;

public class InterfaceCategory extends ToolboxCategory {

    public InterfaceCategory() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategoryAdapter.setPageTitles(getResources().getStringArray(R.array.interface_nav));
        mCategoryAdapter.addFragment(new InterfaceRotation());
        mCategoryAdapter.addFragment(new InterfacePowerMenu());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(
                getResources().getString(R.string.tab_title_interface));
    }
}
