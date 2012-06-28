package com.evervolv.toolbox.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.utils.QwikWidgetsUtil.WidgetInfo;

public class WidgetManagerDialog extends Dialog implements OnClickListener {

    private static final String TAG = "EVToolbox";
    private static final int MENU_DELETE = Menu.FIRST;

    private HashMap<String, WidgetInfo> SUPPORTED_WIDGETS = new HashMap<String, WidgetInfo>();

    private Context mContext;
    private TouchInterceptor mWidgetList;
    private WidgetAdapter mWidgetAdapter;
    private ViewFlipper mViewFlipper;
    private ListView mAddWidgetList;
    private ArrayAdapter<String> mAddWidgetAdapter;

    private Button mAddButton;
    private Button mDoneButton;

    private ArrayList<QwikWidgetsUtil.WidgetInfo> mWidgetsSupported;

    public WidgetManagerDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_widgets_dialog);

        mAddButton = (Button) findViewById(R.id.button_add);
        mAddButton.setOnClickListener(this);
        mDoneButton = (Button) findViewById(R.id.button_done);
        mDoneButton.setOnClickListener(this);

        mWidgetAdapter = new WidgetAdapter(mContext);
        mWidgetList = (TouchInterceptor) findViewById(R.id.widget_list);
        mWidgetList.setAdapter(mWidgetAdapter);
        mWidgetList.setDropListener(mDropListener);
        registerForContextMenu(mWidgetList);
        mWidgetList.setOnCreateContextMenuListener(this);

        loadSupportedWidgets();

        mAddWidgetAdapter = new ArrayAdapter<String>(mContext, R.layout
                .add_widgets_list_item);
        mWidgetsSupported = new ArrayList<QwikWidgetsUtil.WidgetInfo>();
        loadAddWidgetsList();

        mAddWidgetList = (ListView) findViewById(R.id.widget_add_list);
        mAddWidgetList.setAdapter(mAddWidgetAdapter);
        mAddWidgetList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                addWidget(mWidgetsSupported.get(position).getId());
                mWidgetAdapter.reloadWidgets();
                mWidgetList.invalidateViews();
                flip(false);
            }
        });
        mViewFlipper = (ViewFlipper) findViewById(R.id.content_flipper);
        mViewFlipper.setDisplayedChild(0);
    }

    private void loadAddWidgetsList() {
        mWidgetsSupported.clear();
        mAddWidgetAdapter.clear();
        for(QwikWidgetsUtil.WidgetInfo widget : SUPPORTED_WIDGETS.values()) {
            if (!QwikWidgetsUtil.doesWidgetExist(mContext, widget.getId())) {
                mWidgetsSupported.add(widget);
                mAddWidgetAdapter.add(mContext.getResources().getString(widget
                        .getTitleResId()));
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
      menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Remove")
          .setAlphabeticShortcut('d');
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
      switch (item.getItemId()) {
        case MENU_DELETE:
          AdapterView.AdapterContextMenuInfo info =
            (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
          
          ArrayList<String> widgets = QwikWidgetsUtil.getWidgetListFromString(
                  QwikWidgetsUtil.getCurrentWidgets(mContext));
          
          widgets.remove(info.position);
          
          QwikWidgetsUtil.saveCurrentWidgets(mContext,
                  QwikWidgetsUtil.getWidgetStringFromList(widgets));

          mWidgetAdapter.reloadWidgets();
          mWidgetList.invalidateViews();
          return true;
      }

      return super.onOptionsItemSelected(item);
    }
    
    private void loadSupportedWidgets() {
        SUPPORTED_WIDGETS = QwikWidgetsUtil.WIDGETS;

        //Remove unsuppored widgets per-device config overlay.
        if (!mContext.getResources().getBoolean(R.bool.config_has_wimax)) {
            SUPPORTED_WIDGETS.remove(QwikWidgetsUtil.WIDGET_WIMAX);
        }
        if (!mContext.getResources().getBoolean(R.bool.config_has_gps)) {
            SUPPORTED_WIDGETS.remove(QwikWidgetsUtil.WIDGET_GPS);
        }
        if (!mContext.getResources().getBoolean(R.bool.config_has_mobile_data)) {
            SUPPORTED_WIDGETS.remove(QwikWidgetsUtil.WIDGET_MOBDATA);
        }
        if (!mContext.getResources().getBoolean(R.bool.config_has_bluetooth)) {
            SUPPORTED_WIDGETS.remove(QwikWidgetsUtil.WIDGET_BLUETOOTH);
        }
    }
    
    public void addWidget(String widget) {
        if (QwikWidgetsUtil.getCurrentWidgets(mContext).equals("")) {
            QwikWidgetsUtil.saveCurrentWidgets(mContext, widget);
        } else {
            QwikWidgetsUtil.saveCurrentWidgets(mContext, QwikWidgetsUtil.
                    appendWidgetString(widget, QwikWidgetsUtil.getCurrentWidgets(mContext)));
        }
    }

    private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
        public void drop(int from, int to) {
            // get the current button list
            ArrayList<String> widgets = QwikWidgetsUtil.getWidgetListFromString(
                    QwikWidgetsUtil.getCurrentWidgets(mContext));
            
            // move the button
            if (from < widgets.size()) {
                String widget = widgets.remove(from);

                if (to <= widgets.size()) {
                    widgets.add(to, widget);
                    QwikWidgetsUtil.saveCurrentWidgets(mContext,
                            QwikWidgetsUtil.getWidgetStringFromList(widgets));
                    mWidgetAdapter.reloadWidgets();
                    mWidgetList.invalidateViews();
                }
            }
        }
    };
    
    private class WidgetAdapter extends BaseAdapter {
        private Context mContext;
        private Resources mSystemUIResources = null;
        private LayoutInflater mInflater;
        private ArrayList<QwikWidgetsUtil.WidgetInfo> mWidgets;
        
        public WidgetAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);

            PackageManager pm = mContext.getPackageManager();
            if (pm != null) {
                try {
                    mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                } catch (Exception e) {
                    mSystemUIResources = null;
                    Log.e(TAG, "Could not load SystemUI resources", e);
                }
            }
            reloadWidgets();
        }

        public void reloadWidgets() {
            ArrayList<String> widgets = QwikWidgetsUtil.getWidgetListFromString(
                    QwikWidgetsUtil.getCurrentWidgets(mContext));
            mWidgets = new ArrayList<QwikWidgetsUtil.WidgetInfo>();
            for (String widget : widgets) {
                if (QwikWidgetsUtil.WIDGETS.containsKey(widget)) {
                    mWidgets.add(QwikWidgetsUtil.WIDGETS.get(widget));
                }
            }
        }

        public int getCount() {
            return mWidgets.size();
        }

        public Object getItem(int position) {
            return mWidgets.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = mInflater.inflate(R.layout.mange_widgets_list_item, null);
            } else {
                v = convertView;
            }

            QwikWidgetsUtil.WidgetInfo widget = mWidgets.get(position);

            final TextView name = (TextView) v.findViewById(R.id.name);
            final ImageView icon = (ImageView) v.findViewById(R.id.icon);

            name.setText(widget.getTitleResId());

            // assume no icon first
            icon.setVisibility(View.GONE);

            // attempt to load the icon for this button
            if (mSystemUIResources != null) {
                int resId = mSystemUIResources.getIdentifier(widget.getIcon(), null, null);
                if (resId > 0) {
                    try {
                        Drawable d = mSystemUIResources.getDrawable(resId);
                        icon.setVisibility(View.VISIBLE);
                        icon.setImageDrawable(d);
                    } catch (Exception e) {
                        Log.e(TAG, "Error retrieving icon drawable", e);
                    }
                }
            }
            return v;
        }
    }

    private void flip(boolean next) {
        if (next) {
            mViewFlipper.setInAnimation(mContext, R.anim.in_animation);
            mViewFlipper.setOutAnimation(mContext, R.anim.out_animation);
            mViewFlipper.showNext();
            mAddButton.setText(R.string.dialog_cancel);
        } else {
            mViewFlipper.setInAnimation(mContext, R.anim.in_animation1);
            mViewFlipper.setOutAnimation(mContext, R.anim.out_animation1);
            mViewFlipper.showPrevious();
            mAddButton.setText(R.string.dialog_add);
        }
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_add) {
            if (mViewFlipper.getDisplayedChild() == 0) {
                loadAddWidgetsList();
                flip(true);
            } else {
                flip(false);
            }
        } else if (v.getId() == R.id.button_done) {
            dismiss();
        }
    }
    
}
