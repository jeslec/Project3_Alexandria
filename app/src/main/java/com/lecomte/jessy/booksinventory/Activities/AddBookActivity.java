package com.lecomte.jessy.booksinventory.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.lecomte.jessy.booksinventory.Fragments.AddBookFragment;
import com.lecomte.jessy.booksinventory.R;
import com.lecomte.jessy.booksinventory.Services.BookService;

public class AddBookActivity extends AppCompatActivity implements AddBookFragment.Callbacks {

    private static final String TAG = AddBookActivity.class.getSimpleName();

    private FloatingActionButton mShareButton;

    private MessageReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mShareButton = (FloatingActionButton) findViewById(R.id.add_book_Share_FAB);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getHostedFragment() != null) {
                    getHostedFragment().shareBook();
                }
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        // Hide share button until user has selected a book and it is displayed
        hideShareButton();
    }

    @Override
    public void notifyBookSelected(String isbn) {
        Log.d(TAG, "notifyBookSelected");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register to receive messages from the BookService
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(BookService.MESSAGE);
        // http://stackoverflow.com/questions/16616654/registering-and-unregistering-broadcastreceiver-in-a-fragment
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private AddBookFragment getHostedFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return (AddBookFragment)fragmentManager.findFragmentById(R.id.fragment_add_book);
    }

    private void showSharebutton() {
        mShareButton.setVisibility(View.VISIBLE);
    }

    private void hideShareButton() {
        mShareButton.setVisibility(View.INVISIBLE);
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
                    if (getHostedFragment() != null) {
                        getHostedFragment().loadBookData();
                        showSharebutton();
                    }
                } else if (result == BookService.FETCH_RESULT_ALREADY_IN_DB) {
                    Log.d(TAG, "MessageReceiver#onReceive() - FETCH_RESULT_ALREADY_IN_DB");
                    if (getHostedFragment() != null) {
                        getHostedFragment().loadBookData();
                        showSharebutton();
                    }
                }

                else if (result == BookService.FETCH_RESULT_NOT_FOUND) {
                    Log.d(TAG, "MessageReceiver#onReceive() - FETCH_RESULT_NOT_FOUND");
                    /*Toast.makeText(BookListActivity.this, getResources()
                            .getString(R.string.book_not_found), Toast.LENGTH_SHORT).show();*/
                }
            }

            else if (command == BookService.DELETE_BOOK) {
                Log.d(TAG, "MessageReceiver#onReceive() - DELETE_BOOK");

                if (result == BookService.DELETE_RESULT_DELETED) {
                    Log.d(TAG, "MessageReceiver#onReceive() - DELETE_RESULT_DELETED");
                    /*Toast.makeText(BookListActivity.this, getResources()
                            .getString(R.string.book_deleted), Toast.LENGTH_SHORT).show();*/
                }

                else if (result == BookService.DELETE_RESULT_NOT_DELETED) {
                    Log.d(TAG, "MessageReceiver#onReceive() - DELETE_RESULT_NOT_DELETED");
                    /*Toast.makeText(BookListActivity.this, getResources()
                            .getString(R.string.book_not_deleted), Toast.LENGTH_SHORT).show();*/
                }
            }

            /*if (intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(BookListActivity.this, intent.getStringExtra(MESSAGE_KEY),
                        Toast.LENGTH_LONG).show();
            }*/
        }
    }
}
