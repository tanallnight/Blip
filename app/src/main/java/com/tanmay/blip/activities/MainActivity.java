package com.tanmay.blip.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.tanmay.blip.R;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.database.SharedPrefs;
import com.tanmay.blip.networking.XKCDDownloader;

public class MainActivity extends BaseActivity {

    private static final int DOWNLOAD_REQUEST = 1045;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPrefs.getInstance().getFirstRun()) {
            startActivityForResult(new Intent(this, DownloadActivity.class), DOWNLOAD_REQUEST);
        } else {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(XKCDDownloader.DOWNLOAD_SUCCESS);
            intentFilter.addAction(XKCDDownloader.DOWNLOAD_FAIL);
            LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver, intentFilter);
            Intent intent = new Intent(this, XKCDDownloader.class);
            intent.setAction(XKCDDownloader.DOWNLOAD_TODAY);
            startService(intent);
        }

    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DOWNLOAD_REQUEST) {
            if (resultCode == RESULT_OK) {
                SharedPrefs.getInstance().setFirstRun(false);
            } else {
                finish();
            }
        }
    }
}
