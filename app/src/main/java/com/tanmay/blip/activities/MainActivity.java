/*
 *   Copyright 2015, Tanmay Parikh
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.tanmay.blip.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tanmay.blip.R;
import com.tanmay.blip.fragments.FavouritesFragment;
import com.tanmay.blip.fragments.FeedFragment;
import com.tanmay.blip.fragments.RandomFragment;
import com.tanmay.blip.fragments.WhatIfFragment;
import com.tanmay.blip.managers.SharedPrefs;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private static final String FEED_FRAGMENT = "FeedFragment";
    private static final String RANDOM_FRAGMENT = "RandomFragment";
    private static final String FAVOURITES_FRAGMENT = "FavouritesFragment";
    private static final String WHAT_IF_FRAGMENT = "WhatIfFragment";

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.navigation_view)
    NavigationView mNavigationView;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
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
        ButterKnife.bind(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, getToolbar(), R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(this);

        View headerView = getLayoutInflater().inflate(R.layout.item_nav_header, mNavigationView, false);
        ImageView headerImage = (ImageView) headerView.findViewById(R.id.header_image);
        Picasso.with(this)
                .load(R.drawable.nav_header)
                .into(headerImage);
        mNavigationView.addHeaderView(headerView);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new FeedFragment(), FEED_FRAGMENT).commit();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.feed:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new FeedFragment(), FEED_FRAGMENT).commit();
                return true;
            case R.id.random:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new RandomFragment(), RANDOM_FRAGMENT).commit();
                return true;
            case R.id.favourite:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new FavouritesFragment(), FAVOURITES_FRAGMENT).commit();
                return true;
            case R.id.download:
                startActivity(new Intent(this, OfflineActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.whatif:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new WhatIfFragment(), WHAT_IF_FRAGMENT).commit();
                return true;
        }
        return false;
    }
}
