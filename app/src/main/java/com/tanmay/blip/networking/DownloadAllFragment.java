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

package com.tanmay.blip.networking;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.tanmay.blip.managers.ComicManager;

import java.io.IOException;

public class DownloadAllFragment extends Fragment {

    private DownloadListener mListener;
    private ComicManager mComicManager;
    private DownloadComicsTask mTask;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DownloadListener) activity;
        } catch (ClassCastException ignored) {
            throw new ClassCastException("Did you forget to implement the DownloadListener interface?");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mComicManager = new ComicManager(getContext());
        mTask = new DownloadComicsTask();
        mTask.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mComicManager.cleanUp();
    }

    public interface DownloadListener {
        void onDownloadStart();

        void onDownloadSuccess();

        void onDownloadFail();

        void onDownloadProgress(int progress);
    }

    public class DownloadComicsTask extends AsyncTask<Void, Integer, Void> {

        private int mLatestComicNum;
        private boolean mFailed = false;

        public DownloadComicsTask() {
            mLatestComicNum = mComicManager.getLatestComicNum();
            Log.i("DOWNLOAD", "Latest Comic: " + mLatestComicNum);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListener.onDownloadStart();
        }

        @Override
        protected Void doInBackground(Void... params) {
            int exceptions = 0;
            for (int i = 0; i <= mLatestComicNum; i++) {
                if (i != 404) {
                    try {
                        mComicManager.loadComic(i);
                        publishProgress((i * 100) / mLatestComicNum);
                    } catch (IOException ignored) {
                        exceptions++;
                        if (exceptions == 5) {
                            mFailed = true;
                            mListener.onDownloadFail();
                            return null;
                        }
                        continue;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mListener.onDownloadProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!mFailed)
                mListener.onDownloadSuccess();
        }
    }
}
