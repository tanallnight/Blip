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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.tanmay.blip.R;
import com.tanmay.blip.database.SharedPrefs;

import de.psdev.licensesdialog.LicensesDialog;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_about;
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

        (findViewById(R.id.git)).setOnClickListener(this);
        (findViewById(R.id.licenses)).setOnClickListener(this);
        (findViewById(R.id.developer)).setOnClickListener(this);

        if (SharedPrefs.getInstance().isNightModeEnabled()) {
            (findViewById(R.id.parent)).setBackgroundColor(getResources().getColor(R.color.primary_night));
            setStatusBarColor(R.color.primary_dark_night);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.git:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tanallnight/Blip")));
                break;
            case R.id.licenses:
                new LicensesDialog.Builder(this)
                        .setNotices(R.raw.notices)
                        .build()
                        .show();
                break;
            case R.id.developer:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/+TanmayParikh/posts")));
                break;
        }
    }
}
