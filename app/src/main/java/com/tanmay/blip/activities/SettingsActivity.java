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


    private View hideTitleGroup, searchTranscriptGroup;
    private AppCompatCheckBox hideTitle, searchTranscript;
    private SharedPrefs sharedPrefs;

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

        sharedPrefs = SharedPrefs.getInstance();

        hideTitleGroup = findViewById(R.id.hide_title_group);
        searchTranscriptGroup = findViewById(R.id.search_transcript_group);
        hideTitle = (AppCompatCheckBox) findViewById(R.id.hide_title);
        searchTranscript = (AppCompatCheckBox) findViewById(R.id.search_transcript);

        hideTitle.setChecked(sharedPrefs.isTitleHidden());
        searchTranscript.setChecked(sharedPrefs.transcriptSearchEnabled());

        hideTitleGroup.setOnClickListener(this);
        searchTranscriptGroup.setOnClickListener(this);
        hideTitle.setOnCheckedChangeListener(this);
        searchTranscript.setOnCheckedChangeListener(this);

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
        }
    }
}
