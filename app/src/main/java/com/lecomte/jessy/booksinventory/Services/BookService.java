package com.lecomte.jessy.booksinventory.Services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.lecomte.jessy.booksinventory.BuildConfig;
import com.lecomte.jessy.booksinventory.Data.AlexandriaContract;
import com.lecomte.jessy.booksinventory.Data.BookData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jessy on 2015-09-24.
 */
public class BookService extends IntentService {

    private final String TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";

    public static final String EXTRA_RESULT_OBJECT = BuildConfig.APPLICATION_ID + ".EXTRA_RESULT_OBJECT";
    public static final String EXTRA_RESULT_DATA = BuildConfig.APPLICATION_ID + ".EXTRA_RESULT_DATA";

    public static final String EXTRA_RESULT_CODE = BuildConfig.APPLICATION_ID + ".EXTRA_RESULT_CODE";
    public static final int EXTRA_RESULT_BOOK_NOT_FOUND  = 1;
    public static final int EXTRA_RESULT_BOOK_IN_DB      = 2;
    public static final int EXTRA_RESULT_BOOK_DOWNLOADED = 3;

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    private ResultReceiver mCommandResult;
    private BookData mBookData = new BookData();

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");

        if (intent != null) {
            final String action = intent.getAction();
            mCommandResult = intent.getParcelableExtra(EXTRA_RESULT_OBJECT);

            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                Log.d(TAG, "onHandleIntent() - Action: FETCH_BOOK [ISBN: " + ean + "]");
                fetchBook(ean);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                Log.d(TAG, "onHandleIntent() - Action: DELETE_BOOK [ISBN: " + ean + "]");
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        Log.d(TAG, "deleteBook() - ISBN: " + ean);
        if(ean != null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }
    
    private BookData cursorToBookData(Cursor cursor) {
        BookData bookInfo = new BookData();
        bookInfo.setDescription(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.DESC)));
        bookInfo.setTitle(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE)));
        bookInfo.setSubTitle(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE)));

        bookInfo.setImageUrl(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL)));
        bookInfo.setAuthors(cursor.getString(cursor.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR)));
        bookInfo.setCategories(cursor.getString(cursor.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY)));
        return bookInfo;
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean) {
        Log.d(TAG, "fetchBook() - ISBN: " + ean );

        if(ean.length()!=13){
            Log.d(TAG, "fetchBook() - ISBN != 13, exiting method early...");
            return;
        }

        Cursor bookCursor = getContentResolver().query(
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (bookCursor.getCount() > 0) {
            Log.d(TAG, "fetchBook() - No need to download, this book is already in the database");

            if (bookCursor.moveToFirst()) {
                BookData bookInfo = cursorToBookData(bookCursor);
                Bundle data = new Bundle();
                data.putInt(EXTRA_RESULT_CODE, EXTRA_RESULT_BOOK_IN_DB);
                data.putParcelable(BookService.EXTRA_RESULT_DATA, bookInfo);
                mCommandResult.send(0, data);
            }
            
            bookCursor.close();
            return;
        }

        bookCursor.close();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        final String ITEMS = "items";

        final String VOLUME_INFO = "volumeInfo";

        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if (bookJson.has(ITEMS)) {
                bookArray = bookJson.getJSONArray(ITEMS);
            } else {
                // TODO: Jessy - Fix this
                //EXTRA_RESULT_BOOK_NOT_FOUND
                /*Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.not_found));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);*/
                return;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);

            String subtitle = "";
            if(bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }

            String desc="";
            if(bookInfo.has(DESC)){
                desc = bookInfo.getString(DESC);
            }

            String imgUrl = "";
            if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            writeBackBook(ean, title, subtitle, desc, imgUrl);

            if(bookInfo.has(AUTHORS)) {
                writeBackAuthors(ean, bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                writeBackCategories(ean,bookInfo.getJSONArray(CATEGORIES) );
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error ", e);
        }

        // Book downloaded and saved to DB
        //BookData bookInfo = cursorToBookData(bookCursor);
        Bundle addBookResult = new Bundle();
        addBookResult.putInt(EXTRA_RESULT_CODE, EXTRA_RESULT_BOOK_DOWNLOADED);
        addBookResult.putParcelable(BookService.EXTRA_RESULT_DATA, mBookData);
        mCommandResult.send(0, addBookResult);
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        mBookData.setTitle(title);
        mBookData.setSubTitle(subtitle);
        mBookData.setDescription(desc);
        mBookData.setImageUrl(imgUrl);

        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI,values);
    }

    private void writeBackAuthors(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void writeBackCategories(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }
}
