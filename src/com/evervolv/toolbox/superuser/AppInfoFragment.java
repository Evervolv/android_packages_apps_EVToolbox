package com.evervolv.toolbox.superuser;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.superuser.db.LogEntry;
import com.evervolv.toolbox.superuser.db.SuDatabaseHelper;
import com.evervolv.toolbox.superuser.db.SuperuserDatabaseHelper;
import com.evervolv.toolbox.superuser.db.UidPolicy;

import java.util.ArrayList;
import java.util.List;

public class AppInfoFragment extends Fragment {

    private Context mContext;
    private SuperuserAppActivity mActivity;
    private Resources mRes;
    private UidPolicy mCurrPolicy;

    private ImageView mAppIcon;
    private ImageView mPolicyIcon;
    private TextView mAppName;
    private TextView mAppId;
    private TextView mAppPackage;
    private TextView mAppRequestedUuid;
    private TextView mAppCommand;
    private Switch mAppEnableLogging;
    private Switch mAppEnableNotifications;
    private ListView mAppLogList;

    public AppInfoFragment() { }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        mContext = getActivity().getApplicationContext();
        mActivity = (SuperuserAppActivity) getActivity();
        mRes = mContext.getResources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.superuser_fragment_info, container, false);

        mAppIcon = (ImageView) v.findViewById(R.id.info_app_icon);
        mPolicyIcon = (ImageView) v.findViewById(R.id.info_policy_icon);
        mAppName = (TextView) v.findViewById(R.id.info_app_name);
        mAppId = (TextView) v.findViewById(R.id.info_app_id);
        mAppPackage = (TextView) v.findViewById(R.id.info_app_package);
        mAppRequestedUuid = (TextView) v.findViewById(R.id.info_app_requested_uuid);
        mAppCommand = (TextView) v.findViewById(R.id.info_app_command);
        mAppLogList = (ListView) v.findViewById(R.id.log_list);

        mAppEnableLogging = (Switch) v.findViewById(R.id.info_app_logging);
        mAppEnableLogging.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                mCurrPolicy.logging = isChecked;
                SuDatabaseHelper.setPolicy(mContext, mCurrPolicy);
            }
        });

        mAppEnableNotifications = (Switch) v.findViewById(R.id.info_app_notification);
        mAppEnableNotifications.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                mCurrPolicy.notification = isChecked;
                SuDatabaseHelper.setPolicy(mContext, mCurrPolicy);
            }
        });

        //Get the first policy
        if (mCurrPolicy == null) {
            UidPolicy firstPolicy = SuDatabaseHelper.getPolicy(mContext, 0);
            if (firstPolicy != null) {
                setPolicy(firstPolicy);
            }
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.superuser_app_policy_info_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete_policy) {
            deleteCurrentPolicy();
            return true;
        }
        return false;
    }

    public void deleteCurrentPolicy() {
        AppListFragment frag = (AppListFragment) getFragmentManager()
                .findFragmentById(R.id.list_pane);
        frag.deletePolicy(mCurrPolicy);
        mActivity.togglePane();
    }

    public void setPolicy(UidPolicy policy) {
        mCurrPolicy = policy;

        ArrayList<LogEntry> logs;
        logs = SuperuserDatabaseHelper.getLogs(getActivity(), policy, -1);
        mAppLogList.setAdapter(new PolicyLogAdapter(getActivity(),
                R.layout.superuser_policy_log_list_item, logs));

        mAppIcon.setImageDrawable(Helper.loadPackageIcon(mContext, policy.packageName));
        mPolicyIcon.setAlpha(0.5f);
        if (policy.policy.equals(UidPolicy.ALLOW)) {
            mPolicyIcon.setImageResource(R.drawable.ic_allowed);
        } else if (policy.policy.equals(UidPolicy.DENY)) {
            mPolicyIcon.setImageResource(R.drawable.ic_denied);
        } else {
            mPolicyIcon.setImageResource(0);
        }

        mAppName.setText(policy.name);
        mAppId.setText(Integer.toString(policy.uid));
        mAppPackage.setText(policy.packageName);
        mAppRequestedUuid.setText(Integer.toString(policy.desiredUid));
        mAppCommand.setText(TextUtils.isEmpty(policy.command) ? mRes.getString(
                R.string.superuser_app_all_commands) : policy.command);
        mAppEnableLogging.setChecked(policy.logging);
        mAppEnableNotifications.setChecked(policy.notification);
    }

    public String getCurrentPolicyName() {
        if (mCurrPolicy == null) {
            //TODO: Should we do this different?
            //      Maybe a default bogus policy?
            return "";
        }
        return mCurrPolicy.name;
    }

    public class PolicyLogAdapter extends ArrayAdapter<LogEntry> {

        private Context mContext;

        public PolicyLogAdapter(Context context, int resource,
                List<LogEntry> objects) {
            super(context, resource, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.superuser_policy_log_list_item,
                    parent, false);

            TextView logDate = (TextView) v.findViewById(R.id.policy_log_date);
            TextView logAction = (TextView) v.findViewById(R.id.policy_log_action);

            LogEntry log = getItem(position);
            java.text.DateFormat time = DateFormat.getTimeFormat(getActivity());
            java.text.DateFormat day = DateFormat.getDateFormat(getActivity());
            String dateTime = (day.format(log.getDate()) + " - " +
                    time.format(log.getDate()));

            logDate.setText(dateTime);
            logAction.setText(log.getActionResource());

            return v;
        }

    }

}
