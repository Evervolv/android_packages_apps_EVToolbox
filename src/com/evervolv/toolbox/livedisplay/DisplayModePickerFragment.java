/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.evervolv.toolbox.livedisplay;

import android.content.Context;
import android.hardware.display.ColorDisplayManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import evervolv.hardware.DisplayMode;
import evervolv.hardware.LiveDisplayManager;

import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.widget.SelectorWithWidgetPreference;
import com.evervolv.internal.util.ResourceUtils;
import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

public class DisplayModePickerFragment extends SettingsPreferenceFragment implements
        SelectorWithWidgetPreference.OnClickListener {

    private static final String COLOR_PROFILE_TITLE = "live_display_color_profile_%s_title";
    private static final String COLOR_PROFILE = "color_profile_";

    static final String PAGE_VIEWER_SELECTION_INDEX = "page_viewer_selection_index";

    private static final int DOT_INDICATOR_SIZE = 12;
    private static final int DOT_INDICATOR_LEFT_PADDING = 6;
    private static final int DOT_INDICATOR_RIGHT_PADDING = 6;

    private LiveDisplayManager mLiveDisplay;

    private View mViewArrowPrevious;
    private View mViewArrowNext;
    private ViewPager mViewPager;

    private ArrayList<View> mPageList;

    private ImageView[] mDotIndicators;
    private View[] mViewPagerImages;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.display_mode_settings);

        final PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        Context context = screen.getContext();
        final LayoutPreference preview = new LayoutPreference(context,
                R.layout.color_mode_preview);
        preview.setSelectable(false);
        screen.addPreference(preview);
        addViewPager(preview);

        mLiveDisplay = LiveDisplayManager.getInstance(context);

        final DisplayMode[] modes = mLiveDisplay.getDisplayModes();
        if (modes != null && modes.length > 0) {
            for (int i = 0; i < modes.length; i++) {
                SelectorWithWidgetPreference pref = new SelectorWithWidgetPreference(context);
                bindPreference(pref, modes[i]);
                screen.addPreference(pref);
            }
        }
        mayCheckOnlyRadioButton();

        if (savedInstanceState != null) {
            final int selectedPosition = savedInstanceState.getInt(PAGE_VIEWER_SELECTION_INDEX);
            mViewPager.setCurrentItem(selectedPosition);
            updateIndicator(selectedPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_VIEWER_SELECTION_INDEX, mViewPager.getCurrentItem());
    }

    @VisibleForTesting
    public ArrayList<Integer> getViewPagerResource() {
        return new ArrayList<Integer>(
                Arrays.asList(
                        R.layout.color_mode_view1,
                        R.layout.color_mode_view2,
                        R.layout.color_mode_view3));
    }

    void addViewPager(LayoutPreference preview) {
        final ArrayList<Integer> tmpviewPagerList = getViewPagerResource();
        mViewPager = preview.findViewById(R.id.viewpager);

        mViewPagerImages = new View[3];
        for (int idx = 0; idx < tmpviewPagerList.size(); idx++) {
            mViewPagerImages[idx] =
                    getLayoutInflater().inflate(tmpviewPagerList.get(idx), null /* root */);
        }

        mPageList = new ArrayList<View>();
        mPageList.add(mViewPagerImages[0]);
        mPageList.add(mViewPagerImages[1]);
        mPageList.add(mViewPagerImages[2]);

        mViewPager.setAdapter(new ColorPagerAdapter(mPageList));

        mViewArrowPrevious = preview.findViewById(R.id.arrow_previous);
        mViewArrowPrevious.setOnClickListener(v -> {
            final int previousPos = mViewPager.getCurrentItem() - 1;
            mViewPager.setCurrentItem(previousPos, true);
        });

        mViewArrowNext = preview.findViewById(R.id.arrow_next);
        mViewArrowNext.setOnClickListener(v -> {
            final int nextPos = mViewPager.getCurrentItem() + 1;
            mViewPager.setCurrentItem(nextPos, true);
        });

        mViewPager.addOnPageChangeListener(createPageListener());

        final ViewGroup viewGroup = (ViewGroup) preview.findViewById(R.id.viewGroup);
        mDotIndicators = new ImageView[mPageList.size()];
        for (int i = 0; i < mPageList.size(); i++) {
            final ImageView imageView = new ImageView(getContext());
            final ViewGroup.MarginLayoutParams lp =
                    new ViewGroup.MarginLayoutParams(DOT_INDICATOR_SIZE, DOT_INDICATOR_SIZE);
            lp.setMargins(DOT_INDICATOR_LEFT_PADDING, 0, DOT_INDICATOR_RIGHT_PADDING, 0);
            imageView.setLayoutParams(lp);
            mDotIndicators[i] = imageView;

            viewGroup.addView(mDotIndicators[i]);
        }

        updateIndicator(mViewPager.getCurrentItem());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference selected) {
        final String selectedKey = selected.getKey();
        if (selectedKey.startsWith(COLOR_PROFILE)) {
            String modeId = selectedKey.replaceFirst(COLOR_PROFILE, "");
            for (DisplayMode mode : mLiveDisplay.getDisplayModes()) {
                if (mode.id == Integer.valueOf(modeId)) {
                    mLiveDisplay.setDisplayMode(mode, true);
                    updateCheckedState(selectedKey);
                }
            }
        }
    }

    private SelectorWithWidgetPreference bindPreference(SelectorWithWidgetPreference pref, DisplayMode mode) {
        final DisplayMode defaultMode = mLiveDisplay.getCurrentDisplayMode() != null
                    ? mLiveDisplay.getCurrentDisplayMode() : mLiveDisplay.getDefaultDisplayMode();
        pref.setTitle(ResourceUtils.getLocalizedString(
                    getResources(), mode.name, COLOR_PROFILE_TITLE));
        pref.setKey(COLOR_PROFILE + mode.id);
        if (mode.id == defaultMode.id) {
            pref.setChecked(true);
        }
        pref.setOnClickListener(this);
        return pref;
    }

    private void updateCheckedState(String selectedKey) {
        final PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            final int count = screen.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                final Preference pref = screen.getPreference(i);
                if (pref instanceof SelectorWithWidgetPreference) {
                    final SelectorWithWidgetPreference radioPref = (SelectorWithWidgetPreference) pref;
                    final boolean newCheckedState = TextUtils.equals(pref.getKey(), selectedKey);
                    if (radioPref.isChecked() != newCheckedState) {
                        radioPref.setChecked(TextUtils.equals(pref.getKey(), selectedKey));
                    }
                }
            }
        }
    }

    private void mayCheckOnlyRadioButton() {
        final PreferenceScreen screen = getPreferenceScreen();
        // If there is only 1 thing on screen, select it.
        if (screen != null && screen.getPreferenceCount() == 1) {
            final Preference onlyPref = screen.getPreference(0);
            if (onlyPref instanceof SelectorWithWidgetPreference) {
                ((SelectorWithWidgetPreference) onlyPref).setChecked(true);
            }
        }
    }

    private ViewPager.OnPageChangeListener createPageListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                    int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset != 0) {
                    for (int idx = 0; idx < mPageList.size(); idx++) {
                        mViewPagerImages[idx].setVisibility(View.VISIBLE);
                    }
                } else {
                    mViewPagerImages[position].setContentDescription(
                            getContext().getString(R.string.live_display_color_profile_preview));
                    updateIndicator(position);
                }
            }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        };
    }

    private void updateIndicator(int position) {
        for (int i = 0; i < mPageList.size(); i++) {
            if (position == i) {
                mDotIndicators[i].setBackgroundResource(
                        R.drawable.ic_color_page_indicator_focused);

                mViewPagerImages[i].setVisibility(View.VISIBLE);
            } else {
                mDotIndicators[i].setBackgroundResource(
                        R.drawable.ic_color_page_indicator_unfocused);

                mViewPagerImages[i].setVisibility(View.INVISIBLE);
            }
        }

        if (position == 0) {
            mViewArrowPrevious.setVisibility(View.INVISIBLE);
            mViewArrowNext.setVisibility(View.VISIBLE);
        } else if (position == (mPageList.size() - 1)) {
            mViewArrowPrevious.setVisibility(View.VISIBLE);
            mViewArrowNext.setVisibility(View.INVISIBLE);
        } else {
            mViewArrowPrevious.setVisibility(View.VISIBLE);
            mViewArrowNext.setVisibility(View.VISIBLE);
        }
    }

    static class ColorPagerAdapter extends PagerAdapter {
        private final ArrayList<View> mPageViewList;

        ColorPagerAdapter(ArrayList<View> pageViewList) {
            mPageViewList = pageViewList;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (mPageViewList.get(position) != null) {
                container.removeView(mPageViewList.get(position));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mPageViewList.get(position));
            return mPageViewList.get(position);
        }

        @Override
        public int getCount() {
            return mPageViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object == view;
        }
    }
}
