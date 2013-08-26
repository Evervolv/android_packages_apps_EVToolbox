package com.evervolv.toolbox.superuser;

import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.superuser.db.LogEntry;
import com.evervolv.toolbox.superuser.db.SuDatabaseHelper;
import com.evervolv.toolbox.superuser.db.SuperuserDatabaseHelper;
import com.evervolv.toolbox.superuser.db.UidPolicy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppListFragment extends Fragment {

    private Context mContext;
    private SuperuserAppActivity mActivity;

    private AppPolicyAdapter mAppListAdapter;
    private List<AppPolicy> mAppList = new ArrayList<AppPolicy>();
    private ListView mAppListView;
    
    public AppListFragment() {
        //Nothing yet
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        mContext = getActivity();
        mActivity = (SuperuserAppActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View v = inflater.inflate(R.layout.superuser_fragment_apps, container, false);
        mAppListView = (ListView) v.findViewById(R.id.app_list);
        loadApps();
        mAppListAdapter = new AppPolicyAdapter(mContext, R.layout.superuser_app_list_item, mAppList);
        mAppListView.setAdapter(mAppListAdapter);
        if (!mAppList.isEmpty()) {
            mAppListView.setSelection(0);
        }
        mAppListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                showPolicy(position, true);
            }
        });


        return v;
    }

    private void loadApps() {
        final ArrayList<UidPolicy> policies = SuDatabaseHelper
                .getPolicies(mContext);
        SQLiteDatabase db = new SuperuserDatabaseHelper(mContext)
                .getReadableDatabase();

        try {
            for (UidPolicy up: policies) {
                int last = 0;
                java.text.DateFormat df = DateFormat.getLongDateFormat(mContext);
                String date;
                ArrayList<LogEntry> logs = SuperuserDatabaseHelper.getLogs(db, up, 1);
                if (logs.size() > 0) {
                    last = logs.get(0).date;
                }

                if (last == 0) {
                    date = null;
                } else {
                    date = df.format(new Date((long)last * 1000));
                    mAppList.add(new AppPolicy(up, date));
                }
            }
        } finally {
            db.close();
        }
    }

    private void showPolicy(int index, boolean show) {
        AppInfoFragment frag = (AppInfoFragment) getFragmentManager()
                .findFragmentById(R.id.info_pane);
        frag.setPolicy(mAppList.get(index).getPolicy());
        if (show) {
            mActivity.togglePane();
        }
    }

    public void deletePolicy(UidPolicy policy) {
        AppPolicy removeWhich = null;
        for (AppPolicy list : mAppList) {
            if (list.getPolicy() == policy) {
                removeWhich = list;
                SuDatabaseHelper.delete(mContext, policy);
                mAppListAdapter.notifyDataSetChanged();
                /* Set info to the first app policy, but don't show it */
                if (!mAppList.isEmpty()) {
                    showPolicy(0, false);
                }
            }
        }
        if (removeWhich != null) {
            mAppList.remove(removeWhich);
        }
    }

    public class AppPolicyAdapter extends ArrayAdapter<AppPolicy> {

        private Context mContext;

        public AppPolicyAdapter(Context context, int resource,
                List<AppPolicy> objects) {
            super(context, resource, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.superuser_app_list_item,
                        parent, false);
                holder = new ViewHolder();
                holder.appIcon = (ImageView) v.findViewById(R.id.app_icon);
                holder.appName = (TextView) v.findViewById(R.id.app_name);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            AppPolicy app = getItem(position);
            holder.appName.setText(app.getPolicy().name);

            Drawable icon = Helper.loadIcon(mContext, app.getPolicy().packageName);
            if (icon != null) {
                holder.appIcon.setImageDrawable(icon);
            } else {
                holder.appIcon.setImageResource(R.drawable.ic_launcher_toolbox);
            }

            return v;
        }
    }

    public static class ViewHolder {
        public ImageView appIcon;
        public TextView appName;
    }

    public class AppPolicy {
        private UidPolicy mPolicy;
        private String mDate;

        public AppPolicy(UidPolicy policy, String date) {
            mPolicy = policy;
            mDate = date;
        }

        public UidPolicy getPolicy() { return mPolicy; }
        public String getDate() { return mDate; }
    }

}
