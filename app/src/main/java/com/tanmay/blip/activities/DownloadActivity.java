package com.tanmay.blip.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tanmay.blip.R;
import com.tanmay.blip.networking.XKCDDownloader;

public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private TextView progress, title, pageTitle, caption;
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case XKCDDownloader.DOWNLOAD_PROGRESS:
                    if (progressBar.isIndeterminate()) {
                        progressBar.setIndeterminate(false);
                    }
                    progressBar.setProgress((int) intent.getExtras().getDouble(XKCDDownloader.PROGRESS));
                    progress.setText(String.valueOf(intent.getExtras().getDouble(XKCDDownloader.PROGRESS)) + "%");
                    title.setText(intent.getExtras().getString(XKCDDownloader.TITLE));
                    break;
                case XKCDDownloader.DOWNLOAD_FAIL:
                    progress.setText("Failed :(");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DownloadActivity.this.setResult(RESULT_CANCELED);
                            DownloadActivity.this.finish();
                        }
                    }, 2000);
                    break;
                case XKCDDownloader.DOWNLOAD_SUCCESS:
                    progress.setText("Success :D");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DownloadActivity.this.setResult(RESULT_OK);
                            DownloadActivity.this.finish();
                        }
                    }, 2000);
                    break;
            }
        }
    };
    private Button button;
    private View downloadUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        progress = (TextView) findViewById(R.id.progress);
        title = (TextView) findViewById(R.id.titles);
        pageTitle = (TextView) findViewById(R.id.title);
        caption = (TextView) findViewById(R.id.caption);
        button = (Button) findViewById(R.id.start_download);
        downloadUI = findViewById(R.id.download_ui);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        downloadUI.setVisibility(View.GONE);
        pageTitle.setText(getResources().getString(R.string.action_download_all_comics_explain));
        caption.setText(getResources().getString(R.string.action_download_all_comics_explain_caption));
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(XKCDDownloader.DOWNLOAD_PROGRESS);
        intentFilter.addAction(XKCDDownloader.DOWNLOAD_SUCCESS);
        intentFilter.addAction(XKCDDownloader.DOWNLOAD_FAIL);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        Intent intent = new Intent(this, XKCDDownloader.class);
        intent.setAction(XKCDDownloader.DOWNLOAD_ALL);
        startService(intent);

        pageTitle.setText(getResources().getString(R.string.action_download_all_comics));
        caption.setText(getResources().getString(R.string.action_download_all_comics_caption));
        button.setVisibility(View.GONE);
        downloadUI.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
    }
}
