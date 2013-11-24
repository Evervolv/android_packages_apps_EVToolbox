package com.evervolv.toolbox.superuser;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.superuser.db.LogEntry;
import com.evervolv.toolbox.superuser.db.SuperuserDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SuperuserLogsActivity extends ListActivity {
    
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setHomeButtonEnabled(true);
        mContext = this;
        ArrayList<LogEntry> logs = SuperuserDatabaseHelper.getLogs(mContext);
        setListAdapter(new LogAdapter(mContext,
                R.layout.superuser_log_list_item, logs));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class LogAdapter extends ArrayAdapter<LogEntry> {

        private Context mContext;

        public LogAdapter(Context context, int resource,
                List<LogEntry> objects) {
            super(context, resource, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.superuser_log_list_item,
                    parent, false);

            TextView logDate = (TextView) v.findViewById(R.id.log_date);
            TextView logAction = (TextView) v.findViewById(R.id.log_action);
            TextView logName = (TextView) v.findViewById(R.id.log_name);

            LogEntry log = getItem(position);
            java.text.DateFormat time = DateFormat.getTimeFormat(mContext);
            java.text.DateFormat day = DateFormat.getDateFormat(mContext);
            String dateTime = (day.format(log.getDate()) + " - " +
                    time.format(log.getDate()));

            logDate.setText(dateTime);
            logAction.setText(log.getActionResource());
            logName.setText(log.getName());

            return v;
        }
    }

}
