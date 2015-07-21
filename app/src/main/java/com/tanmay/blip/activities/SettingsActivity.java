/*
 * Copyright 2015, Tanmay Parikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tanmay.blip.activities;

import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.CompoundButton;

import com.tanmay.blip.R;
import com.tanmay.blip.database.SharedPrefs;

/**
 * Created by Tanmay Parikh on 7/21/2015.
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private View hideTitleGroup;
    private AppCompatCheckBox hideTitle;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_settings;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideTitleGroup = findViewById(R.id.hide_title_group);
        hideTitle = (AppCompatCheckBox) findViewById(R.id.hide_title);

        hideTitle.setChecked(SharedPrefs.getInstance().isTitleHidden());

        hideTitleGroup.setOnClickListener(this);
        hideTitle.setOnCheckedChangeListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hide_title_group:
                SharedPrefs.getInstance().setHideTitle(!hideTitle.isChecked());
                hideTitle.setChecked(!hideTitle.isChecked());
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPrefs.getInstance().setHideTitle(isChecked);
    }
}
