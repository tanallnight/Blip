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

package com.tanmay.blip.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tanmay.blip.R;
import com.tanmay.blip.activities.ImageActivity;
import com.tanmay.blip.managers.ComicManager;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.utils.BlipUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class RandomFragment extends Fragment implements ComicManager.ComicLoadListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    @Bind(R.id.image)
    ImageView mImage;
    @Bind(R.id.title)
    TextView mTitle;
    @Bind(R.id.subhead)
    TextView mSubhead;
    @Bind(R.id.favourite)
    TextView mFavourite;
    @Bind(R.id.share)
    TextView mShare;
    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;
    @Bind(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindString(R.string.action_favourite)
    String favourite;
    @BindString(R.string.action_unfavourite)
    String unfavourite;

    private ComicManager mComicManager;
    private Calendar mCalendar;
    private SimpleDateFormat mSimpleDateFormat;
    private Comic mCurrentComic;

    private int currentComic = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mComicManager = new ComicManager(getContext());
        mCalendar = Calendar.getInstance();
        mSimpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy (EEEE)", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_random, container, false);
        ButterKnife.bind(this, rootView);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent, R.color.primary);
        mFavourite.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mImage.setOnClickListener(this);
        mFloatingActionButton.setOnClickListener(this);

        if (savedInstanceState != null)
            currentComic = savedInstanceState.getInt("NUM");
        loadComic(currentComic);
        return rootView;
    }

    private void loadComic(int key) {
        int num;
        if (key == 0) {
            num = BlipUtils.randInt(1, mComicManager.getLatestComicNumOffline());
        } else {
            num = key;
        }
        currentComic = num;
        mComicManager.loadComic(num, this);
    }

    @Override
    public void onLoadSuccess(final Comic comic) {
        mCurrentComic = comic;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                mTitle.setText(comic.getTitle());
                mSubhead.setText(BlipUtils.createSubhead(comic, mCalendar, mSimpleDateFormat));
                if (comic.isFavourite()) {
                    mFavourite.setText(unfavourite);
                } else {
                    mFavourite.setText(favourite);
                }
                Picasso.with(mImage.getContext()).load(comic.getImg()).into(mImage);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mComicManager.cleanUp();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                ImageActivity.launch(getActivity(), mCurrentComic.getNum());
                break;
            case R.id.favourite:
                mComicManager.updateFavouriteStatus(mCurrentComic.getNum(), !mCurrentComic.isFavourite());
                mCurrentComic.setFavourite(!mCurrentComic.isFavourite());
                if (mCurrentComic.isFavourite()) {
                    mFavourite.setText(unfavourite);
                } else {
                    mFavourite.setText(favourite);
                }
                break;
            case R.id.share:
                Intent shareIntent = ShareCompat.IntentBuilder
                        .from(getActivity())
                        .setType("text/plain")
                        .setSubject(mCurrentComic.getTitle())
                        .setText(mCurrentComic.getImg())
                        .getIntent();
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.tip_share_image_url)));
                break;
            case R.id.fab:
                loadComic(0);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("NUM", currentComic);
    }

    @Override
    public void onLoadFail() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        loadComic(0);
    }
}
