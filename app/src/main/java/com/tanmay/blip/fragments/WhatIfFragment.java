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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tanmay.blip.R;
import com.tanmay.blip.activities.WhatIfDetailActivity;
import com.tanmay.blip.adapters.WhatIfListAdapter;
import com.tanmay.blip.models.WhatIf;
import com.tanmay.blip.networking.WhatIfIntentService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WhatIfFragment extends Fragment implements WhatIfListAdapter.WhatIfClickListener {

    @Bind(R.id.recyclerview)
    RecyclerView mListView;

    private WhatIfListAdapter mAdapter;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WhatIfIntentService.WHAT_IF_RESULT)) {
                List<WhatIf> whatIfs = intent.getParcelableArrayListExtra(WhatIfIntentService.KEY_RESULT);
                mAdapter = new WhatIfListAdapter(whatIfs, WhatIfFragment.this);
                mListView.setAdapter(mAdapter);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_what_if, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter(WhatIfIntentService.WHAT_IF_RESULT));

        mListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mListView.setItemAnimator(new DefaultItemAnimator());

        Intent intent = new Intent(getActivity(), WhatIfIntentService.class);
        getActivity().startService(intent);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onItemClick(int position) {
        if (mAdapter != null) {
            WhatIfDetailActivity.launch(mAdapter.getItem(position), getActivity());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_what_if, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.resort) {
            if (mAdapter != null) {
                mAdapter.reverseData();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
