package com.tanmay.blip.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.tanmay.blip.R;
import com.tanmay.blip.database.DatabaseManager;
import com.tanmay.blip.database.SharedPrefs;
import com.tanmay.blip.models.Comic;
import com.tanmay.blip.utils.BlipUtils;
import com.tanmay.blip.utils.SpeechSynthesizer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ExternalLinkActivity extends BaseActivity implements View.OnClickListener {

    private TextView alt;
    private ImageView img, favourite;
    private DatabaseManager databaseManager;
    private Comic comic;

    @Override
    protected int getToolbarColor() {
        if (SharedPrefs.getInstance().isNightModeEnabled()) {
            return getResources().getColor(R.color.primary_night);
        } else {
            return getResources().getColor(R.color.primary);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_external_link;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String url = intent.getDataString();
        String[] urlPrefixes = url.split(".com/");

        if (urlPrefixes.length < 2) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        Log.i("TAG", urlPrefixes[1]);
        String prefix = urlPrefixes[1];
        prefix = prefix.replace("/", "");
        if (!BlipUtils.isNumeric(prefix)) {
            finish();
            return;
        }

        int num = Integer.parseInt(prefix);
        databaseManager = new DatabaseManager(this);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy (EEEE)", Locale.getDefault());
        comic = databaseManager.getComic(num);

        TextView title = (TextView) findViewById(R.id.title);
        TextView date = (TextView) findViewById(R.id.date);
        alt = (TextView) findViewById(R.id.alt);
        img = (ImageView) findViewById(R.id.img);
        favourite = (ImageView) findViewById(R.id.favourite);
        ImageView browser = (ImageView) findViewById(R.id.open_in_browser);
        ImageView transcript = (ImageView) findViewById(R.id.transcript);
        View imgContainer = findViewById(R.id.img_container);
        ImageView share = (ImageView) findViewById(R.id.share);
        ImageView explain = (ImageView) findViewById(R.id.help);
        CardView backgroundCard = (CardView) findViewById(R.id.comic);

        browser.setOnClickListener(this);
        transcript.setOnClickListener(this);
        imgContainer.setOnClickListener(this);
        favourite.setOnClickListener(this);
        share.setOnClickListener(this);
        explain.setOnClickListener(this);
        alt.setOnClickListener(this);

        if (SharedPrefs.getInstance().isNightModeEnabled()) {
            backgroundCard.setCardBackgroundColor(getResources().getColor(R.color.primary_night));
            title.setTextColor(getResources().getColor(android.R.color.white));
            date.setTextColor(getResources().getColor(android.R.color.white));
            alt.setTextColor(getResources().getColor(android.R.color.white));
            transcript.setColorFilter(getResources().getColor(android.R.color.white));
            share.setColorFilter(getResources().getColor(android.R.color.white));
            explain.setColorFilter(getResources().getColor(android.R.color.white));
            browser.setColorFilter(getResources().getColor(android.R.color.white));
        }

        String comicTitle;
        if (SharedPrefs.getInstance().isTitleHidden()) {
            comicTitle = String.valueOf(comic.getNum());
        } else {
            comicTitle = comic.getNum() + ". " + comic.getTitle();
        }
        title.setText(comicTitle);
        getSupportActionBar().setTitle(comicTitle);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(comic.getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(comic.getMonth()) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(comic.getDay()));
        date.setText(simpleDateFormat.format(calendar.getTime()));

        if (SharedPrefs.getInstance().isAltSpoilerized()) {
            String altText = getResources().getString(R.string.title_pager_alt_spoiler);
            alt.setClickable(true);
            alt.setText(altText);
        } else {
            alt.setClickable(false);
            alt.setText(comic.getAlt());
        }

        Picasso.with(this)
                .load(comic.getImg())
                .error(R.drawable.error_network)
                .into(img);
        if (comic.isFavourite()) {
            favourite.setColorFilter(getResources().getColor(R.color.accent));
        } else {
            if (SharedPrefs.getInstance().isNightModeEnabled()) {
                favourite.setColorFilter(getResources().getColor(android.R.color.white));
            } else {
                favourite.setColorFilter(getResources().getColor(R.color.icons_dark));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_in_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://xkcd.com/" + comic.getNum()));
                startActivity(intent);
                break;
            case R.id.transcript:
                String content = comic.getTranscript();
                if (content.equals("")) {
                    content = getResources().getString(R.string.message_no_transcript);
                }
                final String speakingContent = content;
                new MaterialDialog.Builder(this)
                        .title(R.string.title_dialog_transcript)
                        .content(content)
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
                break;
            case R.id.img_container:
                ImageActivity.launch(this, img, comic.getNum());
                break;
            case R.id.favourite:
                boolean fav = comic.isFavourite();
                comic.setFavourite(!fav);
                databaseManager.setFavourite(comic.getNum(), !fav);
                if (fav) {
                    //remove from fav
                    favourite.setColorFilter(getResources().getColor(R.color.icons_dark));
                } else {
                    //make fav
                    favourite.setColorFilter(getResources().getColor(R.color.accent));
                }
                break;
            case R.id.help:
                Intent explainIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + comic.getNum()));
                startActivity(explainIntent);
                break;
            case R.id.share:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                if (BlipUtils.isLollopopUp()) {
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                } else {
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                }
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, comic.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, comic.getImg());
                startActivity(Intent.createChooser(shareIntent, this.getResources().getString(R.string.tip_share_image_url)));
                break;
            case R.id.alt:
                alt.setText(comic.getAlt());
                alt.setClickable(false);
                break;
        }
    }
}
