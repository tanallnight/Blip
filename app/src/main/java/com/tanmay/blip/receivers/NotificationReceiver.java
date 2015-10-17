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

package com.tanmay.blip.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.tanmay.blip.R;
import com.tanmay.blip.activities.MainActivity;
import com.tanmay.blip.managers.DatabaseManager;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.networking.UpdateCheckService;

/**
 * Created by Tanmay Parikh on 7/21/2015.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final int NOTIF_ID = 486156;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(UpdateCheckService.NEW_COMIC)) {
            DatabaseManager databaseManager = new DatabaseManager(context);
            Comic comic = databaseManager.getComic(intent.getExtras().getInt(UpdateCheckService.EXTRA_NUM));

            Intent mainActIntent = new Intent(context, MainActivity.class);
            PendingIntent clickIntent = PendingIntent.getActivity(context, 57836, mainActIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setContentTitle(context.getResources().getString(R.string.title_notification_new_comic))
                    .setContentText(comic.getTitle())
                    .setContentIntent(clickIntent)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIF_ID, builder.build());
        }
    }
}
