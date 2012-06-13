package com.evervolv.toolbox.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import com.evervolv.toolbox.R;

public class NumberPickerPreference extends DialogPreference {

    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 30;
    
    private NumberPicker mPicker;
    private Context mContext;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setDialogLayoutResource(R.layout.number_picker_pref_dialog);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mPicker = (NumberPicker)view.findViewById(R.id.pref_num_picker);
        mPicker.setMinValue(MIN_VALUE);
        mPicker.setMaxValue(MAX_VALUE);

        int initialValue = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.POWER_MENU_SCREENSHOT_DELAY, 1);
        mPicker.setValue(initialValue);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if ( which == DialogInterface.BUTTON_POSITIVE ) {
            int value = mPicker.getValue();
            if (callChangeListener(value)) {
                persistInt(value);
            }
        }
    }
}