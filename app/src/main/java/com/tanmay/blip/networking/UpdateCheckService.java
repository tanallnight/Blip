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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

import com.google.gson.Gson;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tanmay.blip.BlipApplication;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.models.Comic;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Tanmay Parikh on 7/21/2015.
 */
public class UpdateCheckService extends IntentService {

    public static final String NEW_COMIC = "com.tanmay.blip.NEW_COMIC";
    public static final String EXTRA_NUM = "number";

    public static final int RESTART_CODE = 134324;

    public UpdateCheckService() {
        super("UpdateCheckService");
    }

    public UpdateCheckService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseManager databaseManager = new DatabaseManager(this);
        Gson gson = new Gson();
        Request request = new Request.Builder().url("http://xkcd.com/info.0.json").build();
        try {
            Response response = BlipApplication.getInstance().client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException();
            String responseBody = response.body().string();
            if (responseBody.substring(0, 1).equals("{")) {
                Comic comic = gson.fromJson(responseBody, Comic.class);
                if (!databaseManager.comicExists(comic)) {
                    databaseManager.addComic(comic);
                    Intent notifIntent = new Intent(NEW_COMIC);
                    notifIntent.putExtra(EXTRA_NUM, comic.getNum());
                    sendBroadcast(notifIntent);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, RESTART_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 4);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        super.onDestroy();
    }
}
