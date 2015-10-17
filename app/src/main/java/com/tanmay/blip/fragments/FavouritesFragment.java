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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tanmay.blip.R;
import com.tanmay.blip.activities.ImageActivity;
import com.tanmay.blip.adapters.FeedAdapter;
import com.tanmay.blip.managers.ComicManager;
import com.tanmay.blip.models.Comic;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FavouritesFragment extends Fragment implements FeedAdapter.ItemClickListener {

    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.no_favs)
    ImageView mNoFavsImage;

    private ComicManager mComicManager;
    private FeedAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favourite, container, false);
        ButterKnife.bind(this, rootView);

        mComicManager = new ComicManager(getContext());
        List<Comic> comics = mComicManager.getFavourites();
        if (comics.size() != 0) {
            mAdapter = new FeedAdapter(comics, this);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoFavsImage.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onImageClick(int position, int num) {
        ImageActivity.launch(getActivity(), num);
    }

    @Override
    public void onFavouriteClick(int position, boolean isFavourite, int num) {
        mComicManager.updateFavouriteStatus(num, !isFavourite);
        mAdapter.notifyItemRemoved(position);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mComicManager.cleanUp();
    }
}
