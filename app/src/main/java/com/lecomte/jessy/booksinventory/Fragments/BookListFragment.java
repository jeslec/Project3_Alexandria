package com.lecomte.jessy.booksinventory.Fragments;

import android.app.Activity;
import android.content.Context;
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
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lecomte.jessy.booksinventory.BuildConfig;
import com.lecomte.jessy.booksinventory.Data.AlexandriaContract;
import com.lecomte.jessy.booksinventory.Other.BookListAdapter;
import com.lecomte.jessy.booksinventory.Other.Utility;
import com.lecomte.jessy.booksinventory.R;
import com.lecomte.jessy.booksinventory.Services.BookService;

import org.w3c.dom.Text;

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

    private Callbacks mCallbacks = null;
    private BookListAdapter mBookListAdapter;
    private View mEmptyView;

    // Initially, the list is empty so there is no item selected
    private int mSelectedItemIndex = ListView.INVALID_POSITION;

    private boolean mDeleteBookInProgress = false;
    private View mViewForEmptyList;

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

    void updateEmptyView(Cursor data) {
        // No data
        if (!data.moveToFirst()) {
            // Internet not available
            if (!Utility.isInternetAvailable(getActivity())) {
                Toast.makeText(getActivity(), R.string.internet_not_available,
                        Toast.LENGTH_LONG).show();
                mEmptyView = createViewForEmptyList(true, false);

                if (mEmptyView != null) {
                    getListView().setEmptyView(mEmptyView);
                }
            }
            // No books records in database
            else {
                Toast.makeText(getActivity(), R.string.book_list_empty,
                        Toast.LENGTH_LONG).show();
                mEmptyView = createViewForEmptyList(false, true);

                if (mEmptyView != null) {
                    getListView().setEmptyView(mEmptyView);
                }
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");

        updateEmptyView(data);
        // No data
        /*if (!data.moveToFirst()) {
            // Internet not available
            if (!Utility.isInternetAvailable(getActivity())) {
                Toast.makeText(getActivity(), R.string.internet_not_available,
                        Toast.LENGTH_LONG).show();
                mEmptyView = createViewForEmptyList(true, false);

                if (mEmptyView != null) {
                    getListView().setEmptyView(mEmptyView);
                }
            }
            // No books records in database
            else {
                Toast.makeText(getActivity(), R.string.book_list_empty,
                        Toast.LENGTH_LONG).show();
                mEmptyView = createViewForEmptyList(false, true);

                if (mEmptyView != null) {
                    getListView().setEmptyView(mEmptyView);
                }
            }
        }*/

        // If the app was just started, the adapter is not set
        if (mBookListAdapter == null) {
            mBookListAdapter = new BookListAdapter(getActivity(), data, 0);
            setListAdapter(mBookListAdapter);

            if (mEmptyView != null) {
                getListView().setEmptyView(mEmptyView);
            }
            // Only call this if we are in a 2-pane layout
            setSelectedBookRunnable();
        }

        // Just load the new data into the books list
        else {
            mBookListAdapter.swapCursor(data);
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

    // View displayed when the list view is empty
    // http://stackoverflow.com/questions/14082303/setemptyview-with-custom-listlayout-in-listfragment#15990955
    // http://cyrilmottier.com/2011/06/20/listview-tips-tricks-1-handle-emptiness/
    private ViewStub createViewForEmptyList(boolean bNetworkOff, boolean bNoBooksInDb) {
        ViewStub viewStub = new ViewStub(getActivity());

        // Make sure you import android.widget.LinearLayout.LayoutParams;
        viewStub.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        // Instead of passing resource id here I passed resolved color
        // That is, getResources().getColor((R.color.gray_dark))
        //emptyView.setTextColor(getResources().getColor(R.color.gray_dark));
        //viewStub.setText(text);
        //viewStub.setTextSize(12);
        viewStub.setVisibility(View.GONE);

        if (bNetworkOff) {
            viewStub.setLayoutResource(R.layout.empty_book_list_no_network);
        }

        else {
            viewStub.setLayoutResource(R.layout.empty_book_list_db_empty);
        }


        /*viewStub.setGravity(Gravity.CENTER_VERTICAL
                | Gravity.CENTER_HORIZONTAL);*/

        // Add empty view to the list view
        ((ViewGroup) getListView().getParent()).addView(viewStub);

        return viewStub;
    }
}
