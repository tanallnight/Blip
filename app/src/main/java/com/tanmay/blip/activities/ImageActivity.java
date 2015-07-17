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

package com.tanmay.blip.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tanmay.blip.R;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.utils.BlipUtils;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends AppCompatActivity implements PhotoViewAttacher.OnPhotoTapListener, View.OnClickListener {

    public static final String EXTRA_NUM = "num";
    public static final String EXTRA_IMAGE = "comic";
    ImageView photo;
    private DatabaseManager databaseManager;
    private PhotoViewAttacher photoViewAttacher;
    private View topBar, close;
    private TextView title, number;

    public static void launch(AppCompatActivity compatActivity, View transistionView, int num) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(compatActivity, transistionView, EXTRA_IMAGE);
        Intent intent = new Intent(compatActivity, ImageActivity.class);
        intent.putExtra(EXTRA_NUM, num);
        ActivityCompat.startActivity(compatActivity, intent, options.toBundle());
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        if (BlipUtils.isLollopopUp()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        databaseManager = new DatabaseManager(this);
        int num = getIntent().getExtras().getInt(EXTRA_NUM);
        final Comic comic = databaseManager.getComic(num);

        topBar = findViewById(R.id.topBar);
        close = findViewById(R.id.close);
        title = (TextView) findViewById(R.id.title);
        number = (TextView) findViewById(R.id.number);

        number.setText(String.valueOf(num));
        close.setOnClickListener(this);
        title.setText(comic.getTitle());
        photo = (ImageView) findViewById(R.id.img);
        topBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                topBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (BlipUtils.isLollopopUp()) {
                    BlipUtils.setMargins(topBar, 0, getStatusBarHeight(), 0, 0);
                    if (getResources().getBoolean(R.bool.landscape)) {
                        photo.setPadding(0, getStatusBarHeight() + topBar.getHeight(), 0, 0);
                    } else {
                        photo.setPadding(0, getStatusBarHeight() + topBar.getHeight(), 0, getNavigationBarHeight());
                    }
                } else {
                    photo.setPadding(0, topBar.getHeight(), 0, 0);
                }
                Picasso.with(ImageActivity.this).load(comic.getImg()).into(photo);
                photoViewAttacher = new PhotoViewAttacher(photo);
                photoViewAttacher.setOnPhotoTapListener(ImageActivity.this);
                photoViewAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
                topBar.setTranslationY(-(topBar.getHeight() + getStatusBarHeight()));
                topBar.animate().translationY(0).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        photoViewAttacher.cleanup();
    }

    private int getNavigationBarHeight() {
        int id = getResources().getIdentifier("navigation_bar_height",
                "dimen", "android");
        if (id > 0) {
            return getResources().getDimensionPixelSize(id);
        }
        return 0;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        topBar.animate().translationY(-(topBar.getHeight() + getStatusBarHeight()))
                .setDuration(500).setInterpolator(new AccelerateInterpolator()).start();
        super.onBackPressed();
    }

    @Override
    public void onPhotoTap(View view, float v, float v1) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }
}
