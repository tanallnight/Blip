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
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanmay.blip.R;
import com.tanmay.blip.activities.ImageActivity;
import com.tanmay.blip.adapters.FeedAdapter;
import com.tanmay.blip.managers.ComicManager;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.views.EndlessRecyclerOnScrollListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FeedFragment extends Fragment implements ComicManager.ComicMultiLoadListener, ComicManager.LatestComicLoadListener, FeedAdapter.ItemClickListener {

    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private FeedAdapter mAdapter;
    private ComicManager mComicManager;

    private int continuationToken;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mComicManager = new ComicManager(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, rootView);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(staggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                mComicManager.loadBatch(continuationToken, ComicManager.COMICS_TO_DOWNLOAD, FeedFragment.this);
                continuationToken = continuationToken - ComicManager.COMICS_TO_DOWNLOAD;
            }
        });

        mComicManager.getLatestComicNum(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mComicManager.cleanUp();
    }

    @Override
    public void onLoadSuccess(List<Comic> comics) {
        if (mAdapter == null) {
            mAdapter = new FeedAdapter(comics, this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.addComics(comics);
        }
    }

    @Override
    public void onLoad(int num) {
        mComicManager.loadBatch(num, ComicManager.COMICS_TO_DOWNLOAD, this);
        continuationToken = num - ComicManager.COMICS_TO_DOWNLOAD;
    }

    @Override
    public void onImageClick(int position, int num) {
        ImageActivity.launch(getActivity(), num);
    }

    @Override
    public void onFavouriteClick(int position, boolean isFavourite, int num) {
        mComicManager.updateFavouriteStatus(num, !isFavourite);
        mAdapter.changeFavourite(position);
    }

    @Override
    public void onShareClick(Comic comic) {
        Intent shareIntent = ShareCompat.IntentBuilder
                .from(getActivity())
                .setType("text/plain")
                .setSubject(comic.getTitle())
                .setText(comic.getImg())
                .getIntent();
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.tip_share_image_url)));
    }

}
