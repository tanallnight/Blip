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

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.tanmay.blip.R;
import com.tanmay.blip.activities.AboutActivity;
import com.tanmay.blip.activities.ImageActivity;
import com.tanmay.blip.activities.SearchActivity;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.utils.BlipUtils;
import com.tanmay.blip.utils.SpeechSynthesizer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RandomFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private TextView title, date, alt;
    private ImageView img, favourite;
    private View browser, transcript, imgContainer, share, explain;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseManager databaseManager;
    private SimpleDateFormat simpleDateFormat;
    private Comic comic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_random, container, false);

        setHasOptionsMenu(true);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        title = (TextView) rootView.findViewById(R.id.title);
        date = (TextView) rootView.findViewById(R.id.date);
        alt = (TextView) rootView.findViewById(R.id.alt);
        img = (ImageView) rootView.findViewById(R.id.img);
        favourite = (ImageView) rootView.findViewById(R.id.favourite);
        browser = rootView.findViewById(R.id.open_in_browser);
        transcript = rootView.findViewById(R.id.transcript);
        imgContainer = rootView.findViewById(R.id.img_container);
        share = rootView.findViewById(R.id.share);
        explain = rootView.findViewById(R.id.help);

        browser.setOnClickListener(this);
        transcript.setOnClickListener(this);
        imgContainer.setOnClickListener(this);
        favourite.setOnClickListener(this);
        share.setOnClickListener(this);
        explain.setOnClickListener(this);

        databaseManager = new DatabaseManager(getActivity());
        simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy (EEEE)", Locale.getDefault());

        OkHttpClient picassoClient = BlipApplication.getInstance().client.clone();
        picassoClient.interceptors().add(BlipUtils.REWRITE_CACHE_CONTROL_INTERCEPTOR);
        new Picasso.Builder(getActivity()).downloader(new OkHttpDownloader(picassoClient)).build();

        int num = 0;
        if (savedInstanceState != null)
            num = savedInstanceState.getInt("NUM");
        loadComic(num);

        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    private void loadComic(int num) {
        int random;
        if (num == 0) {
            random = BlipUtils.randInt(1, databaseManager.getMax());
        } else {
            random = num;
        }
        comic = databaseManager.getComic(random);

        title.setText(comic.getNum() + ". " + comic.getTitle());
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
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("NUM", comic.getNum());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_random, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.random) {
            loadComic(0);
            return true;
        } else if (item.getItemId() == R.id.search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            return true;
        } else if (item.getItemId() == R.id.about) {
            startActivity(new Intent(getActivity(), AboutActivity.class));
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
                final String speakingContent = content;
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.title_dialog_transcript)
                        .content(content)
                        .negativeText(R.string.negative_text_dialog)
                        .neutralText(R.string.neutral_text_dialog_speak)
                        .autoDismiss(false)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                dialog.dismiss();
                            }

                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                super.onNeutral(dialog);
                                SpeechSynthesizer.getInstance().convertToSpeechFlush(speakingContent);
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                SpeechSynthesizer.getInstance().stopSpeaking();
                            }
                        })
                        .show();
                break;
            case R.id.img_container:
                ImageActivity.launch((AppCompatActivity) getActivity(), img, comic.getNum());
                break;
            case R.id.favourite:
                boolean fav = comic.isFavourite();
                comic.setFavourite(!fav);
                databaseManager.setFavourite(comic.getNum(), !fav);
                if (fav) {
                    //remove from fav
                    favourite.setColorFilter(getResources().getColor(R.color.icons_dark));
                } else {
                    //make fav
                    favourite.setColorFilter(getResources().getColor(R.color.accent));
                }
                break;
            case R.id.help:
                Intent explainIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + comic.getNum()));
                startActivity(explainIntent);
                break;
            case R.id.share:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                if (BlipUtils.isLollopopUp()) {
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                } else {
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                }
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, comic.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, comic.getImg());
                startActivity(Intent.createChooser(shareIntent, getActivity().getResources().getString(R.string.tip_share_image_url)));
                break;
        }
    }

    @Override
    public void onRefresh() {
        loadComic(0);
    }
}
