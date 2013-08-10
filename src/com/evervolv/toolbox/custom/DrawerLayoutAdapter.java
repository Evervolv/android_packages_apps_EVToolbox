/*
 * Copyright (C) 2013 The Evervolv Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evervolv.toolbox.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.evervolv.toolbox.R;

public class DrawerLayoutAdapter extends ArrayAdapter<DrawerItem> {

    public static final int ITEM_TYPE_HEADER    = 0;
    public static final int ITEM_TYPE_NAV       = 1;

    public DrawerLayoutAdapter(Context context) {
        super(context, 0);
    }

    public void addHeader(String title, int icon) {
        add(new DrawerItem(title, icon, true));
    }

    public void addNavItem(String title, int icon) {
        add(new DrawerItem(title, icon, false));
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isHeader() ? ITEM_TYPE_HEADER : ITEM_TYPE_NAV;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        DrawerItem item = getItem(position);

        int layout = -1;
        if (item.isHeader()) {
            layout = R.layout.drawer_header_item;
        }else {
            layout = R.layout.drawer_nav_item;
        }

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) 
                    getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout, null);
        }

        TextView title = (TextView) view.findViewById(R.id.item_title);
        title.setText(item.getTitle());

        ImageView icon = (ImageView) view.findViewById(R.id.item_icon);
        if (item.getIconResource() > 0) {
            icon.setImageResource(item.getIconResource());
        }

        return view;
    }

}
