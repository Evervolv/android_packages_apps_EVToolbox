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
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evervolv.toolbox.R;

import java.util.ArrayList;

public class ToolboxCategory extends Fragment {

    protected ViewPager mViewPager;
    protected CategoryAdapter mCategoryAdapter;

    public ToolboxCategory() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pager fragments are nested (children of this fragment)
        mCategoryAdapter = new CategoryAdapter(getChildFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_pager, container, false);
        mViewPager = (ViewPager) v.findViewById(R.id.pager_frame);
        mViewPager.setAdapter(mCategoryAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected class CategoryAdapter extends FragmentPagerAdapter {

        private final ArrayList<Fragment> mFragmentList = new ArrayList<Fragment>();
        private final ArrayList<String> mTitleList = new ArrayList<String>();

        public CategoryAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public void setPageTitles(String[] titles) {
            for (String title: titles) {
                mTitleList.add(title);
            }
        }

        public void addPageTitle(String title) {
            mTitleList.add(title);
        }

        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

    }

}
