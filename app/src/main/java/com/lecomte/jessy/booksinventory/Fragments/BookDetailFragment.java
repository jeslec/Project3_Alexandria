package com.lecomte.jessy.booksinventory.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lecomte.jessy.booksinventory.Activities.BookDetailActivity;
import com.lecomte.jessy.booksinventory.Activities.BookListActivity;
import com.lecomte.jessy.booksinventory.BuildConfig;
import com.lecomte.jessy.booksinventory.Data.AlexandriaContract;
import com.lecomte.jessy.booksinventory.Other.Utility;
import com.lecomte.jessy.booksinventory.R;
import com.lecomte.jessy.booksinventory.Services.DownloadImage;

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link BookListActivity}
 * in two-pane mode (on tablets) or a {@link BookDetailActivity}
 * on handsets.
 */
public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = BookDetailFragment.class.getSimpleName();
    private static final int LOADER_ID = 11;
    public static final String INTENT_ACTION_DELETE_BOOK = BuildConfig.APPLICATION_ID +
            ".INTENT_ACTION_DELETE_BOOK";

    private String mItemIsbn;
    private TextView mTitleTextView;
    private TextView mSubTitleTextView;
    private ImageView mImageView;
    private TextView mDescriptionTextView;
    private TextView mAuthorTextView;
    private TextView mCategoryTextView;
    private ImageView mDeleteButton;
    private boolean mTwoPaneLayout = false;
    private Callbacks mCallbacks = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemIsbn = getArguments().getString(ARG_ITEM_ID);

            /*Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.content);
            }*/
        }

        mTwoPaneLayout = Utility.isTwoPaneLayout(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mItemIsbn != null) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    private void clearWidgets() {
        mTitleTextView.setText("");
        mSubTitleTextView.setText("");
        mDescriptionTextView.setText("");
        mImageView.setVisibility(View.INVISIBLE);

        if (mDeleteButton != null) {
            mDeleteButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);

        mTitleTextView = (TextView) rootView.findViewById(R.id.book_detail_Title);
        mSubTitleTextView = (TextView) rootView.findViewById(R.id.book_detail_SubTitle);
        mImageView = (ImageView) rootView.findViewById(R.id.book_detail_Image);
        mDescriptionTextView = (TextView) rootView.findViewById(R.id.book_detail_Description);
        mAuthorTextView = (TextView) rootView.findViewById(R.id.book_detail_Authors);
        mCategoryTextView = (TextView) rootView.findViewById(R.id.book_detail_Categories);
        mDeleteButton = (ImageButton) rootView.findViewById(R.id.book_detail_DeleteButton);

        // This button is not present in a 2-pane layout so it will be null in that case
        if (mDeleteButton != null) {
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // There's a delete button in the details view only and for single-pane layout
                    if (!mTwoPaneLayout) {
                        // Ask for user confirmation before deleting the book
                        // http://stackoverflow.com/questions/2115758/how-to-display-alert-dialog-in-android
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Delete Book")
                                .setMessage("Are you sure you want to delete this book?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                        Intent deleteBookIntent = new Intent(INTENT_ACTION_DELETE_BOOK);
                                        deleteBookIntent.setClass(getActivity(), BookListActivity.class);
                                        startActivity(deleteBookIntent);
                                    }
                                })
                            /*.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })*/
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
            });
        }

        clearWidgets();

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader() - Creating new cursor loader...");
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(mItemIsbn)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        if (!data.moveToFirst()) {
            return;
        }

        // Title
        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        mTitleTextView.setText(bookTitle);

        // SubTitle
        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        mSubTitleTextView.setText(bookSubTitle);

        // Description
        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        mDescriptionTextView.setText(desc);

        // Authors
        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        mAuthorTextView.setLines(authorsArr.length);
        mAuthorTextView.setText(authors.replace(",","\n"));

        // Categories
        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        mCategoryTextView.setText(categories);

        // Image
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage(mImageView).execute(imgUrl);
            mImageView.setVisibility(View.VISIBLE);
        }

        if (mDeleteButton != null) {
            mDeleteButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onBookAdded(String isbn) {
        mItemIsbn = isbn;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    public void shareBook() {
        Intent shareBookIntent = new Intent(Intent.ACTION_SEND);
        shareBookIntent.putExtra(Intent.EXTRA_TEXT, mTitleTextView.getText());
        shareBookIntent.setType("text/plain");
        startActivity(shareBookIntent);
    }

    // Container Activity must implement this interface
    public interface Callbacks {

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
}
