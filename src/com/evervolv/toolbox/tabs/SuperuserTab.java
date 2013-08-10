package com.evervolv.toolbox.tabs;

import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evervolv.toolbox.custom.PagerFragment;
import com.evervolv.toolbox.fragments.SuperuserLogs;
import com.evervolv.toolbox.fragments.SuperuserPolicy;

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
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), container.getId());

        mTabHost.addTab(mTabHost.newTabSpec("policy").setIndicator("Policy"),
                SuperuserPolicy.class, null);
        //mTabHost.addTab(mTabHost.newTabSpec("logs").setIndicator("Logs"),
        //        SuperuserLogs.class, null);
        mTabHost.setOnTabChangedListener(this);
        return mTabHost;
    }
}
