package com.lecomte.jessy.booksinventory.Services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
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

    // Commands this service can accomplish for the client
    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";

    // Service to client communication

    // Intent action when this service sends data to the client
    public static final String MESSAGE = BuildConfig.APPLICATION_ID + ".MESSAGE";

    // Extras returned to client (keys)
    public static final String EXTRA_COMMAND = BuildConfig.APPLICATION_ID + ".EXTRA_COMMAND";
    public static final String EXTRA_RESULT  = BuildConfig.APPLICATION_ID + ".EXTRA_RESULT";
    public static final String EXTRA_ISBN    = BuildConfig.APPLICATION_ID + ".EXTRA_ISBN";

    // Fetch book command results (possible values for EXTRA_RESULT key when EXTRA_COMMAND is FETCH_BOOK)
    public static final int FETCH_RESULT_ADDED_TO_DB   = 1;
    public static final int FETCH_RESULT_ALREADY_IN_DB = 2;
    public static final int FETCH_RESULT_NOT_FOUND     = 3;

    // Delete book command results (possible values for EXTRA_RESULT key when EXTRA_COMMAND is DELETE_BOOK)
    public static final int DELETE_RESULT_DELETED      = 10;
    public static final int DELETE_RESULT_NOT_DELETED  = 11;

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");

        if (intent != null) {
            final String action = intent.getAction();

            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EXTRA_ISBN);
                Log.d(TAG, "onHandleIntent() - Action: FETCH_BOOK [ISBN: " + ean + "]");
                fetchBook(ean);
            }

            else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EXTRA_ISBN);
                Log.d(TAG, "onHandleIntent() - Action: DELETE_BOOK [ISBN: " + ean + "]");
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String isbn) {
        Log.d(TAG, "deleteSelectedBook() - ISBN: " + isbn);
        int bookRowsDeleted = 0;
        int categoryRowsDeleted = 0;
        int authorRowsDeleted = 0;
        if (isbn != null) {
            // Delete from "categories" table
            categoryRowsDeleted = getContentResolver().delete(
                    AlexandriaContract.CategoryEntry.buildCategoryUri(Long.parseLong(isbn)), null, null);

            // Delete from "authors" table
            authorRowsDeleted = getContentResolver().delete(
                    AlexandriaContract.AuthorEntry.buildAuthorUri(Long.parseLong(isbn)), null, null);

            // Delete from "books" table
            bookRowsDeleted = getContentResolver().delete(
                    AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(isbn)), null, null);
        }

        // Send status to client about the delete command
        if (authorRowsDeleted > 0 && categoryRowsDeleted > 0 && bookRowsDeleted > 0) {
            sendCommandResultToClient(DELETE_BOOK, DELETE_RESULT_DELETED, isbn);
        } else {
            sendCommandResultToClient(DELETE_BOOK, DELETE_RESULT_NOT_DELETED, isbn);
        }
    }

    private void sendCommandResultToClient(String command, int result, String isbn) {
        Log.d(TAG, "sendCommandResultToClient() - ISBN: " + isbn);
        Intent intent = new Intent(MESSAGE);
        intent.putExtra(EXTRA_COMMAND, command);
        intent.putExtra(EXTRA_RESULT, result);

        if (isbn != null) {
            intent.putExtra(EXTRA_ISBN, isbn);
        }

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String isbn) {
        Log.d(TAG, "fetchBook() - ISBN: " + isbn );

        if(isbn.length()!=13){
            Log.d(TAG, "fetchBook() - ISBN != 13, exiting method early...");
            return;
        }

        Cursor bookCursor = getContentResolver().query(
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(isbn)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (bookCursor.getCount() > 0) {
            Log.d(TAG, "fetchBook() - No need to download, this book is already in the database");

            if (bookCursor.moveToFirst()) {
                sendCommandResultToClient(FETCH_BOOK, FETCH_RESULT_ALREADY_IN_DB, isbn);
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

            final String ISBN_PARAM = "isbn:" + isbn;

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
                sendCommandResultToClient(FETCH_BOOK, FETCH_RESULT_NOT_FOUND, isbn);
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

            writeBackBook(isbn, title, subtitle, desc, imgUrl);

            if(bookInfo.has(AUTHORS)) {
                writeBackAuthors(isbn, bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                writeBackCategories(isbn,bookInfo.getJSONArray(CATEGORIES) );
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error ", e);
        }

        // Book downloaded and saved to DB
        sendCommandResultToClient(FETCH_BOOK, FETCH_RESULT_ADDED_TO_DB, isbn);
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
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
