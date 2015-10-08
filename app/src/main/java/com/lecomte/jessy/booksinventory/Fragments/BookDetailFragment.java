package com.lecomte.jessy.booksinventory.Fragments;

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
import android.widget.Button;
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
    private TextView mBookTitle;
    private TextView mBookSubTitle;
    private ImageView mBookImage;
    private TextView mBookDescription;
    private Button mShareButton;

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
        mBookTitle.setText("");
        mBookSubTitle.setText("");
        mBookDescription.setText("");
        mBookImage.setVisibility(View.INVISIBLE);
        mShareButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);

        mBookTitle = (TextView) rootView.findViewById(R.id.book_detail_Title);
        mBookSubTitle = (TextView) rootView.findViewById(R.id.book_detail_SubTitle);
        mBookImage = (ImageView) rootView.findViewById(R.id.book_detail_Image);
        mBookDescription = (TextView) rootView.findViewById(R.id.book_detail_Description);
        mShareButton = (Button) rootView.findViewById(R.id.book_detail_ShareButton);

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

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        mBookTitle.setText(bookTitle);

        /*Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text)+bookTitle);
        shareActionProvider.setShareIntent(shareIntent);*/

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        mBookSubTitle.setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        mBookDescription.setText(desc);

        /*String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));*/

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage(mBookImage).execute(imgUrl);
            mBookImage.setVisibility(View.VISIBLE);
        }

        /*String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        if(rootView.findViewById(R.id.right_container)!=null){
            rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }*/

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onBookAdded(String isbn) {
        mItemIsbn = isbn;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }
}
