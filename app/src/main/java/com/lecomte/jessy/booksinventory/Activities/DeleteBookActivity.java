package com.lecomte.jessy.booksinventory.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.lecomte.jessy.booksinventory.Fragments.DeleteBookFragment;
import com.lecomte.jessy.booksinventory.R;

public class DeleteBookActivity extends AppCompatActivity implements DeleteBookFragment.Callbacks {

    private static final String TAG = DeleteBookActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.delete_book_Share_FAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onDeleteBookRequest() {
        Log.d(TAG, "onDeleteBookRequest()");
    }
}
