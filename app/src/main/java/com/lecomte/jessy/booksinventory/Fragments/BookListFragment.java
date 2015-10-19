package com.lecomte.jessy.booksinventory.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lecomte.jessy.booksinventory.BuildConfig;
import com.lecomte.jessy.booksinventory.Data.AlexandriaContract;
import com.lecomte.jessy.booksinventory.Other.BookListAdapter;
import com.lecomte.jessy.booksinventory.Other.Utility;
import com.lecomte.jessy.booksinventory.R;
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
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = BookListFragment.class.getSimpleName();
    private static final String SELECTED_ITEM_INDEX = BuildConfig.APPLICATION_ID + ".SELECTED_ITEM_INDEX";
    private final int LOADER_ID = 10;

    private Callbacks mCallbacks = null;
    private BookListAdapter mBookListAdapter;
    TextView mEmptyListTextView;
    ImageView mEmptyListImageView;
    private String mFetchResultDesc = "";
    private boolean mDeleteBookInProgress = false;

    // Initially, the list is empty so there is no item selected
    private int mSelectedItemIndex = ListView.INVALID_POSITION;

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
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        // http://developer.android.com/guide/topics/ui/layout/listview.html
        // Create a progress bar to display while the list loads
        /*ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);*/

        // Initially, we create the adapter with a null cursor (no data to display)
        mBookListAdapter = new BookListAdapter(getActivity(), null, 0);
        setListAdapter(mBookListAdapter);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged() - key: " + key);

        if (key.equals(getString(R.string.pref_delete_result))) {
            //Toast.makeText(getActivity(), "Delete result receivedd!", Toast.LENGTH_LONG).show();
        }

        else if (key.equals(getString(R.string.pref_fetch_result))) {

            // Get the error message (if any) to display to the user in the empty view
            @BookService.FetchResult int fetchResult = Utility.getFetchResult(getActivity());
            mFetchResultDesc = Utility.getFetchResultDesc(getActivity(), fetchResult);
        }
    }

    private String getIsbnAtIndex(Cursor cursor, int index) {
        if (cursor == null || !cursor.moveToPosition(index)) {
            Log.d(TAG, "getIsbnAtIndex() - Invalid cursor");
            return null;
        }
        return cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID));
    }

    private String getTitleAtIndex(Cursor cursor, int index) {
        if (cursor == null || !cursor.moveToPosition(index)) {
            Log.d(TAG, "getTitleAtIndex() - Invalid cursor");
            return null;
        }
        return cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
    }

    // Find a way to refresh the empty view
    public void reloadList() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");

        // Update list view with book data
        mBookListAdapter.swapCursor(data);

        // If the list is empty, we need to inform the user of the reason why
        if (getListView().getCount() == 0 && mEmptyListTextView != null &&
                mEmptyListImageView != null) {

            // No fetch operation occurred (we don't have a result of such an operation)
            if (mFetchResultDesc.isEmpty()) {
                // Check if the network is available
                if (!Utility.isInternetAvailable(getActivity())) {
                    mEmptyListTextView.setText("Internet disabled!");
                    mEmptyListImageView.setBackgroundResource(R.drawable.no_internet);
                }
                // Everything is okay, no books displayed because there's no books in the database
                else {
                    mEmptyListTextView.setText("No books in DB!");
                    mEmptyListImageView.setBackgroundResource(R.drawable.no_books);
                }
            }
            // Show the result of the last fetch operation
            else {
                mEmptyListTextView.setText(mFetchResultDesc);
                // For now, we use a default image for all server errors
                mEmptyListImageView.setBackgroundResource(R.drawable.server_issue);
            }
        }

        mCallbacks.onBookListLoadFinished(mBookListAdapter.getCount());

        if (mDeleteBookInProgress) {
            mDeleteBookInProgress = false;
            // Only call this if we are in a 2-pane layout
            setSelectedBookRunnable();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookListAdapter.swapCursor(null);
    }

    public boolean deleteSelectedBook() {
        if (mSelectedItemIndex != ListView.INVALID_POSITION) {
            String isbn = getIsbnAtIndex(mBookListAdapter.getCursor(), mSelectedItemIndex);

            if (isbn != null) {
                // Delete selected book from database as identified by its ISBN
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EXTRA_ISBN, isbn);
                bookIntent.setAction(BookService.DELETE_BOOK);
                mDeleteBookInProgress = true;
                getActivity().startService(bookIntent);
                return true;
            }
        }

        return false;
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
        void onBookForcedSelection(String id);

        // Inform main activity when book list is reloaded and sends book count
        void onBookListLoadFinished(int bookCount);

        void onBookClicked(String isbn);
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

        // Set the view to display when our list of books is empty
        // We use a viewStub so we can have an image and text instead of just plain text

        ViewStub emptyView = new ViewStub(getActivity());

        emptyView.setOnInflateListener(new ViewStub.OnInflateListener() {
            @Override
            public void onInflate(ViewStub stub, View inflated) {
                mEmptyListTextView = (TextView) inflated.findViewById(R.id.empty_book_list_Text);
                mEmptyListImageView = (ImageView) inflated.findViewById(R.id.empty_book_list_Image);
            }
        });

        // Make sure you import android.widget.LinearLayout.LayoutParams;
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setVisibility(View.GONE);
        emptyView.setLayoutResource(R.layout.empty_book_list);
        // The empty view must always be a sibling (share same parent) of the list view
        ((ViewGroup) getListView().getParent()).addView(emptyView);

        getListView().setEmptyView(emptyView);

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

    private void setSelectedBookRunnable() {
        // Set the currently selected book in the list as the first one in the list
        mSelectedItemIndex = mBookListAdapter.getCount() > 0? 0 : ListView.INVALID_POSITION;
        if (mSelectedItemIndex != ListView.INVALID_POSITION) {
            getListView().setItemChecked(mSelectedItemIndex, true);
        }

        // Inform the main activity so it can update the details view with this book's data
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String isbn = mBookListAdapter.getCount() > 0 ?
                        getIsbnAtIndex(mBookListAdapter.getCursor(), mSelectedItemIndex) : null;
                mCallbacks.onBookForcedSelection(isbn);
            }
        });
    }

    public void notifyOnBookClicked(String isbn) {
        int booksCount = mBookListAdapter.getCount();
        boolean bIndexFound = false;
        Cursor cursor = mBookListAdapter.getCursor();
        String rowIsbn;
        int indexToSelect = ListView.INVALID_POSITION;

        for (int i=0; i<booksCount && !bIndexFound; i++) {
            rowIsbn = getIsbnAtIndex(cursor, i);

            if (rowIsbn.equals(isbn)) {
                indexToSelect = i;
                bIndexFound = true;
            }
        }
        mSelectedItemIndex = indexToSelect;

        if (mSelectedItemIndex != ListView.INVALID_POSITION) {
            getListView().setItemChecked(mSelectedItemIndex, true);
            getListView().smoothScrollToPosition(mSelectedItemIndex);
        }
    }

    private void notifyOnBookClicked(int position) {
        String isbn = getIsbnAtIndex(mBookListAdapter.getCursor(), position);

        if (isbn == null) {
            Log.d(TAG, "notifyOnBookClicked() - ISBN is null!");
            mSelectedItemIndex = ListView.INVALID_POSITION;
            return;
        }
        mCallbacks.onBookClicked(isbn);
        mSelectedItemIndex = position;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        notifyOnBookClicked(position);
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

    public void shareSelectedBook() {
        String bookTitle = getTitleAtIndex(mBookListAdapter.getCursor(), mSelectedItemIndex);

        if (bookTitle == null) {
            return;
        }

        Intent shareUrlIntent = new Intent(Intent.ACTION_SEND);
        shareUrlIntent.putExtra(Intent.EXTRA_TEXT, bookTitle);
        shareUrlIntent.setType("text/plain");
        startActivity(shareUrlIntent);
    }
}

