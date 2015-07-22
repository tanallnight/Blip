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
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tanmay.blip.R;
import com.tanmay.blip.utils.BlipUtils;

public abstract class BaseActivity extends AppCompatActivity{

    private Toolbar mToolbar;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mToolbar != null){
            mToolbar.setBackgroundColor(getToolbarColor());
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(getDisplayHomeAsUpEnabled());
        } else {
            throw new NullPointerException("Layout must contain a toolbar with id 'toolbar'");
        }
    }

    protected void setStatusBarColor(@ColorRes int color) {
        if (BlipUtils.isLollopopUp()) {
            getWindow().setStatusBarColor(getResources().getColor(color));
        }
    }

    protected abstract int getToolbarColor();

    protected abstract int getLayoutResource();

    protected abstract boolean getDisplayHomeAsUpEnabled();

    protected Toolbar getToolbar(){
        return this.mToolbar;
    }
}
