package com.evervolv.toolbox.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.content.Context;
import android.net.wimax.WimaxHelper;
import android.provider.Settings;

import com.evervolv.toolbox.R;

public class QwikWidgetsUtil {

    public static final String WIDGET_WIFI = "toggleWifi";
    public static final String WIDGET_GPS = "toggleGps";
    public static final String WIDGET_BLUETOOTH = "toggleBluetooth";
    public static final String WIDGET_SYNC = "toggleSync";
    public static final String WIDGET_WIFIAP = "toggleWifiAp";
    public static final String WIDGET_MOBDATA = "toggleMobileData";
    public static final String WIDGET_USBTETHER = "toggleUsbTether";
    public static final String WIDGET_AUTOROTATE = "toggleAutoRotate";
    public static final String WIDGET_AIRPLANE = "toggleAirplaneMode";
    public static final String WIDGET_BRIGHTNESS = "toggleBrightness";
    public static final String WIDGET_WIMAX = "toggleWimax";
    public static final String WIDGET_NOTIFICATIONS = "toggleNotifications";

    //Master list of all possible widgets.
    public static final HashMap<String, WidgetInfo> WIDGETS = new HashMap<String, WidgetInfo>();
    static {
	    WIDGETS.put(WIDGET_WIFI, new WidgetInfo(
	            WIDGET_WIFI, R.string.title_toggle_wifi,
	            "com.android.systemui:drawable/widget_wifi_icon"));
	    WIDGETS.put(WIDGET_GPS, new WidgetInfo(
	            WIDGET_GPS, R.string.title_toggle_gps,
	            "com.android.systemui:drawable/widget_gps_icon"));
	    WIDGETS.put(WIDGET_BLUETOOTH, new WidgetInfo(
	            WIDGET_BLUETOOTH, R.string.title_toggle_bluetooth,
	            "com.android.systemui:drawable/widget_bluetooth_icon"));
	    WIDGETS.put(WIDGET_SYNC, new WidgetInfo(
	            WIDGET_SYNC, R.string.title_toggle_sync,
	            "com.android.systemui:drawable/widget_sync_icon"));
	    WIDGETS.put(WIDGET_WIFIAP, new WidgetInfo(
                WIDGET_WIFIAP, R.string.title_toggle_wifiap,
                "com.android.systemui:drawable/widget_wifiap_icon"));
	    WIDGETS.put(WIDGET_MOBDATA, new WidgetInfo(
                WIDGET_MOBDATA, R.string.title_toggle_mobiledata,
                "com.android.systemui:drawable/widget_mobile_data_icon"));
	    WIDGETS.put(WIDGET_USBTETHER, new WidgetInfo(
                WIDGET_USBTETHER, R.string.title_toggle_usbtether,
                "com.android.systemui:drawable/widget_usb_tether_icon"));
	    WIDGETS.put(WIDGET_AUTOROTATE, new WidgetInfo(
                WIDGET_AUTOROTATE, R.string.title_toggle_autorotate,
                "com.android.systemui:drawable/widget_auto_rotate_icon"));
	    WIDGETS.put(WIDGET_AIRPLANE, new WidgetInfo(
                WIDGET_AIRPLANE, R.string.title_toggle_airplane,
                "com.android.systemui:drawable/widget_airplane_icon"));
        WIDGETS.put(WIDGET_BRIGHTNESS, new WidgetInfo(
                WIDGET_BRIGHTNESS, R.string.title_toggle_brightness,
                "com.android.systemui:drawable/widget_brightness_icon"));
        WIDGETS.put(WIDGET_WIMAX, new WidgetInfo(
                WIDGET_WIMAX, R.string.title_toggle_wimax,
                "com.android.systemui:drawable/widget_wimax_icon"));
        WIDGETS.put(WIDGET_NOTIFICATIONS, new WidgetInfo(
                WIDGET_NOTIFICATIONS, R.string.title_toggle_notifications,
                "com.android.systemui:drawable/widget_notifications_icon"));
    }

    private static final String WIDGET_DELIMITER = "|";

    public static String getCurrentWidgets(Context context) {
        String widgets = Settings.System.getString(context.getContentResolver(), Settings.System.SELECTED_QWIK_WIDGETS);
        if (widgets == null) {
            widgets = context.getResources().getString(R.string
                    .default_qwik_widgets);

            // Add the WiMAX widget if it's supported
            if (WimaxHelper.isWimaxSupported(context)) {
                widgets += WIDGET_DELIMITER + WIDGET_WIMAX;
            }
        }

        return widgets;
    }

    public static void saveCurrentWidgets(Context context, String widgets) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.SELECTED_QWIK_WIDGETS, widgets);
    }

    public static String mergeInNewWidgetString(String oldString, String newString) {
        ArrayList<String> oldList = getWidgetListFromString(oldString);
        ArrayList<String> newList = getWidgetListFromString(newString);
        ArrayList<String> mergedList = new ArrayList<String>();

        // add any items from oldlist that are in new list
        for(String widget : oldList) {
            if(newList.contains(widget)) {
                mergedList.add(widget);
            }
        }

        // append anything in newlist that isn't already in the merged list to the end of the list
        for(String widget : newList) {
            if(!mergedList.contains(widget)) {
                mergedList.add(widget);
            }
        }

        // return merged list
        return getWidgetStringFromList(mergedList);
    }

    public static ArrayList<String> getWidgetListFromString(String widgets) {
        return new ArrayList<String>(Arrays.asList(widgets.split("\\|")));
    }

    public static String getWidgetStringFromList(ArrayList<String> widgets) {
        if(widgets == null || widgets.size() <= 0) {
            return "";
        } else {
            String s = widgets.get(0);
            for(int i = 1; i < widgets.size(); i++) {
                s += WIDGET_DELIMITER + widgets.get(i);
            }
            return s;
        }
    }

    public static String appendWidgetString(String newWidget, String oldWidgetString) {
    	return oldWidgetString + WIDGET_DELIMITER + newWidget ;
    }

    public static class WidgetInfo {
        private String mId;
        private int mTitleResId;
        private String mIcon;

        public WidgetInfo(String id, int titleResId, String icon) {
            mId = id;
            mTitleResId = titleResId;
            mIcon = icon;
        }

        public String getId() { return mId; }
        public int getTitleResId() { return mTitleResId; }
        public String getIcon() { return mIcon; }
    }

    public static String arrayToString(String[] a, String separator) {
        String result = "";
        if (a.length > 0) {
            result = a[0];    // start with the first element
            for (int i=1; i<a.length; i++) {
                result = result + separator + a[i];
            }
        }
        return result;
    }

    public static boolean doesWidgetExist(Context c, String id) {
        ArrayList<String> widgets = getWidgetListFromString(getCurrentWidgets(c));
        if (widgets.contains(id)) {
            return true;
        } else {
            return false;
        }
    }
    
}
