package com.evervolv.toolbox.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.custom.PagerFragment;
import com.evervolv.toolbox.fragments.SuperuserSecurity;
import com.evervolv.toolbox.fragments.SuperuserSettings;
import com.evervolv.toolbox.superuser.SuperuserAppActivity;
import com.evervolv.toolbox.superuser.SuperuserLogsActivity;

public class SuperuserTab extends PagerFragment {

    public SuperuserTab() {
        super();
    }

    public SuperuserTab(String title, int pageIndex) {
        super(title, pageIndex);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), container.getId());

        mTabHost.addTab(mTabHost.newTabSpec("security").setIndicator("Security"),
                SuperuserSecurity.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("settings").setIndicator("Settings"),
                SuperuserSettings.class, null);
        mTabHost.setOnTabChangedListener(this);
        return mTabHost;
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
