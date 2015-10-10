package com.lecomte.jessy.booksinventory.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lecomte.jessy.booksinventory.Activities.BookDetailActivity;
import com.lecomte.jessy.booksinventory.Activities.BookListActivity;
import com.lecomte.jessy.booksinventory.Data.AlexandriaContract;
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

    private String mItemIsbn;
    private TextView mTitleTextView;
    private TextView mSubTitleTextView;
    private ImageView mImageView;
    private TextView mDescriptionTextView;
    private TextView mAuthorTextView;
    private TextView mCategoryTextView;

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
        mCategoryTextView = (TextView)rootView.findViewById(R.id.book_detail_Categories);

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
}
