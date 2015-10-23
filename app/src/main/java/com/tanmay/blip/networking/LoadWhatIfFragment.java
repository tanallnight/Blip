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

import com.tanmay.blip.models.WhatIf;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class LoadWhatIfFragment extends Fragment {

    public interface WhatIfLoadListener {
        void onLoad(String html);

        void onFail(Exception e);
    }

    public static final String EXTRA_WHAT_IF = "NUM";

    private WhatIfLoadListener mListener;
    private WhatIf whatIf;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WhatIfLoadListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Did you forget to implement the interface in the activity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle bundle = getArguments();
        whatIf = bundle.getParcelable(EXTRA_WHAT_IF);
        new WhatIfLoadingTask().execute();
    }

    public class WhatIfLoadingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document document = Jsoup.connect(whatIf.getHtml()).get();
                Element article = document.tagName("article");
                mListener.onLoad(article.html());
            } catch (IOException e) {
                e.printStackTrace();
                mListener.onFail(e);
            }
            return null;
        }
    }
}
