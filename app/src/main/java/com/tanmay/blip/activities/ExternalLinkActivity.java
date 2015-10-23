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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tanmay.blip.managers.ComicManager;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.utils.BlipUtils;

public class ExternalLinkActivity extends AppCompatActivity implements ComicManager.ComicLoadListener {

    private ComicManager mComicManager;
    int urlNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String URL = getIntent().getDataString();
        String[] splitURL = URL.split(".com/");
        if (splitURL.length < 2) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        String numString = splitURL[1];
        numString = numString.replace("/", "");
        if (!BlipUtils.isNumeric(numString)) {
            finish();
            Toast.makeText(this, "URL is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        urlNum = Integer.parseInt(numString);

        mComicManager = new ComicManager(this);
        mComicManager.getLatestComic(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mComicManager.cleanUp();
    }

    @Override
    public void onLoadSuccess(Comic comic) {
        if (urlNum > comic.getNum() || urlNum == 404 || urlNum < 1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExternalLinkActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            ImageActivity.launch(this, urlNum);
            finish();
        }
    }

    @Override
    public void onLoadFail() {
        finish();
        Toast.makeText(this, "Network error!", Toast.LENGTH_SHORT).show();
    }
}
