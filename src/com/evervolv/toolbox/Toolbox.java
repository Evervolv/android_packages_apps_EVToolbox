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

import android.content.Context;
import android.provider.Settings;

import com.evervolv.toolbox.ToolboxActivity;

import java.util.ArrayList;
import java.util.List;

public class Toolbox extends ToolboxActivity {

    private List<DisabledListener> mCallbacks = new ArrayList<DisabledListener>();

    /**
     * Implemented by child preferences affected by DISABLE_TOOLBOX
     */
    public interface DisabledListener {
        public void onToolboxDisabled(boolean enabled);
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
     * Inform children of state change
     */
    public void updateListeners(boolean isChecked) {
        for (DisabledListener cb: mCallbacks) {
            cb.onToolboxDisabled(isChecked);
        }
    }

    /**
     * Checks if toolbox is enabled
     */
    public static boolean isEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.DISABLE_TOOLBOX, 0) != 1;
    }
}
