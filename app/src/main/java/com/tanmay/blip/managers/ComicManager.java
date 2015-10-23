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

package com.tanmay.blip.managers;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tanmay.blip.BlipApplication;
import com.tanmay.blip.models.Comic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComicManager {

    public static final int COMICS_TO_DOWNLOAD = 8;
    private static final String LATEST_URL = "http://xkcd.com/info.0.json";
    private static final String COMICS_URL = "http://xkcd.com/%d/info.0.json";
    private DatabaseManager mDatabaseManager;
    private SharedPrefs mSharedPrefs;
    private ComicLoadListener mLoadListener;
    private Gson mGson;

    public ComicManager(Context context) {
        mDatabaseManager = new DatabaseManager(context);
        mSharedPrefs = SharedPrefs.getInstance();
        mGson = new Gson();
    }

    public Comic loadComic(int num) throws IOException {
        if (mDatabaseManager.comicExists(num)) {
            return mDatabaseManager.getComic(num);
        }
        String url = makeURL(num);
        Request request = new Request.Builder().url(url).build();
        Response response = BlipApplication.getInstance().client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Download Fail");
        String responseBody = response.body().string();
        Comic comic = mGson.fromJson(responseBody, Comic.class);
        mDatabaseManager.addComic(comic);
        return comic;
    }

    public void loadComic(int num, ComicLoadListener loadListener) {
        mLoadListener = loadListener;
        if (mDatabaseManager.comicExists(num)) {
            mLoadListener.onLoadSuccess(mDatabaseManager.getComic(num));
            return;
        }
        String url = makeURL(num);
        Request request = new Request.Builder().url(url).build();
        BlipApplication.getInstance().client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                String responseText = response.body().string();
                Comic comic = mGson.fromJson(responseText, Comic.class);
                mDatabaseManager.addComic(comic);
                mLoadListener.onLoadSuccess(comic);
            }

            @Override
            public void onFailure(Request request, IOException e) {
                mLoadListener.onLoadFail();
            }
        });
    }

    public void getLatestComic(ComicLoadListener loadListener) {
        mLoadListener = loadListener;
        Request request = new Request.Builder().url(LATEST_URL).build();
        BlipApplication.getInstance().client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                Comic comic = mGson.fromJson(response.body().string(), Comic.class);
                mDatabaseManager.addComic(comic);
                mLoadListener.onLoadSuccess(comic);
            }

            @Override
            public void onFailure(Request request, IOException e) {
                mLoadListener.onLoadFail();
            }
        });
    }

    public int getLatestComicNumOffline() {
        return mSharedPrefs.getLatestComicNum();
    }

    public int getLatestComicNum() {
        int comicNum = mSharedPrefs.getLatestComicNum();
        Request request = new Request.Builder().url(LATEST_URL).build();
        try {
            OkHttpClient client = BlipApplication.getInstance().client.clone();
            client.setConnectTimeout(5, TimeUnit.SECONDS);
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();
            Comic comic = mGson.fromJson(response.body().string(), Comic.class);
            if (comic.getNum() != comicNum) {
                comicNum = comic.getNum();
                mSharedPrefs.setLatestComicNum(comicNum);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return comicNum;
    }

    public void getLatestComicNum(final LatestComicLoadListener listener) {
        Request request = new Request.Builder().url(LATEST_URL).build();
        BlipApplication.getInstance().client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                listener.onLoad(mSharedPrefs.getLatestComicNum());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Comic comic = mGson.fromJson(response.body().string(), Comic.class);
                mSharedPrefs.setLatestComicNum(comic.getNum());
                listener.onLoad(comic.getNum());
            }
        });
    }

    public List<Comic> getFavourites() {
        return mDatabaseManager.getFavourites();
    }

    public void loadBatch(int start, int count, ComicMultiLoadListener listener) {
        new BatchDownloadTask(start, count, listener).execute();
    }

    public void updateFavouriteStatus(int num, boolean isFavourite) {
        mDatabaseManager.setFavourite(num, isFavourite);
    }

    private String makeURL(int num) {
        return String.format(COMICS_URL, num);
    }

    public void cleanUp() {
        mDatabaseManager = null;
        mLoadListener = null;
    }

    public interface ComicLoadListener {
        void onLoadSuccess(Comic comic);

        void onLoadFail();
    }

    public interface ComicMultiLoadListener {
        void onLoadSuccess(List<Comic> comics);
    }

    public interface LatestComicLoadListener {
        void onLoad(int num);
    }

    private class BatchDownloadTask extends AsyncTask<Void, Void, Void> {

        private int mStartNumber;
        private int mNumberOfComics;

        private ComicMultiLoadListener mListener;
        private List<Comic> mResultsList;

        public BatchDownloadTask(int startNumber, int numberOfComics, ComicMultiLoadListener listener) {
            mStartNumber = startNumber;
            mNumberOfComics = numberOfComics;
            mListener = listener;
            mResultsList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = mStartNumber; i > mStartNumber - mNumberOfComics; i--) {
                if (i != 404) {
                    try {
                        Comic comic = loadComic(i);
                        mResultsList.add(comic);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListener.onLoadSuccess(mResultsList);
        }
    }
}
