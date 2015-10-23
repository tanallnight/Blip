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

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.tanmay.blip.models.WhatIf;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class WhatIfIntentService extends IntentService {

    public static final String WHAT_IF_RESULT = "WHAT_IF_RESULT";
    public static final String KEY_RESULT = "RESULTS";

    public WhatIfIntentService() {
        super("WhatIfIntentService");
    }

    public WhatIfIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Document document = Jsoup.connect("http://what-if.xkcd.com/archive/").get();
            Elements items = document.select("div[class=archive-entry]");
            ArrayList<WhatIf> whatIfs = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                Element item = items.get(i);
                WhatIf whatIf = new WhatIf();
                whatIf.setHtml("http:" + item.child(0).attr("href"));
                whatIf.setThumbnail("http://what-if.xkcd.com/" + item.child(0).child(0).attr("src"));
                whatIf.setTitle(item.child(1).text());
                whatIf.setDate(item.child(2).text());
                whatIfs.add(whatIf);
            }

            Intent resultIntent = new Intent(WHAT_IF_RESULT);
            resultIntent.putParcelableArrayListExtra(KEY_RESULT, whatIfs);
            LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
