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

package com.tanmay.blip.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tanmay.blip.R;
import com.tanmay.blip.managers.ComicManager;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.utils.BlipUtils;
import com.tanmay.blip.utils.SpeechSynthesizer;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoViewAttacher;

@SuppressWarnings("ALL")
public class ImageActivity extends AppCompatActivity implements ComicManager.ComicLoadListener, PhotoViewAttacher.OnPhotoTapListener {

    private static final String EXTRA_NUMBER = "num";
    private static final int PERMISSION_REQUEST = 240;

    @Bind(R.id.img)
    ImageView mImage;
    @Bind(R.id.topBar)
    FrameLayout mTopBar;
    @Bind(R.id.bottom_bar)
    LinearLayout mBottomBar;
    @Bind(R.id.close)
    ImageView mClose;
    @Bind(R.id.title)
    TextView mTitle;
    @Bind(R.id.number)
    TextView mNumber;
    @Bind(R.id.favourite)
    ImageView mFavourite;
    @Bind(R.id.alt)
    ImageView mAltText;
    @Bind(R.id.transcript)
    ImageView mTranscript;
    @Bind(R.id.explain)
    ImageView mExplain;
    @Bind(R.id.open_in_browser)
    ImageView mOpenInBrowser;
    @Bind(R.id.share)
    ImageView mShare;
    @Bind(R.id.link)
    ImageView mLink;

    @BindString(R.string.title_dialog_transcript)
    String titleTranscript;
    @BindString(R.string.message_no_transcript)
    String noTranscript;

    private Comic mComic;
    private Bitmap mBitmap;
    private ComicManager mComicManager;
    private PhotoViewAttacher mAttacher;

    public static void launch(Activity context, int num) {
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtra(EXTRA_NUMBER, num);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in, R.anim.hold);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        if (BlipUtils.isLollopopUp()) {
            getWindow().setStatusBarColor(Color.parseColor("#222222"));
        }

        ButterKnife.bind(this);

        mComicManager = new ComicManager(this);
        int comicNum = getIntent().getExtras().getInt(EXTRA_NUMBER);
        mComicManager.loadComic(comicNum, this);

        mTopBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTopBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mTopBar.setTranslationY(-mTopBar.getHeight());
                mBottomBar.setTranslationY(mBottomBar.getHeight());
                mTopBar.animate().translationY(0).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                mBottomBar.animate().translationY(0).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }
        });

    }

    @Override
    public void onLoadSuccess(Comic comic) {
        mComic = comic;
        if (mComic.getLink().equals(""))
            mLink.setVisibility(View.GONE);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(ImageActivity.this).load(mComic.getImg()).into(mImage);
                mAttacher = new PhotoViewAttacher(mImage);
                mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
                mAttacher.setOnPhotoTapListener(ImageActivity.this);
                mTitle.setText(mComic.getTitle());
                mNumber.setText(String.valueOf(mComic.getNum()));
                updateFavouriteIcon();
            }
        });
    }

    @OnClick(R.id.close)
    public void onCloseClick() {
        onBackPressed();
    }

    @OnClick(R.id.favourite)
    public void onFavouriteClick() {
        mComicManager.updateFavouriteStatus(mComic.getNum(), !mComic.isFavourite());
        mComic.setFavourite(!mComic.isFavourite());
        updateFavouriteIcon();
    }

    @OnClick(R.id.alt)
    public void onAltTextClick() {
        new MaterialDialog.Builder(this).content(mComic.getAlt())
                .autoDismiss(false)
                .positiveText(R.string.dialog_option_copy)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData data = ClipData.newPlainText(getPackageName(), mComic.getAlt());
                        manager.setPrimaryClip(data);
                        Toast.makeText(ImageActivity.this, "Text Copied!", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    @OnClick(R.id.transcript)
    public void onTranscriptClick() {
        String content = mComic.getTranscript();
        if (content.equals("")) content = noTranscript;
        final String speakingContent = content;
        new MaterialDialog.Builder(this).title(titleTranscript).content(content)
                .negativeText(R.string.negative_text_dialog)
                .neutralText(R.string.neutral_text_dialog_speak)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);
                        SpeechSynthesizer.getInstance().convertToSpeechFlush(speakingContent);
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        SpeechSynthesizer.getInstance().stopSpeaking();
                    }
                })
                .show();
    }

    @OnClick(R.id.share)
    public void onShareClick() {
        new MaterialDialog.Builder(this)
                .title(R.string.action_share)
                .content(R.string.tip_share_image_help)
                .negativeText(R.string.tip_share_image_url)
                .positiveText(R.string.tip_share_image)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                mBitmap = bitmap;
                                if (ContextCompat.checkSelfPermission(ImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(ImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        ActivityCompat.requestPermissions(ImageActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                                    } else {
                                        ActivityCompat.requestPermissions(ImageActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                                    }
                                } else {
                                    BlipUtils.shareImage(mBitmap, ImageActivity.this, mComic.getNum());
                                }
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        };
                        Picasso.with(ImageActivity.this).load(mComic.getImg()).into(target);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        Intent shareIntent = ShareCompat.IntentBuilder
                                .from(ImageActivity.this)
                                .setType("text/plain")
                                .setSubject(mComic.getTitle())
                                .setText(mComic.getImg())
                                .getIntent();
                        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.tip_share_image_url)));
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BlipUtils.shareImage(mBitmap, this, mComic.getNum());
            } else {
                Toast.makeText(this, "Permissions required to share image were not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.explain)
    public void onExplainClick() {
        Intent explainIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + mComic.getNum()));
        startActivity(explainIntent);
    }

    @OnClick(R.id.open_in_browser)
    public void onBrowserClick() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://xkcd.com/" + mComic.getNum()));
        startActivity(browserIntent);
    }

    @OnClick(R.id.link)
    public void onLinkClick() {
        Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mComic.getLink()));
        startActivity(linkIntent);
    }

    private void updateFavouriteIcon() {
        if (mComic.isFavourite()) {
            mFavourite.setImageResource(R.drawable.ic_favorite);
        } else {
            mFavourite.setImageResource(R.drawable.ic_favorite_outline);
        }
    }

    @Override
    public void onLoadFail() {
        finish();
        Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        mTopBar.animate().translationY(-mTopBar.getHeight()).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        mBottomBar.animate().translationY(mBottomBar.getHeight()).setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ImageActivity.super.onBackPressed();
                        overridePendingTransition(R.anim.hold, R.anim.fade_out);
                    }
                })
                .setInterpolator(new AccelerateInterpolator()).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mComicManager.cleanUp();
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        onBackPressed();
    }
}
