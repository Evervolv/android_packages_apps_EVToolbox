package com.evervolv.toolbox.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.evervolv.toolbox.R;

public class GalleryPickerPreference extends Preference implements OnClickListener {

    private static final String TAG = "EVToolbox";

    private static final int DEFAULT_X = 240;
    private static final int DEFAULT_Y = 400;

    private Gallery mGallery;
    private ImageAdapter mAdapter;
    private TextView mCurrentPositionView;
    private TextView mStyleNameView;
    private int mCurrStylePosition;
    private Button applyButton;
    private View lastView = null;
    private SharedPreferences mSharedPrefs;
    private CharSequence[] mCaptions;
    private int[] mDrawableIds;

    public GalleryPickerPreference(Context context) {
        this(context, null);
    }

    public GalleryPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable
                .GalleryPickerPreference);

        mCaptions = a.getTextArray(R.styleable.GalleryPickerPreference_entryCaptions);
        Resources res = getContext().getResources();

        TypedArray array = res.obtainTypedArray(a.getResourceId(R.styleable
                .GalleryPickerPreference_entryDrawables, 0));

        int count = array.length();
        mDrawableIds = new int[count];
        for (int i = 0; i < count; i++) {
            Log.d(TAG, "Drawable ResourceID: " + array.getResourceId(i, 0));
            mDrawableIds[i] = array.getResourceId(i, 0);
        }

        a.recycle();
        onInit();
    }

    public void onInit() {
        setLayoutResource(R.layout.gallery_picker_preference);
        mAdapter = new ImageAdapter(getContext(), mDrawableIds);
        setEnabled(true);
        setSelectable(true);
        setPersistent(true);
    }

    public void setCurrPos(int pos) {
        mCurrStylePosition = pos;
    }

    public void setSharedPrefs(SharedPreferences shPref) {
        mSharedPrefs = shPref;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mStyleNameView = (TextView) view.findViewById(R.id.theme_name);
        mCurrentPositionView = (TextView) view.findViewById(R.id.adapter_position);

        mGallery = (Gallery) view.findViewById(R.id.gallery);
        mGallery.setAdapter(mAdapter);
        mGallery.setOnItemSelectedListener(mItemSelected);
        mGallery.setFocusable(true);
        mGallery.setFocusableInTouchMode(true);
        mGallery.setUnselectedAlpha(0.5f);
        mGallery.setSelection(mCurrStylePosition);

        applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(this);
    }

    public class ImageAdapter extends BaseAdapter {

        private Context myContext;
        private int[] mResIds;

        public ImageAdapter(Context c, int[] drawableIds) {
            myContext = c;
            mResIds = drawableIds;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
           ImageView i = new ImageView(myContext);
           i.setImageResource(mResIds[position]);
           i.setScaleType(ImageView.ScaleType.FIT_XY);
           int x = DEFAULT_X;
           int y = DEFAULT_Y;

           DisplayMetrics dm = myContext.getResources().getDisplayMetrics();
           switch(dm.densityDpi){
                case DisplayMetrics.DENSITY_MEDIUM:
                    x = (int) (x * .50);
                    y = (int) (y * .75);
                    break;
                case DisplayMetrics.DENSITY_HIGH:
                    x = (int) (x * .75);
                    y = (int) (y * .75);
                    break;
                case DisplayMetrics.DENSITY_XHIGH:
                    //use defaults
                    break;
           }
           i.setLayoutParams(new Gallery.LayoutParams(x, y));
           return i;
        }

        public float getScale(boolean focused, int offset) {
            return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
        }

        @Override
        public int getCount() {
            return mResIds.length;
        }
    }

    private final OnItemSelectedListener mItemSelected = new OnItemSelectedListener() {
        Resources res = getContext().getResources();

        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            // Shrink the view that was zoomed
            try {
                if (lastView != null) lastView.clearAnimation();
            } catch (Exception clear) {
                //What to do?
            }

            // Zoom the new selected view
            try {
                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.grow));
            } catch (Exception animate) {
                //What to do?
            }
            String caption = (String) mCaptions[position];
            if (mCurrStylePosition == position) {
                caption += " (current)";
            }
            // Set the last view so we can clear the animation
            lastView = view;
            //Set the current views caption & position
            mCurrentPositionView.setText(res.getString(R.string.item_count,
                    (position + 1), mAdapter.getCount()));
            mStyleNameView.setText(caption);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    @Override
    public void onClick(View v) {
        if (v == applyButton) {
            String value = Integer.toString(mGallery.getSelectedItemPosition());
            if (callChangeListener(value)) {
                setValue(value);
                mCurrStylePosition = mGallery.getSelectedItemPosition();
            }
        }
    }

    private void setValue(String value) {
        mSharedPrefs.edit().putString(getKey(), value).commit();
    }
}
