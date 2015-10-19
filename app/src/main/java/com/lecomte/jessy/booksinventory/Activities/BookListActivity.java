package com.lecomte.jessy.booksinventory.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lecomte.jessy.booksinventory.BuildConfig;
import com.lecomte.jessy.booksinventory.Fragments.AboutFragment;
import com.lecomte.jessy.booksinventory.Fragments.AddBookFragment;
import com.lecomte.jessy.booksinventory.Fragments.BookDetailFragment;
import com.lecomte.jessy.booksinventory.Fragments.BookListFragment;
import com.lecomte.jessy.booksinventory.Other.Utility;
import com.lecomte.jessy.booksinventory.R;


/**
 * An activity representing a list of Books. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BookDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link BookListFragment} and the item details
 * (if present) is a {@link BookDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link BookListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class BookListActivity extends AppCompatActivity
        implements BookListFragment.Callbacks,
        AddBookFragment.Callbacks,
        BookDetailFragment.Callbacks {

    private static final String TAG = BookListActivity.class.getSimpleName();
    public static final String EXTRA_BOOL_2PANE = BuildConfig.APPLICATION_ID + ".EXTRA_BOOL_2PANE";

    //private BroadcastReceiver mMessageReceiver;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private int mBooksInListCount = 0;
    private FloatingActionButton mShareFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mShareFloatingActionButton = (FloatingActionButton) findViewById(R.id.book_list_Share_FAB);
        mShareFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookListFragment bookListFragment = getBookListFragment();

                if (bookListFragment == null) {
                    return;
                }

                bookListFragment.shareSelectedBook();

                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        // An alternative way of determining if it's a 2-pane layout would have been to
        // look for the Id of the details fragment's container (R.id.book_detail_container)
        if (Utility.isTwoPaneLayout(this)) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((BookListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.book_list))
                    .setActivateOnItemClick(true);
        }
    }

    private BookListFragment getBookListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (BookListFragment)fm.findFragmentById(R.id.book_list);
    }

    private void setSelectedBook(String isbn) {

        FragmentManager fm = getSupportFragmentManager();
        BookListFragment bookListFragment = (BookListFragment) fm.findFragmentById(R.id.book_list);

        if (bookListFragment != null) {
            bookListFragment.notifyOnBookClicked(isbn);
        }
    }

    @Override
    public void notifyBookSelected(String isbn) {
        // Select book in list
        setSelectedBook(isbn);

        // Show details view for this book
        loadBookDetailsView(isbn);
    }

    @Override
    public void onFetchError() {
        if (getBookListFragment() != null) {
            getBookListFragment().reloadList();
        }
    }

    public void onDeleteBookRequest() {
        boolean bBookDeleted = false;
        BookListFragment bookListFragment = (BookListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.book_list);

        if (bookListFragment != null) {
            bBookDeleted = bookListFragment.deleteSelectedBook();
        }

        // If book was not deleted, it means no book was selected in the list
        if (!bBookDeleted) {
            Toast.makeText(this, R.string.book_not_selected, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBookDetailsFragment(String isbn) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(BookDetailFragment.ARG_ITEM_ID, isbn);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.book_detail_container, fragment)
                    .commit();
        }
    }

    private void loadBookDetailsView(String isbn) {
        if (mTwoPane) {
            loadBookDetailsFragment(isbn);
        }

        // In single-pane mode, simply start the detail activity for the selected item ID
        else {
            Intent detailIntent = new Intent(this, BookDetailActivity.class);
            detailIntent.putExtra(BookDetailFragment.ARG_ITEM_ID, isbn);
            startActivity(detailIntent);
        }
    }

    private void loadAddBookView() {
        if (mTwoPane) {
            AddBookFragment fragment;
            FragmentManager fragMgr = getSupportFragmentManager();
            fragment = (AddBookFragment) fragMgr.findFragmentById(R.id.fragment_add_book);

            FragmentTransaction fragmentTransaction = fragMgr.beginTransaction();

            if (fragment == null) {
                Log.d(TAG, "AddBookActivityFragment not found, creating a new one and putting it in layout");
                fragment = AddBookFragment.newInstance();
                fragmentTransaction.add(fragment, AddBookFragment.TAG);
            } else {
                Log.d(TAG, "AddBookActivityFragment found, putting it in layout...");
                fragmentTransaction.remove(fragment)
                        .add(fragment, AddBookFragment.TAG);
            }
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(this, AddBookActivity.class);
            startActivity(intent);
        }
    }

    private void loadAboutView() {
        if (mTwoPane) {
            AboutFragment fragment;
            FragmentManager fragMgr = getSupportFragmentManager();
            fragment = (AboutFragment) fragMgr.findFragmentById(R.id.fragment_about);

            FragmentTransaction fragmentTransaction = fragMgr.beginTransaction();

            if (fragment == null) {
                Log.d(TAG, "AboutActivityFragment not found, creating a new one and putting it in layout");
                fragment = AboutFragment.newInstance(mTwoPane);
                fragmentTransaction.add(fragment, AboutFragment.TAG);
            } else {
                Log.d(TAG, "AboutActivityFragment found, putting it in layout...");
                fragmentTransaction.remove(fragment)
                        .add(fragment, AboutFragment.TAG);
            }
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(this, AboutActivity.class);
            intent.putExtra(BookListActivity.EXTRA_BOOL_2PANE, mTwoPane);
            startActivity(intent);
        }
    }

    /**
     * Callback method from {@link BookListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onBookForcedSelection(String isbn) {
        // 2-pane layout: load book details into fragment; 1-pane: do nothing
        loadBookDetailsFragment(isbn);
    }

    // User clicked on a book item within the list of books
    public void onBookClicked(String isbn) {

        // 2-pane layout: load book details into fragment; 1-pane: load into a separate activity
        loadBookDetailsView(isbn);
    }

    @Override
    public void onBookListLoadFinished(int bookCount) {
        Log.d(TAG, "onBookListLoadFinished() - Books in list: " + bookCount);
        // This value will be used in onPrepareOptionsMenu()
        mBooksInListCount = bookCount;

        //  Must call this so the menu gets updated right away
        invalidateOptionsMenu();
    }

    // Receive messages sent by activities when app running in a single layout
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent == null || intent.getAction() == null) {
            Log.d(TAG, "onNewIntent() - Intent or intent action is null!");
            return;
        }

        // We have received a request, sent by an activity, to delete the selected book
        if (intent.getAction() == BookDetailFragment.INTENT_ACTION_DELETE_BOOK) {
            onDeleteBookRequest();
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem deleteBookMenuItem = menu.findItem(R.id.menu_delete_book);

        if (deleteBookMenuItem == null) {
            Log.d(TAG, "onPrepareOptionsMenu() - deleteBookMenuItem is null!");
            return true;
        }

        // Single pane or no books: hide DeleteBook icon & share button
        boolean bVisible = mTwoPane && mBooksInListCount > 0;
        deleteBookMenuItem.setVisible(bVisible);
        mShareFloatingActionButton.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            return true;
        }

        else if (id == R.id.menu_add_book) {
            loadAddBookView();
            return true;
        }

        // This menu option is only available in a 2-pane layout
        else if (id == R.id.menu_delete_book) {
            onDeleteBookRequest();
            return true;
        }

        else if (id == R.id.menu_about) {
            loadAboutView();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
