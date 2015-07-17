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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import java.util.List;
import java.util.Locale;

public class FavouritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private View noFavs;
    private DatabaseManager databaseManager;
    private FavouritesListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favourite, container, false);

        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        StaggeredGridLayoutManager layoutManager;
        if (getResources().getBoolean(R.bool.landscape)) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        noFavs = rootView.findViewById(R.id.no_favs);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (databaseManager == null) {
            databaseManager = new DatabaseManager(getActivity());
        }

        if (recyclerView.getAdapter() == null) {
            adapter = new FavouritesListAdapter();
            recyclerView.setAdapter(adapter);
            noFavs.setVisibility(View.GONE);
        } else {
            adapter.notifyDataSetChanged();
        }

        if (recyclerView.getAdapter().getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            noFavs.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            return true;
        } else if (item.getItemId() == R.id.about) {
            startActivity(new Intent(getActivity(), AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FavouritesListAdapter extends RecyclerView.Adapter<FavouritesListAdapter.ViewHolder> {

        private List<Comic> comics;
        private SimpleDateFormat simpleDateFormat;

        public FavouritesListAdapter() {
            updateList();
            simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy (EEEE)", Locale.getDefault());
            OkHttpClient picassoClient = BlipApplication.getInstance().client.clone();
            picassoClient.interceptors().add(BlipUtils.REWRITE_CACHE_CONTROL_INTERCEPTOR);
            new Picasso.Builder(getActivity()).downloader(new OkHttpDownloader(picassoClient)).build();
        }

        public void updateList() {
            comics = databaseManager.getFavourites();
            if (getItemCount() == 0) {
                recyclerView.setVisibility(View.GONE);
                noFavs.setVisibility(View.VISIBLE);
            } else {
                notifyDataSetChanged();
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.item_comic, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Comic comic = comics.get(position);

            holder.title.setText(comic.getNum() + ". " + comic.getTitle());

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(comic.getYear()));
            calendar.set(Calendar.MONTH, Integer.parseInt(comic.getMonth()) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(comic.getDay()));
            holder.date.setText(simpleDateFormat.format(calendar.getTime()));

            holder.alt.setText(comic.getAlt());

            Picasso.with(holder.img.getContext())
                    .load(comic.getImg())
                    .error(R.drawable.error_network)
                    .into(holder.img);

            if (comic.isFavourite()) {
                holder.favourite.setColorFilter(getResources().getColor(R.color.accent));
            } else {
                holder.favourite.setColorFilter(getResources().getColor(R.color.icons_dark));
            }
        }

        @Override
        public int getItemCount() {
            return comics.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView title, date, alt;
            ImageView img, favourite;
            View browser, transcript, imgContainer, share, explain;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title);
                date = (TextView) itemView.findViewById(R.id.date);
                alt = (TextView) itemView.findViewById(R.id.alt);
                img = (ImageView) itemView.findViewById(R.id.img);
                favourite = (ImageView) itemView.findViewById(R.id.favourite);
                browser = itemView.findViewById(R.id.open_in_browser);
                transcript = itemView.findViewById(R.id.transcript);
                imgContainer = itemView.findViewById(R.id.img_container);
                share = itemView.findViewById(R.id.share);
                explain = itemView.findViewById(R.id.help);

                browser.setOnClickListener(this);
                transcript.setOnClickListener(this);
                imgContainer.setOnClickListener(this);
                favourite.setOnClickListener(this);
                share.setOnClickListener(this);
                explain.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                final int position = getAdapterPosition();
                switch (v.getId()) {
                    case R.id.open_in_browser:
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://xkcd.com/" + comics.get(position).getNum()));
                        startActivity(intent);
                        break;
                    case R.id.transcript:
                        String content = comics.get(position).getTranscript();
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
                        ImageActivity.launch((AppCompatActivity) getActivity(), img, comics.get(position).getNum());
                        break;
                    case R.id.favourite:
                        boolean fav = comics.get(position).isFavourite();
                        comics.get(position).setFavourite(!fav);
                        databaseManager.setFavourite(comics.get(position).getNum(), !fav);
                        updateList();
                        break;
                    case R.id.help:
                        Intent explainIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + comics.get(position).getNum()));
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
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, comics.get(position).getTitle());
                        shareIntent.putExtra(Intent.EXTRA_TEXT, comics.get(position).getImg());
                        startActivity(Intent.createChooser(shareIntent, getActivity().getResources().getString(R.string.tip_share_image_url)));
                        break;
                }
            }
        }

    }
}
