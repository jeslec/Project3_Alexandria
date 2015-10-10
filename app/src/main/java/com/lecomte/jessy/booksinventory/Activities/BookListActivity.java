package com.lecomte.jessy.booksinventory.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lecomte.jessy.booksinventory.Fragments.AboutFragment;
import com.lecomte.jessy.booksinventory.Fragments.AddBookFragment;
import com.lecomte.jessy.booksinventory.Fragments.BookDetailFragment;
import com.lecomte.jessy.booksinventory.Fragments.BookListFragment;
import com.lecomte.jessy.booksinventory.Fragments.DeleteBookFragment;
import com.lecomte.jessy.booksinventory.R;
import com.lecomte.jessy.booksinventory.Services.BookService;


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
        DeleteBookFragment.Callbacks {

    private static final String TAG = BookListActivity.class.getSimpleName();

    private BroadcastReceiver mMessageReceiver;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private int mBooksInListCount = 0;
    private FloatingActionButton mShareFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_app_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mShareFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
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

        if (findViewById(R.id.book_detail_container) != null) {
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

        // TODO: If exposing deep links into your app, handle intents here.
    }

    private BookListFragment getBookListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (BookListFragment)fm.findFragmentById(R.id.book_list);
    }

    private void notifyAddBookFragmentToLoadBookData() {
        FragmentManager fm = getSupportFragmentManager();
        AddBookFragment addFragment = (AddBookFragment)fm.findFragmentByTag(AddBookFragment.TAG);

        if (addFragment != null) {
            addFragment.loadBookData();
        }
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

    // Good tutorial on broadcast receivers:
    //http://www.vogella.com/tutorials/AndroidServices/article.html#servicecommunication_receiver
    // Receive messages from BookService
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "MessageReceiver#onReceive()");

            if (intent == null || !intent.hasExtra(BookService.EXTRA_COMMAND) ||
                    !intent.hasExtra(BookService.EXTRA_RESULT)) {
                Log.e(TAG, "MessageReceiver() - One of the params sent by the service is invalid");
                return;
            }

            String command = intent.getStringExtra(BookService.EXTRA_COMMAND);
            int result = intent.getIntExtra(BookService.EXTRA_RESULT, 0);
            String isbn = intent.getStringExtra(BookService.EXTRA_ISBN);

            if (command == BookService.FETCH_BOOK) {
                Log.d(TAG, "MessageReceiver#onReceive() - FETCH_BOOK");

                if (result == BookService.FETCH_RESULT_ADDED_TO_DB) {
                    Log.d(TAG, "MessageReceiver#onReceive() - FETCH_RESULT_ADDED_TO_DB");
                    notifyAddBookFragmentToLoadBookData();
                    Toast.makeText(BookListActivity.this, getResources()
                            .getString(R.string.book_added_to_library), Toast.LENGTH_SHORT).show();
                }

                else if (result == BookService.FETCH_RESULT_ALREADY_IN_DB) {
                    Log.d(TAG, "MessageReceiver#onReceive() - FETCH_RESULT_ALREADY_IN_DB");
                    notifyAddBookFragmentToLoadBookData();
                    Toast.makeText(BookListActivity.this, getResources()
                            .getString(R.string.book_already_in_library), Toast.LENGTH_SHORT).show();
                }

                else if (result == BookService.FETCH_RESULT_NOT_FOUND) {
                    Log.d(TAG, "MessageReceiver#onReceive() - FETCH_RESULT_NOT_FOUND");
                    Toast.makeText(BookListActivity.this, getResources()
                            .getString(R.string.book_not_found), Toast.LENGTH_SHORT).show();
                }
            }

            else if (command == BookService.DELETE_BOOK) {
                Log.d(TAG, "MessageReceiver#onReceive() - DELETE_BOOK");

                if (result == BookService.DELETE_RESULT_DELETED) {
                    Log.d(TAG, "MessageReceiver#onReceive() - DELETE_RESULT_DELETED");
                    Toast.makeText(BookListActivity.this, getResources()
                            .getString(R.string.book_deleted), Toast.LENGTH_SHORT).show();
                }

                else if (result == BookService.DELETE_RESULT_NOT_DELETED) {
                    Log.d(TAG, "MessageReceiver#onReceive() - DELETE_RESULT_NOT_DELETED");
                    Toast.makeText(BookListActivity.this, getResources()
                            .getString(R.string.book_not_deleted), Toast.LENGTH_SHORT).show();
                }
            }

            /*if (intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(BookListActivity.this, intent.getStringExtra(MESSAGE_KEY),
                        Toast.LENGTH_LONG).show();
            }*/
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
        Toast.makeText(this, "Add Book", Toast.LENGTH_SHORT).show();
        if (mTwoPane) {
            AddBookFragment fragment = null;
            FragmentManager fragMgr = getSupportFragmentManager();
            fragment = (AddBookFragment) fragMgr.findFragmentByTag(AddBookFragment.TAG);

            FragmentTransaction fragmentTransaction = fragMgr.beginTransaction();

            if (fragment == null) {
                Log.d(TAG, "AddBookActivityFragment not found, creating a new one and putting it in layout");
                fragment = AddBookFragment.newInstance(mTwoPane);
                fragmentTransaction.add(fragment, AddBookFragment.TAG);
            } else {
                Log.d(TAG, "AddBookActivityFragment found, putting it in layout...");
                fragmentTransaction.remove(fragment)
                        .add(fragment, AddBookFragment.TAG);
            }
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(this, AddBookActivity.class);
            intent.putExtra(AddBookFragment.EXTRA_BOOL_2PANE, mTwoPane);
            startActivity(intent);
        }
    }

    private void loadDeleteBookConfirmationView() {
        if (mTwoPane) {
            DeleteBookFragment fragment = null;
            FragmentManager fragMgr = getSupportFragmentManager();
            fragment = (DeleteBookFragment) fragMgr.findFragmentByTag(AddBookFragment.TAG);

            FragmentTransaction fragmentTransaction = fragMgr.beginTransaction();

            if (fragment == null) {
                Log.d(TAG, "AddBookActivityFragment not found, creating a new one and putting it in layout");
                fragment = DeleteBookFragment.newInstance(mTwoPane);
                fragmentTransaction.add(fragment, DeleteBookFragment.TAG);
            } else {
                Log.d(TAG, "AddBookActivityFragment found, putting it in layout...");
                fragmentTransaction.remove(fragment)
                        .add(fragment, DeleteBookFragment.TAG);
            }
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(this, DeleteBookActivity.class);
            intent.putExtra(DeleteBookFragment.EXTRA_BOOL_2PANE, mTwoPane);
            startActivity(intent);
        }
    }

    private void loadAboutView() {
        if (mTwoPane) {
            AboutFragment fragment = null;
            FragmentManager fragMgr = getSupportFragmentManager();
            fragment = (AboutFragment) fragMgr.findFragmentByTag(AboutFragment.TAG);

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
            intent.putExtra(AboutFragment.EXTRA_BOOL_2PANE, mTwoPane);
            startActivity(intent);
        }
    }

    /**
     * Callback method from {@link BookListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onBookForcedSelection(String isbn) {
        // 2-pane layout: load book detains into fragment; 1-pane: do nothing
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
        mShareFloatingActionButton.setVisibility(bVisible? View.VISIBLE: View.INVISIBLE);
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

        else if (id == R.id.menu_delete_book) {
            loadDeleteBookConfirmationView();
            return true;
        }

        else if (id == R.id.menu_about) {
            loadAboutView();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Register to receive messages from the BookService
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(BookService.MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
}
