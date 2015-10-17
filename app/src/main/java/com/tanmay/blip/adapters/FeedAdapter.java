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

package com.tanmay.blip.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tanmay.blip.R;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.utils.BlipUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private List<Comic> mComicsList;
    private ItemClickListener mCallbackListener;
    private SimpleDateFormat mSimpleDateFormat;
    private Calendar mCalendar;

    public FeedAdapter(List<Comic> comicsList, ItemClickListener callbackListener) {
        mComicsList = comicsList;
        mCallbackListener = callbackListener;
        mCalendar = Calendar.getInstance();
        mSimpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy (EEEE)", Locale.getDefault());
    }

    public void addComics(List<Comic> comicList) {
        int posStart = mComicsList.size();
        mComicsList.addAll(comicList);
        notifyItemRangeInserted(posStart, comicList.size());
    }

    public void addComic(Comic comic) {
        int insertPosition = mComicsList.size();
        mComicsList.add(comic);
        notifyItemInserted(insertPosition);
    }

    public void updateItem(Comic comic, int position) {
        mComicsList.set(position, comic);
        notifyItemChanged(position);
    }

    public void changeFavourite(int position) {
        mComicsList.get(position).setFavourite(!mComicsList.get(position).isFavourite());
        notifyItemChanged(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comic comic = mComicsList.get(position);

        holder.mTitle.setText(comic.getTitle());
        holder.mSubhead.setText(BlipUtils.createSubhead(comic, mCalendar, mSimpleDateFormat));
        if (comic.isFavourite()) {
            holder.mFavourite.setText(holder.unfavourite);
        } else {
            holder.mFavourite.setText(holder.favourite);
        }
        Picasso.with(holder.mImage.getContext()).load(comic.getImg()).error(R.drawable.error_network).into(holder.mImage);
    }

    @Override
    public int getItemCount() {
        return mComicsList.size();
    }

    public interface ItemClickListener {
        void onImageClick(int position, int num);

        void onFavouriteClick(int position, boolean isFavourite, int num);

        void onShareClick(Comic comic);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

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

        @BindString(R.string.action_favourite)
        String favourite;
        @BindString(R.string.action_unfavourite)
        String unfavourite;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick({R.id.image, R.id.share, R.id.favourite})
        void onClick(View view) {
            int position = getAdapterPosition();
            switch (view.getId()) {
                case R.id.image:
                    mCallbackListener.onImageClick(position, mComicsList.get(position).getNum());
                    break;
                case R.id.share:
                    mCallbackListener.onShareClick(mComicsList.get(position));
                    break;
                case R.id.favourite:
                    mCallbackListener.onFavouriteClick(position,
                            mComicsList.get(position).isFavourite(),
                            mComicsList.get(position).getNum());
                    break;
            }
        }

    }

}
