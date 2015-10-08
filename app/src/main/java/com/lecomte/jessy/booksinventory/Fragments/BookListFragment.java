package com.lecomte.jessy.booksinventory.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.lecomte.jessy.booksinventory.BuildConfig;
import com.lecomte.jessy.booksinventory.Data.AlexandriaContract;
import com.lecomte.jessy.booksinventory.Other.BookListAdapter;
import com.lecomte.jessy.booksinventory.Services.BookService;

/**
 * A list fragment representing a list of Books. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link BookDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class BookListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = BookListFragment.class.getSimpleName();
    private static final String SELECTED_ITEM_INDEX = BuildConfig.APPLICATION_ID + ".SELECTED_ITEM_INDEX";
    private final int LOADER_ID = 10;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = null;

    private BookListAdapter mBookListAdapter;
    private int mSelectedItemIndex = ListView.INVALID_POSITION;
    private int mBooksInListCount = 0;

    public void onBookAdded(String isbn) {
        mSelectedItemIndex = 0;
        getListView().setItemChecked(mSelectedItemIndex, true);
    }

    void notifyOfBookSelection() {
        // Notify the details fragment to load the book data
        // Has to be done this way or else I get an illegal state exception
        // Apparently, it's a known bug in Android
        // http://stackoverflow.com/questions/22788684/can-not-perform-this-action-inside-of-onloadfinished#24962974
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = (Cursor) getListView().getItemAtPosition(mSelectedItemIndex);

                if (cursor == null || !cursor.moveToFirst()) {
                    Log.d(TAG, "bookList.onItemClick() - Cursor null or empty!");
                    return;
                }
                mCallbacks.onItemSelected(cursor.getString(cursor
                        .getColumnIndex(AlexandriaContract.BookEntry._ID)));
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d(TAG, "onCreateLoader() - Creating new CursorLoader with URI: " + AlexandriaContract.BookEntry.CONTENT_URI);

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        /*boolean bBookDeleted = false;
        boolean bBookAdded = false;*/

        // If the app was just started, the adapter is not set
        if (mBookListAdapter == null) {
            mBookListAdapter = new BookListAdapter(getActivity(), data, 0);
            setListAdapter(mBookListAdapter);
            mBooksInListCount = mBookListAdapter.getCount();
        }

        // Just load the new data into the books list
        else {
            mBookListAdapter.swapCursor(data);
        }

        // Select/highlight the first book in the list
        /*mSelectedItemIndex = 0;
        getListView().setItemChecked(mSelectedItemIndex, true);*/

        // Notify the main activity so it can load the details view for the newly added book


        // Highglight/select the item that was selected before we reloaded the books list
        /*getListView().setItemChecked(mSelectedItemIndex, true);
        getListView().smoothScrollToPosition(mSelectedItemIndex);*/
        /*mBooksInListCount = mBookListAdapter.getCount();
        Log.d(TAG, "onLoadFinished() - Books in list: " + mBooksInListCount);
        Log.d(TAG, "onLoadFinished() - Selected item index: " + mSelectedItemIndex);*/

        // A book was added to the database since the last time the list was loaded
        /*if (mBookListAdapter.getCount() > mBooksInListCount) {
            Log.d(TAG, "onLoadFinished() - A book was ADDED to the DB");
            mSelectedItemIndex = 0;
            notifyOfBookSelection();
        }*/

        /*
        // A book was deleted from the database since the last time the list was loaded
        else if (mBookListAdapter.getCount() < mBooksInListCount) {
            Log.d(TAG, "onLoadFinished() - A book was DELETED from the DB");
            if (mBookListAdapter.getCount() == 0) {
                mSelectedItemIndex = ListView.INVALID_POSITION;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallbacks.onItemSelected(null);
                    }
                });
            }

            else {
                mSelectedItemIndex = 0;
                notifyOfBookSelection();
            }
        }

        // First-time loading or reloading after a configuration change
        else {
            // The app was just started
            if (mSelectedItemIndex == ListView.INVALID_POSITION) {
                // The database is not empty (it has some books records)
                if (data.getCount() > 0) {
                    // Select the first book in the list
                    mSelectedItemIndex = 0;
                    notifyOfBookSelection();

                } else {
                    return;
                }
            }
        }
        */

        // Highglight/select the item that was selected before we reloaded the books list
        /*getListView().setItemChecked(mSelectedItemIndex, true);
        getListView().smoothScrollToPosition(mSelectedItemIndex);
        mBooksInListCount = mBookListAdapter.getCount();
        Log.d(TAG, "onLoadFinished() - Books in list: " + mBooksInListCount);
        Log.d(TAG, "onLoadFinished() - Selected item index: " + mSelectedItemIndex);*/

        // Print list content
        /*String bookEntry;
        String isbn;
        int isbnIndex;
        Cursor cursor;
        int titleIndex;
        String title;
        ListAdapter adapter = getListAdapter();

        for (int i=0; i<adapter.getCount(); i++) {
            cursor = (Cursor)adapter.getItem(i);
            isbnIndex = cursor.getColumnIndex(AlexandriaContract.BookEntry._ID);
            isbn = cursor.getString(isbnIndex);
            titleIndex = cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE);
            title = cursor.getString(titleIndex);
            bookEntry = String.format("[%d]: %s   %s", i, isbn, title);
            Log.d(TAG, bookEntry);
        }*/
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookListAdapter.swapCursor(null);
    }

    public void deleteSelectedBook() {
        Cursor cursor = mBookListAdapter.getCursor();

        if (cursor == null || !cursor.moveToPosition(mSelectedItemIndex)) {
            Log.d(TAG, "deleteSelectedBook() - Invalid cursor");
            return;
        }

        String isbn = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));

        if (isbn != null && !isbn.isEmpty()) {
            // Delete selected book from database as identified by its ISBN
            Intent bookIntent = new Intent(getActivity(), BookService.class);
            bookIntent.putExtra(BookService.ISBN, isbn);
            bookIntent.setAction(BookService.DELETE_BOOK);
            getActivity().startService(bookIntent);

            // Since currently selected book was deleted, we must highlight another book in list
            // We set it to the first book in the list (at index 0)
            setSelectedBook(0);
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookListFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mSelectedItemIndex = savedInstanceState.getInt(SELECTED_ITEM_INDEX);
            Log.d(TAG, "onViewCreated() - mSelectedItemIndex: " + mSelectedItemIndex);
        }

        // Load list view with books data from the database
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = null;
    }

    public void setSelectedBook(String isbn) {
        String rowIsbn;
        int indexToSelect = 0;
        boolean bIndexFound = false;
        Cursor cursor = mBookListAdapter.getCursor();

        if (cursor == null || !cursor.moveToFirst()) {
            Log.d(TAG, "setSelectedBook() - Cursor is invalid!");    
        }

        for (int i=0; i<cursor.getCount() && !bIndexFound; i++) {
            rowIsbn = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));

            if (rowIsbn.equals(isbn)) {
                indexToSelect = i;
                bIndexFound = true;
            }
            cursor.moveToNext();
        }

        mSelectedItemIndex = indexToSelect;
        getListView().setItemChecked(mSelectedItemIndex, true);
        getListView().smoothScrollToPosition(mSelectedItemIndex);
    }

    private void setSelectedBook(int position) {
        Cursor cursor = (Cursor)getListView().getItemAtPosition(position);

        if (cursor == null || !cursor.moveToPosition(position)) {
            Log.d(TAG, "setSelectedBook() - Cursor null or empty!");
            mSelectedItemIndex = ListView.INVALID_POSITION;
            return;
        }

        mCallbacks.onItemSelected(cursor.getString(cursor
                .getColumnIndex(AlexandriaContract.BookEntry._ID)));

        mSelectedItemIndex = position;
        Log.d(TAG, "setSelectedBook() - mSelectedItemIndex: " + mSelectedItemIndex);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        setSelectedBook(position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_ITEM_INDEX, mSelectedItemIndex);
        Log.d(TAG, "onSaveInstanceState() - mSelectedItemIndex: " + mSelectedItemIndex);
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }
}
