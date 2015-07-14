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

package com.tanmay.blip.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.tanmay.blip.BlipApplication;
import com.tanmay.blip.BlipUtils;
import com.tanmay.blip.R;
import com.tanmay.blip.activities.ImageActivity;
import com.tanmay.blip.activities.SearchActivity;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.models.Comic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RandomFragment extends Fragment implements View.OnClickListener {

    private TextView title, date, alt;
    private ImageView img, favourite;
    private View browser, transcript, imgContainer;
    private DatabaseManager databaseManager;
    private SimpleDateFormat simpleDateFormat;
    private Comic comic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_random, container, false);

        setHasOptionsMenu(true);

        title = (TextView) rootView.findViewById(R.id.title);
        date = (TextView) rootView.findViewById(R.id.date);
        alt = (TextView) rootView.findViewById(R.id.alt);
        img = (ImageView) rootView.findViewById(R.id.img);
        favourite = (ImageView) rootView.findViewById(R.id.favourite);
        browser = rootView.findViewById(R.id.open_in_browser);
        transcript = rootView.findViewById(R.id.transcript);
        imgContainer = rootView.findViewById(R.id.img_container);

        browser.setOnClickListener(this);
        transcript.setOnClickListener(this);
        imgContainer.setOnClickListener(this);
        favourite.setOnClickListener(this);

        databaseManager = new DatabaseManager(getActivity());
        simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy (EEEE)", Locale.getDefault());

        OkHttpClient picassoClient = BlipApplication.getInstance().client.clone();
        picassoClient.interceptors().add(BlipUtils.REWRITE_CACHE_CONTROL_INTERCEPTOR);
        new Picasso.Builder(getActivity()).downloader(new OkHttpDownloader(picassoClient)).build();

        loadComic();

        return rootView;
    }

    private void loadComic() {
        int random = BlipUtils.randInt(1, databaseManager.getMax());
        comic = databaseManager.getComic(random);

        title.setText(comic.getTitle());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(comic.getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(comic.getMonth()) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(comic.getDay()));
        date.setText(simpleDateFormat.format(calendar.getTime()));
        alt.setText(comic.getAlt());

        Picasso.with(getActivity())
                .load(comic.getImg())
                .error(R.drawable.error_network)
                .into(img);
        if (comic.isFavourite()) {
            favourite.setColorFilter(getResources().getColor(R.color.accent));
        } else {
            favourite.setColorFilter(getResources().getColor(R.color.icons_dark));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_random, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.random) {
            loadComic();
            return true;
        } else if (item.getItemId() == R.id.search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_in_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://xkcd.com/" + comic.getNum()));
                startActivity(intent);
                break;
            case R.id.transcript:
                String content = comic.getTranscript();
                if (content.equals("")) {
                    content = getResources().getString(R.string.message_no_transcript);
                }
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.title_dialog_transcript)
                        .content(content)
                        .negativeText(R.string.negative_text_dialog)
                        .show();
                break;
            case R.id.img_container:
                ImageActivity.launch((AppCompatActivity) getActivity(), img, comic.getNum());
                break;
            case R.id.favourite:
                boolean fav = comic.isFavourite();
                databaseManager.setFavourite(comic.getNum(), !fav);
                if (fav) {
                    //remove from fav
                    favourite.setColorFilter(getResources().getColor(R.color.icons_dark));
                } else {
                    //make fav
                    favourite.setColorFilter(getResources().getColor(R.color.accent));
                }
                break;
        }
    }

}
