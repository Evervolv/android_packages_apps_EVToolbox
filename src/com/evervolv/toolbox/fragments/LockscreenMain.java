package com.evervolv.toolbox.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Display;
import android.view.Window;
import android.widget.Toast;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.custom.ColorPickerView;

import java.io.File;
import java.io.IOException;

public class LockscreenMain extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String LOCKSCREEN_MESSAGE_PREF = "pref_lockscreen_main_message";
    private static final String LOCKSCREEN_BACKGROUND_PREF = "pref_lockscreen_main_background";

    private static final int LOCKSCREEN_BACKGROUND_COLOR_FILL = 0;
    private static final int LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE = 1;
    private static final int LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER = 2;
    private static final int LOCKSCREEN_BACKGROUND_TRANSPARENT = 3;

    private static final int REQUEST_CODE_BG_WALLPAPER = 1024;

    private EditTextPreference mLockMessage;
    private ListPreference mLockBackground;
    private PreferenceScreen mPrefSet;
    private ContentResolver mCr;

    private File mWallpaperImage;
    private File mWallpaperTemporary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_main);

        mPrefSet = getPreferenceScreen();
        mCr = getActivity().getContentResolver();

        mLockBackground = (ListPreference) mPrefSet.findPreference(
                LOCKSCREEN_BACKGROUND_PREF);
        mLockBackground.setOnPreferenceChangeListener(this);
        updateLockBackgroundSummary();

        mLockMessage = (EditTextPreference) mPrefSet.findPreference(
                LOCKSCREEN_MESSAGE_PREF);
        mLockMessage.setOnPreferenceChangeListener(this);
        String message = Settings.System.getString(mCr,
                Settings.System.LOCKSCREEN_MESSAGE);
        setLockMessageSummaryAndText(message);

        mWallpaperImage = new File(getActivity().getFilesDir() + "/lock_wallpaper");
        mWallpaperTemporary = new File(getActivity().getCacheDir() + "/lock_wallpaper.tmp");
    }

    private void setLockMessageSummaryAndText(String message) {
        if (message == null || message.equals("")) {
            mLockMessage.setText("");
            mLockMessage.setSummary(R.string
                    .pref_lockscreen_main_message_summary);
        } else {
            mLockMessage.setSummary(message);
            mLockMessage.setText(message);
        }
    }

    private void updateLockBackgroundSummary() {
        int resId;
        int value = Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_BACKGROUND, -1);
        if (value == -1) {
            resId = R.string.pref_lockscreen_main_background_default_wallpaper;
            mLockBackground.setValueIndex(LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER);
        } else if (value == -2) {
            resId = R.string.pref_lockscreen_main_background_custom_image;
            mLockBackground.setValueIndex(LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE);
        } else if (value == -3) {
            resId = R.string.pref_lockscreen_main_background_transparent;
            mLockBackground.setValueIndex(LOCKSCREEN_BACKGROUND_TRANSPARENT);
        } else {
            resId = R.string.pref_lockscreen_main_background_color_fill;
            mLockBackground.setValueIndex(LOCKSCREEN_BACKGROUND_COLOR_FILL);
        }
        mLockBackground.setSummary(getResources().getString(resId));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockMessage) {
            String value = newValue.toString();
            setLockMessageSummaryAndText(value);
            if (value.equals("")) {
                Settings.System.putString(mCr, Settings.System.LOCKSCREEN_MESSAGE,
                        null);
            } else {
                Settings.System.putString(mCr, Settings.System.LOCKSCREEN_MESSAGE,
                        value);
            }
        } else if (preference == mLockBackground) {
            int selection = mLockBackground.findIndexOfValue(newValue.toString());
            return handleBackgroundSelection(selection);
        }
        return false;
    }

    private boolean handleBackgroundSelection(int selection) {
        if (selection == LOCKSCREEN_BACKGROUND_COLOR_FILL) {
            final ColorPickerView colorView = new ColorPickerView(getActivity());
            int currentColor = Settings.System.getInt(mCr,
                    Settings.System.LOCKSCREEN_BACKGROUND, -1);

            if (currentColor > 0) {
                colorView.setColor(currentColor);
            }
            colorView.setAlphaSliderVisible(true);

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pref_lockscreen_main_background_dialog_title)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(mCr,
                                    Settings.System.LOCKSCREEN_BACKGROUND, colorView.getColor());
                            updateLockBackgroundSummary();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setView(colorView)
                    .show();
        } else if (selection == LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

            final Display display = getActivity().getWindowManager().getDefaultDisplay();
            final Rect rect = new Rect();
            final Window window = getActivity().getWindow();

            window.getDecorView().getWindowVisibleDisplayFrame(rect);

            int statusBarHeight = rect.top;
            int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight = contentViewTop - statusBarHeight;
            boolean isPortrait = getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT;

            int width = display.getWidth();
            int height = display.getHeight() - titleBarHeight;

            intent.putExtra("aspectX", isPortrait ? width : height);
            intent.putExtra("aspectY", isPortrait ? height : width);

            try {
                mWallpaperTemporary.createNewFile();
                mWallpaperTemporary.setWritable(true, false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mWallpaperTemporary));
                intent.putExtra("return-data", false);
                getActivity().startActivityFromFragment(this, intent, REQUEST_CODE_BG_WALLPAPER);
                Settings.System.putInt(mCr,
                        Settings.System.LOCKSCREEN_BACKGROUND, -2);
            } catch (IOException e) {
                // Do nothing here
            } catch (ActivityNotFoundException e) {
                // Do nothing here
            }
        } else if (selection == LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER) {
            Settings.System.putInt(mCr,
                    Settings.System.LOCKSCREEN_BACKGROUND, -1);
            updateLockBackgroundSummary();
            return true;
        } else if (selection == LOCKSCREEN_BACKGROUND_TRANSPARENT) {
            Settings.System.putInt(mCr,
                    Settings.System.LOCKSCREEN_BACKGROUND, -3);
            updateLockBackgroundSummary();
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_BG_WALLPAPER) {
            int hintId;

            if (resultCode == Activity.RESULT_OK) {
                if (mWallpaperTemporary.exists()) {
                    mWallpaperTemporary.renameTo(mWallpaperImage);
                }
                mWallpaperImage.setReadOnly();
                hintId = R.string.pref_lockscreen_main_background_result_successful;
                Settings.System.putInt(mCr,
                        Settings.System.LOCKSCREEN_BACKGROUND, -2);
                updateLockBackgroundSummary();
            } else {
                if (mWallpaperTemporary.exists()) {
                    mWallpaperTemporary.delete();
                }
                hintId = R.string.pref_lockscreen_main_background_result_not_successful;
            }
            Toast.makeText(getActivity(),
                    getResources().getString(hintId), Toast.LENGTH_LONG).show();
        }
    }

}
