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
import android.widget.Toast;

import com.lecomte.jessy.booksinventory.Fragments.AddBookFragment;
import com.lecomte.jessy.booksinventory.R;
import com.lecomte.jessy.booksinventory.Services.BookService;

public class AddBookActivity extends AppCompatActivity implements AddBookFragment.Callbacks {

    private static final String TAG = AddBookActivity.class.getSimpleName();

    private FloatingActionButton mShareButton;

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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
}
