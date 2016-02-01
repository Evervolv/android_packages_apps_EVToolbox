/*
 * Copyright (C) 2014 The Evervolv Project
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

import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.fragments.*;


public class DeviceCategory extends CategoryFragment implements Toolbox.DisabledListener {

    private static final String TAG ="DeviceCategory";

    private static final ArrayMap<Integer, DeviceSettingFragment> mClassMap = new ArrayMap<Integer, DeviceSettingFragment>();

    /*
     * List of possible fragments
     */
    private static final String[] CLASS_LIST = {
        "DeviceTouchscreen",
        "DeviceHaptic"
    };

    private static final int[] CLASS_NAME_IDS = {
        R.string.device_touchscreen_title,
        R.string.device_haptic_title
    };

    static {
        for (int i = 0 ; i < CLASS_LIST.length; i++) {
            Class<?> clazz;
            try {
                clazz = Class.forName("com.evervolv.toolbox.fragments." + CLASS_LIST[i]);
            } catch( ClassNotFoundException e ) {
                continue;
            }
            try {
                mClassMap.put(CLASS_NAME_IDS[i], (DeviceSettingFragment) clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public DeviceCategory() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (Map.Entry<Integer, DeviceSettingFragment> entry : mClassMap.entrySet()) {
            Integer id = entry.getKey();
            DeviceSettingFragment fragment = entry.getValue();
            mCategoryAdapter.addFragment(fragment);
            mCategoryAdapter.addPageTitle(getString(id));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(R.string.device_settings_title);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((Toolbox) getActivity()).registerCallback(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((Toolbox) getActivity()).unRegisterCallback(this);
    }

    /*
     * Used for restoring settings on boot
     */
    public static void restoreSettings(Context context) {
        for (Map.Entry<Integer, DeviceSettingFragment> entry : mClassMap.entrySet()) {
            DeviceSettingFragment fragment = entry.getValue();
            fragment.restore(context, Toolbox.isEnabled(context));
        }
    }

    public static boolean areClassesPresent(Context context) {
        return !mClassMap.isEmpty();
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        restoreSettings(getActivity());
    }

}
