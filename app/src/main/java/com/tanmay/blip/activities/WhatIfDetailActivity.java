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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.tanmay.blip.R;
import com.tanmay.blip.models.WhatIf;
import com.tanmay.blip.networking.LoadWhatIfFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WhatIfDetailActivity extends BaseActivity implements LoadWhatIfFragment.WhatIfLoadListener {

    private static final String WHAT_IF_EXTRA = "ITEM_EXTRA";

    @Bind(R.id.webview)
    WebView webView;

    @Override
    protected int getToolbarColor() {
        return getResources().getColor(R.color.primary);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_what_if_detail;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    public static void launch(WhatIf whatIf, Activity activity) {
        Intent intent = new Intent(activity, WhatIfDetailActivity.class);
        intent.putExtra(WHAT_IF_EXTRA, whatIf);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setStatusBarColor(R.color.primary_dark);

        WhatIf whatIf = getIntent().getParcelableExtra(WHAT_IF_EXTRA);

        webView.loadUrl(whatIf.getHtml());

       /* Bundle bundle = new Bundle();
        bundle.putParcelable(LoadWhatIfFragment.EXTRA_WHAT_IF, whatIf);
        LoadWhatIfFragment fragment = new LoadWhatIfFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(fragment, "LOADER").commit();*/
    }


    @Override
    public void onLoad(final String html) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
            }
        });
    }

    @Override
    public void onFail(Exception e) {

    }
}
