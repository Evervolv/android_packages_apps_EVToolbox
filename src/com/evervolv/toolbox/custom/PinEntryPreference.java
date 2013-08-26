package com.evervolv.toolbox.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.evervolv.toolbox.R;

public class PinEntryPreference extends DialogPreference {

    private EditText mPassword;

    public PinEntryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.superuser_pin_preference);
    }

    @Override
    protected void onBindDialogView(View view) {
        mPassword = (EditText) view.findViewById(R.id.password);
        int[] ids = new int[] { R.id.p0, R.id.p1, R.id.p2, R.id.p3, R.id.p4, R.id.p5, R.id.p6, R.id.p7, R.id.p8, R.id.p9, };
        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            Button b = (Button) view.findViewById(id);
            final String text = String.valueOf(i);
            b.setText(text);
            b.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPassword.setText(mPassword.getText().toString() + text);
                }
            });
        }

        view.findViewById(R.id.pd).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String curPass = mPassword.getText().toString();
                if (curPass.length() > 0) {
                    curPass = curPass.substring(0, curPass.length() - 1);
                    mPassword.setText(curPass);
                }
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if ( which == DialogInterface.BUTTON_POSITIVE ) {
            String pin = mPassword.getText().toString();
            if (callChangeListener(pin)) {
                persistString(pin);
            }
        }
    }

}
