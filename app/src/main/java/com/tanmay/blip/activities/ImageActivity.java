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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tanmay.blip.R;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.models.Comic;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends AppCompatActivity {

    public static final String EXTRA_NUM = "num";
    public static final String EXTRA_IMAGE = "comic";
    private DatabaseManager databaseManager;

    public static void launch(AppCompatActivity compatActivity, View transistionView, int num) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(compatActivity, transistionView, EXTRA_IMAGE);
        Intent intent = new Intent(compatActivity, ImageActivity.class);
        intent.putExtra(EXTRA_NUM, num);
        ActivityCompat.startActivity(compatActivity, intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        databaseManager = new DatabaseManager(this);
        int num = getIntent().getExtras().getInt(EXTRA_NUM);
        Comic comic = databaseManager.getComic(num);

        ImageView imageView = (ImageView) findViewById(R.id.img);
        ViewCompat.setTransitionName(imageView, EXTRA_IMAGE);
        imageView.setPadding(0, getStatusBarHeight(), 0, getNavigationBarHeight());
        Picasso.with(this).load(comic.getImg()).into(imageView);
        new PhotoViewAttacher(imageView);
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

}
