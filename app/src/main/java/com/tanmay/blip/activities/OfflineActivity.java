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

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.tanmay.blip.R;
import com.tanmay.blip.networking.DownloadAllFragment;
import com.tanmay.blip.utils.BlipUtils;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OfflineActivity extends AppCompatActivity implements DownloadAllFragment.DownloadListener {

    public static final String DOWNLOAD_FRAGMENT = "DOWNLOAD_FRAGMENT";

    @Bind(R.id.home)
    ImageView mHome;
    @Bind(R.id.tip_text)
    TextView mTip;
    @Bind(R.id.start_download)
    FloatingActionButton mStart;
    @Bind(R.id.fabProgressCircle)
    FABProgressCircle mProgress;

    @BindString(R.string.tip_download_success)
    String success;
    @BindString(R.string.tip_download_fail)
    String fail;
    @BindString(R.string.tip_download_incomplete)
    String incomplete;
    @BindString(R.string.tip_download_complete)
    String complete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        ButterKnife.bind(this);
        if (BlipUtils.isLollopopUp()) {
            getWindow().setStatusBarColor(Color.parseColor("#1565C0"));
        }
    }

    @OnClick(R.id.start_download)
    public void onStartClick() {
        getSupportFragmentManager().beginTransaction().add(new DownloadAllFragment(), DOWNLOAD_FRAGMENT).commit();
    }

    @OnClick(R.id.home)
    public void onBackClick() {
        onBackPressed();
    }

    @Override
    public void onDownloadStart() {
        mProgress.show();
    }

    @Override
    public void onDownloadSuccess() {
        mProgress.beginFinalAnimation();
        mTip.setText(success);
    }

    @Override
    public void onDownloadFail() {
        mTip.setText(fail);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    @Override
    public void onDownloadProgress(int progress) {
        mTip.setText(progress + "%");
    }
}
