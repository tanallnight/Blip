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

package com.tanmay.blip.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ShareCompat;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;
import com.tanmay.blip.R;
import com.tanmay.blip.activities.MainActivity;
import com.tanmay.blip.models.Comic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class BlipUtils {

    public static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=178200")
                    .build();
        }
    };

    public static String createSubhead(Comic comic, Calendar calendar, SimpleDateFormat dateFormat) {
        calendar.set(Calendar.YEAR, Integer.parseInt(comic.getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(comic.getMonth()) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(comic.getDay()));
        return dateFormat.format(calendar.getTime());
    }

    public static void shareImage(Bitmap bitmap, Activity activity, int num) {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Blip");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "Blip" + File.separator + num + ".png");
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent shareImageIntent = ShareCompat.IntentBuilder.from(activity).setType("image/png").setStream(Uri.fromFile(file)).getIntent();
        activity.startActivity(shareImageIntent);
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static boolean isLollopopUp() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isNumeric(String input) {
        try {
            int num = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static void restartApp(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.title_dialog_restart)
                .content(R.string.content_dialog_restart)
                .positiveText(R.string.positive_text_restart)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Intent intent = new Intent(context, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2351, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent);
                        System.exit(0);
                    }
                })
                .show();
    }
}
