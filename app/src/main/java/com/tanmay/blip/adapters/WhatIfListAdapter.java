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
import com.tanmay.blip.models.WhatIf;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WhatIfListAdapter extends RecyclerView.Adapter<WhatIfListAdapter.ViewHolder> {

    public interface WhatIfClickListener {
        void onItemClick(int position);
    }

    private List<WhatIf> mWhatIfList;
    private WhatIfClickListener mListener;

    public WhatIfListAdapter(List<WhatIf> whatIfs, WhatIfClickListener listener) {
        mWhatIfList = whatIfs;
        mListener = listener;
    }

    public void reverseData() {
        List<WhatIf> tempList = new ArrayList<>();
        for (int i = mWhatIfList.size() - 1; i >= 0; i--) {
            tempList.add(mWhatIfList.get(i));
        }
        mWhatIfList = tempList;
        notifyDataSetChanged();
    }

    public WhatIf getItem(int position) {
        return mWhatIfList.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_what_if, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(mWhatIfList.get(position).getTitle());
        holder.date.setText(mWhatIfList.get(position).getDate());
        Picasso.with(holder.thumbnail.getContext()).load(mWhatIfList.get(position).getThumbnail()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return mWhatIfList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.date)
        TextView date;
        @Bind(R.id.thumbnail)
        ImageView thumbnail;
        @Bind(R.id.parent)
        View parent;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.parent)
        public void onParentClick() {
            mListener.onItemClick(getAdapterPosition());
        }
    }

}
