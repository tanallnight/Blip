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

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tanmay.blip.BlipApplication;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.database.SharedPrefs;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.utils.UnicodeUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class XKCDDownloader extends IntentService {

    public static final String LATEST_URL = "http://xkcd.com/info.0.json";
    public static final String COMICS_URL = "http://xkcd.com/%d/info.0.json";

    public static final String DOWNLOAD_PROGRESS = "com.tanmay.blip.DOWNLOAD_PROGRESS";
    public static final String DOWNLOAD_SUCCESS = "com.tanmay.blip.DOWNLOAD_SUCCESS";
    public static final String DOWNLOAD_FAIL = "com.tanmay.blip.DOWNLOAD_FAIL";

    public static final String DOWNLOAD_TODAY = "com.tanmay.blip.DOWNLOAD_TODAY";
    public static final String DOWNLOAD_ALL = "com.tanmay.blip.DOWNLOAD_ALL";
    public static final String DOWNLOAD_SPECIFIC = "com.tanmay.blip.DOWNLOAD_SPECIFIC";
    public static final String DOWNLOAD_TRANSCRIPT = "com.tanmay.blip.DOWNLOAD_TRANSCRIPT";
    public static final String DOWNLOAD_LAST_TEN = "com.tanmay.blip.DOWNLOAD_LAST_TEN";

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
                downloadSpecific(intent.getExtras().getInt(COMIC_NUM));
                break;
            case DOWNLOAD_TRANSCRIPT:
                downloadAllMissingTranscripts();
                break;
            case DOWNLOAD_LAST_TEN:
                redownloadLastTen();
        }
    }

    private void downloadSpecific(int i) {
        Gson gson = new Gson();
        DatabaseManager databaseManager = new DatabaseManager(this);
        String url = String.format(COMICS_URL, i);
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = BlipApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();
            String responseBody = UnicodeUtils.unescapeUTF8(response.body().string());
            Comic comic = gson.fromJson(responseBody, Comic.class);
            databaseManager.addComic(comic);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } catch (IOException e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

    private void downloadToday() {
        DatabaseManager databaseManager = new DatabaseManager(this);
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
    }

    private void redownloadLastTen() {
        final Gson gson = new Gson();
        final DatabaseManager databaseManager = new DatabaseManager(this);
        try {
            Request todayReq = new Request.Builder().url(LATEST_URL).build();
            Response response = BlipApplication.getInstance().client.newCall(todayReq).execute();
            if (!response.isSuccessful()) throw new IOException();
            Comic comic = gson.fromJson(response.body().string(), Comic.class);
            final CountDownLatch latch = new CountDownLatch(10);
            final Executor executor = Executors.newFixedThreadPool(5);
            int num = comic.getNum();
            for (int i = num - 9; i <= num; i++) {
                final int index = i;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = String.format(COMICS_URL, index);
                            Request request = new Request.Builder().url(url).build();
                            Response response = BlipApplication.getInstance().client.newCall(request).execute();
                            if (!response.isSuccessful()) throw new IOException();
                            String responseBody = UnicodeUtils.unescapeUTF8(response.body().string());
                            Comic comic = null;
                            try {
                                comic = gson.fromJson(responseBody, Comic.class);
                            } catch (JsonSyntaxException e) {
                                Crashlytics.log(1, "XKCDDownloader", e.getMessage() + " POS:" + index);
                            }
                            if (comic != null) {
                                if (databaseManager.comicExists(comic)) {
                                    databaseManager.updateComic(comic);
                                } else {
                                    databaseManager.addComic(comic);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            LocalBroadcastManager.getInstance(XKCDDownloader.this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                LocalBroadcastManager.getInstance(XKCDDownloader.this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
            }

            SharedPrefs.getInstance().setLastRedownladTime(System.currentTimeMillis());
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));

        } catch (IOException e) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

    private void downloadAllMissingTranscripts() {
        final Gson gson = new Gson();
        final DatabaseManager databaseManager = new DatabaseManager(this);
        List<Integer> nums = databaseManager.getAllMissingTranscripts();
        if (nums.size() > 1) {
            final CountDownLatch latch = new CountDownLatch(nums.size());
            final Executor executor = Executors.newFixedThreadPool(nums.size() / 2);
            for (int i : nums) {
                final int index = i;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = String.format(COMICS_URL, index);
                            Request request = new Request.Builder().url(url).build();
                            Response response = BlipApplication.getInstance().client.newCall(request).execute();
                            if (!response.isSuccessful()) throw new IOException();
                            String responseBody = UnicodeUtils.unescapeUTF8(response.body().string());
                            Comic comic = null;
                            try {
                                comic = gson.fromJson(responseBody, Comic.class);
                            } catch (JsonSyntaxException e) {
                                Crashlytics.log(1, "XKCDDownloader", e.getMessage() + " POS:" + index);
                            }
                            if (comic != null) {
                                databaseManager.updateComic(comic);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            LocalBroadcastManager.getInstance(XKCDDownloader.this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                LocalBroadcastManager.getInstance(XKCDDownloader.this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
            }
            SharedPrefs.getInstance().setLastTranscriptCheckTime(System.currentTimeMillis());
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } else {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        }
    }

    private void downloadAll() {
        final Gson gson = new Gson();
        final DatabaseManager databaseManager = new DatabaseManager(this);
        Request request = new Request.Builder().url(LATEST_URL).build();
        try {
            final Response response = BlipApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();
            Comic comic = gson.fromJson(response.body().string(), Comic.class);

            final int num = comic.getNum();
            final CountDownLatch latch = new CountDownLatch(num);
            final Executor executor = Executors.newFixedThreadPool(10);

            for (int i = 1; i <= num; i++) {
                final int index = i;
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (index != 404) {
                                String url = String.format(COMICS_URL, index);
                                Request newReq = new Request.Builder().url(url).build();
                                Response newResp = BlipApplication.getInstance().client.newCall(newReq).execute();
                                if (!newResp.isSuccessful()) throw new IOException();
                                String resp = UnicodeUtils.unescapeUTF8(newResp.body().string());
                                Comic comic1 = null;
                                try {
                                    comic1 = gson.fromJson(resp, Comic.class);
                                } catch (JsonSyntaxException e) {
                                    Crashlytics.log(1, "XKCDDownloader", e.getMessage() + " POS:" + index);
                                }
                                if (comic1 != null) {
                                    databaseManager.addComic(comic1);
                                    double progress = ((double) databaseManager.getCount() / num) * 100;
                                    Intent intent = new Intent(DOWNLOAD_PROGRESS);
                                    intent.putExtra(PROGRESS, progress);
                                    intent.putExtra(TITLE, comic1.getTitle());
                                    LocalBroadcastManager.getInstance(XKCDDownloader.this).sendBroadcast(intent);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            LocalBroadcastManager.getInstance(XKCDDownloader.this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_SUCCESS));
        } catch (IOException e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_FAIL));
        }
    }

}
