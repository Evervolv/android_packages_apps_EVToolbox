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

package com.evervolv.toolbox.custom;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.widget.TabHost.OnTabChangeListener;

public class PagerFragment extends Fragment implements OnTabChangeListener {

    protected FragmentTabHost mTabHost;
    protected String mTitle;
    protected OnTabChangeListener mCallback;
    protected int mPageIndex;

    public interface OnTabChangeListener {
        public void onTabChanged(int page, int tab);
    }

    public PagerFragment() {
        super();
    }

    public PagerFragment(String title, int pageIndex) {
        mTitle = title;
        mPageIndex = pageIndex;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnTabChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTabChangeListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

    public void tabScrollTo(int position) {
        if (mTabHost != null) {
            mTabHost.setCurrentTab(position);
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public int getCurrentTab() {
        if (mTabHost != null) {
            return mTabHost.getCurrentTab();
        }
        return -1;
    }

    @Override
    public void onTabChanged(String tabId) {
        mCallback.onTabChanged(mPageIndex, mTabHost.getCurrentTab());
    }

}
