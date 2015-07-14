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

package com.tanmay.blip.networking;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tanmay.blip.BlipApplication;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.models.Comic;

import java.io.IOException;
import java.util.Calendar;

public class XKCDDownloader extends IntentService {

    public static final String LATEST_URL = "http://xkcd.com/info.0.json";
    public static final String COMICS_URL = "http://xkcd.com/%d/info.0.json";

    public static final String DOWNLOAD_PROGRESS = "com.tanmay.blip.DOWNLOAD_PROGRESS";
    public static final String DOWNLOAD_SUCCESS = "com.tanmay.blip.DOWNLOAD_SUCCESS";
    public static final String DOWNLOAD_FAIL = "com.tanmay.blip.DOWNLOAD_FAIL";

    public static final String DOWNLOAD_TODAY = "com.tanmay.blip.DOWNLOAD_TODAY";
    public static final String DOWNLOAD_ALL = "com.tanmay.blip.DOWNLOAD_ALL";
    public static final String DOWNLOAD_SPECIFIC = "com.tanmay.blip.DOWNLOAD_SPECIFIC";

    public static final String COMIC_NUM = "comicNum";
    public static final String PROGRESS = "progress";
    public static final String TITLE = "title";

    public XKCDDownloader() {
        super("XKCDDownloaderService");
    }

    public XKCDDownloader(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case DOWNLOAD_TODAY:
                downloadToday();
                break;
            case DOWNLOAD_ALL:
                downloadAll();
                break;
            case DOWNLOAD_SPECIFIC:
                downloadSpecifice(intent.getExtras().getInt(COMIC_NUM));
                break;
        }
    }

    private void downloadSpecifice(int i) {
        Gson gson = new Gson();
        DatabaseManager databaseManager = new DatabaseManager(this);
        String url = String.format(COMICS_URL, i);
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = BlipApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();
            Comic comic = gson.fromJson(response.body().string(), Comic.class);
            databaseManager.addComic(comic);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } catch (IOException e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

    private void downloadToday() {
        DatabaseManager databaseManager = new DatabaseManager(this);
        if (isStale(databaseManager)) {
            Gson gson = new Gson();
            Request request = new Request.Builder().url(LATEST_URL).build();
            try {
                Response response = BlipApplication.getInstance().client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException();
                Comic comic = gson.fromJson(response.body().string(), Comic.class);
                if (!databaseManager.comicExists(comic))
                    databaseManager.addComic(comic);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
            } catch (IOException e) {
                e.printStackTrace();
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
            }
        } else {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        }
    }

    private void downloadAll() {
        Gson gson = new Gson();
        DatabaseManager databaseManager = new DatabaseManager(this);
        Request request = new Request.Builder().url(LATEST_URL).build();
        try {
            Response response = BlipApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();
            Comic comic = gson.fromJson(response.body().string(), Comic.class);
            int num = comic.getNum();
            for (int i = 1; i <= num; i++) {
                if (i != 404) {
                    String url = String.format(COMICS_URL, i);
                    Request newReq = new Request.Builder().url(url).build();
                    Response newResp = BlipApplication.getInstance().client.newCall(newReq).execute();
                    if (!response.isSuccessful()) throw new IOException();
                    String resp = newResp.body().string();
                    Comic comic1 = gson.fromJson(resp, Comic.class);
                    databaseManager.addComic(comic1);
                    double progress = ((double) i / num) * 100;
                    Intent intent = new Intent(DOWNLOAD_PROGRESS);
                    intent.putExtra(PROGRESS, progress);
                    intent.putExtra(TITLE, comic1.getTitle());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } catch (IOException e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

    private boolean isStale(DatabaseManager databaseManager) {
        Calendar calendar = Calendar.getInstance();
        boolean updateDay = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ||
                calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ||
                calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
        boolean existsInDB = databaseManager.dateExists(calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
        return updateDay && !existsInDB;
    }

}
