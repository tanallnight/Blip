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
import android.widget.TextView;

import com.tanmay.blip.R;
import com.tanmay.blip.database.SharedPrefs;
import com.tanmay.blip.utils.BlipUtils;

/**
 * Created by Tanmay Parikh on 7/21/2015.
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private View hideTitleGroup, searchTranscriptGroup, nightModeGroup;
    private AppCompatCheckBox hideTitle, searchTranscript, nightMode;
    private SharedPrefs sharedPrefs;
    private TextView hideTitleTitle, showTranscriptTitle, nightModeTitle;
    private TextView hideTitleSubhead, showTranscriptSubhead, nightModeSubhead;
    private View parent;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_settings;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected int getToolbarColor() {
        if (SharedPrefs.getInstance().isNightModeEnabled()) {
            return getResources().getColor(R.color.primary_night);
        } else {
            return getResources().getColor(R.color.primary);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = SharedPrefs.getInstance();

        hideTitleGroup = findViewById(R.id.hide_title_group);
        searchTranscriptGroup = findViewById(R.id.search_transcript_group);
        nightModeGroup = findViewById(R.id.night_mode_group);
        hideTitle = (AppCompatCheckBox) findViewById(R.id.hide_title);
        searchTranscript = (AppCompatCheckBox) findViewById(R.id.search_transcript);
        nightMode = (AppCompatCheckBox) findViewById(R.id.night_mode);

        hideTitleTitle = (TextView) findViewById(R.id.hide_title_title);
        showTranscriptTitle = (TextView) findViewById(R.id.search_transcript_title);
        nightModeTitle = (TextView) findViewById(R.id.night_mode_title);
        hideTitleSubhead = (TextView) findViewById(R.id.hide_title_subhead);
        showTranscriptSubhead = (TextView) findViewById(R.id.search_transcript_subhead);
        nightModeSubhead = (TextView) findViewById(R.id.night_mode_subhead);

        parent = findViewById(R.id.parent);

        hideTitle.setChecked(sharedPrefs.isTitleHidden());
        searchTranscript.setChecked(sharedPrefs.transcriptSearchEnabled());
        nightMode.setChecked(sharedPrefs.isNightModeEnabled());

        hideTitleGroup.setOnClickListener(this);
        searchTranscriptGroup.setOnClickListener(this);
        nightModeGroup.setOnClickListener(this);
        hideTitle.setOnCheckedChangeListener(this);
        searchTranscript.setOnCheckedChangeListener(this);
        nightMode.setOnCheckedChangeListener(this);

        if (sharedPrefs.isNightModeEnabled()) {
            parent.setBackgroundColor(getResources().getColor(R.color.primary_light_night));
            hideTitleTitle.setTextColor(getResources().getColor(android.R.color.white));
            showTranscriptTitle.setTextColor(getResources().getColor(android.R.color.white));
            nightModeTitle.setTextColor(getResources().getColor(android.R.color.white));
            hideTitleSubhead.setTextColor(getResources().getColor(android.R.color.white));
            showTranscriptSubhead.setTextColor(getResources().getColor(android.R.color.white));
            nightModeSubhead.setTextColor(getResources().getColor(android.R.color.white));
            setStatusBarColor(R.color.primary_dark_night);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hide_title_group:
                sharedPrefs.setHideTitle(!hideTitle.isChecked());
                hideTitle.setChecked(!hideTitle.isChecked());
                break;
            case R.id.search_transcript_group:
                sharedPrefs.searchThroughTranscript(!searchTranscript.isChecked());
                searchTranscript.setChecked(!searchTranscript.isChecked());
                break;
            case R.id.night_mode_group:
                sharedPrefs.nightModeEnabled(!nightMode.isChecked());
                nightMode.setChecked(!nightMode.isChecked());
                BlipUtils.restartApp(this);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.hide_title:
                sharedPrefs.setHideTitle(isChecked);
                break;
            case R.id.search_transcript:
                sharedPrefs.searchThroughTranscript(isChecked);
                break;
            case R.id.night_mode:
                sharedPrefs.nightModeEnabled(isChecked);
                BlipUtils.restartApp(this);
                break;
        }
    }
}
