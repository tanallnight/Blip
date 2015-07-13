package com.tanmay.blip.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tanmay.blip.models.Comic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "XKCD";
    private static final int DB_VERSION = 1;

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_REAL = " REAL";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String COMMA_SEP = ",";

    private static final String TABLE_XKCD = "XKCD_Table";
    private static final String MONTH = "month";
    private static final String NUM = "num";
    private static final String LINK = "link";
    private static final String YEAR = "year";
    private static final String NEWS = "news";
    private static final String SAFE_TITLE = "safe_title";
    private static final String TRANSCRIPT = "transcript";
    private static final String ALT = "alt";
    private static final String IMG = "img";
    private static final String TITLE = "title";
    private static final String DAY = "day";
    private static final String FAV = "fav";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_XKCD + "(" +
                    MONTH + TYPE_TEXT + COMMA_SEP +
                    NUM + TYPE_INTEGER + PRIMARY_KEY + COMMA_SEP +
                    LINK + TYPE_TEXT + COMMA_SEP +
                    YEAR + TYPE_TEXT + COMMA_SEP +
                    NEWS + TYPE_TEXT + COMMA_SEP +
                    SAFE_TITLE + TYPE_TEXT + COMMA_SEP +
                    TRANSCRIPT + TYPE_TEXT + COMMA_SEP +
                    ALT + TYPE_TEXT + COMMA_SEP +
                    IMG + TYPE_TEXT + COMMA_SEP +
                    TITLE + TYPE_TEXT + COMMA_SEP +
                    DAY + TYPE_TEXT + COMMA_SEP +
                    FAV + TYPE_INTEGER + ")";

    private static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_XKCD;

    public DatabaseManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TABLE);
        onCreate(db);
    }

    public void addComic(Comic comic) {
        if (comicExists(comic)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MONTH, comic.getMonth());
        values.put(NUM, comic.getNum());
        values.put(LINK, comic.getLink());
        values.put(YEAR, comic.getYear());
        values.put(NEWS, comic.getNews());
        values.put(SAFE_TITLE, comic.getSafe_title());
        values.put(TRANSCRIPT, comic.getTranscript());
        values.put(ALT, comic.getAlt());
        values.put(IMG, comic.getImg());
        values.put(TITLE, comic.getTitle());
        values.put(DAY, comic.getDay());
        values.put(FAV, comic.isFavourite() ? 1 : 0);

        getWritableDatabase().insert(TABLE_XKCD, null, values);
    }

    public Comic getComic(int num) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_XKCD + " WHERE " + NUM + " = ?",
                new String[]{String.valueOf(num)});
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            Comic comic = new Comic();
            comic.setMonth(cursor.getString(0));
            comic.setNum(cursor.getInt(1));
            comic.setLink(cursor.getString(2));
            comic.setYear(cursor.getString(3));
            comic.setSafe_title(cursor.getString(4));
            comic.setTranscript(cursor.getString(5));
            comic.setAlt(cursor.getString(6));
            comic.setImg(cursor.getString(7));
            comic.setTitle(cursor.getString(8));
            comic.setDay(cursor.getString(9));
            comic.setFavourite(cursor.getInt(10) == 1);
            cursor.close();
            return comic;
        }
        return null;
    }

    public List<Comic> getAllComics(int start, int stop) {
        List<Comic> comics = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_XKCD + " WHERE " + NUM + " BETWEEN = ? AND = ?",
                new String[]{String.valueOf(start), String.valueOf(stop)});
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            comics = new ArrayList<>();
            do {
                Comic comic = new Comic();
                comic.setMonth(cursor.getString(0));
                comic.setNum(cursor.getInt(1));
                comic.setLink(cursor.getString(2));
                comic.setYear(cursor.getString(3));
                comic.setSafe_title(cursor.getString(4));
                comic.setTranscript(cursor.getString(5));
                comic.setAlt(cursor.getString(6));
                comic.setImg(cursor.getString(7));
                comic.setTitle(cursor.getString(8));
                comic.setDay(cursor.getString(9));
                comic.setFavourite(cursor.getInt(10) == 1);
                comics.add(comic);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return comics;
    }

    public List<Comic> search(String keyWord) {
        List<Comic> comics = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_XKCD + " WHERE " + TITLE + " LIKE ?",
                new String[]{keyWord});
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            comics = new ArrayList<>();
            do {
                Comic comic = new Comic();
                comic.setMonth(cursor.getString(0));
                comic.setNum(cursor.getInt(1));
                comic.setLink(cursor.getString(2));
                comic.setYear(cursor.getString(3));
                comic.setSafe_title(cursor.getString(4));
                comic.setTranscript(cursor.getString(5));
                comic.setAlt(cursor.getString(6));
                comic.setImg(cursor.getString(7));
                comic.setTitle(cursor.getString(8));
                comic.setDay(cursor.getString(9));
                comic.setFavourite(cursor.getInt(10) == 1);
                comics.add(comic);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return comics;
    }

    public List<Comic> getFavourites() {
        List<Comic> comics = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_XKCD + " WHERE " + FAV + " = 1", null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            comics = new ArrayList<>();
            do {
                Comic comic = new Comic();
                comic.setMonth(cursor.getString(0));
                comic.setNum(cursor.getInt(1));
                comic.setLink(cursor.getString(2));
                comic.setYear(cursor.getString(3));
                comic.setSafe_title(cursor.getString(4));
                comic.setTranscript(cursor.getString(5));
                comic.setAlt(cursor.getString(6));
                comic.setImg(cursor.getString(7));
                comic.setTitle(cursor.getString(8));
                comic.setDay(cursor.getString(9));
                comic.setFavourite(cursor.getInt(10) == 1);
                comics.add(comic);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return comics;
    }

    public boolean comicExists(Comic comic) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_XKCD
                + " WHERE " + NUM + " = ?", new String[]{String.valueOf(comic.getNum())});
        boolean exists = false;
        if (cursor != null && cursor.getCount() != 0) {
            exists = true;
            cursor.close();
        }
        return exists;
    }
}
