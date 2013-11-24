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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.fragments.SuperuserSecurity;
import com.evervolv.toolbox.fragments.SuperuserSettings;
import com.evervolv.toolbox.superuser.SuperuserAppActivity;
import com.evervolv.toolbox.superuser.SuperuserLogsActivity;

public class SuperuserCategory extends CategoryFragment {

    public SuperuserCategory() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCategoryAdapter.setPageTitles(getResources().getStringArray(R.array.superuser_nav));
        mCategoryAdapter.addFragment(new SuperuserSecurity());
        mCategoryAdapter.addFragment(new SuperuserSettings());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(getResources().getString(R.string.tab_title_statusbar));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.superuser_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_app_policies:
                Intent applist = new Intent(getActivity(), SuperuserAppActivity.class);
                startActivity(applist);
                return true;
            case R.id.menu_logs:
                Intent logs = new Intent(getActivity(), SuperuserLogsActivity.class);
                startActivity(logs);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
