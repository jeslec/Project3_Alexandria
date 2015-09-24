package com.lecomte.jessy.booksinventory;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddBookFragment extends DialogFragment {

    public static final String TAG = AddBookFragment.class.getSimpleName();
    public static final String EXTRA_BOOL_2PANE = BuildConfig.APPLICATION_ID + ".EXTRA_BOOL_2PANE";

    private float mWidthMultiplier = 1;
    private float mHeightMultiplier = 1;
    private float mDimAmount = 0;

    private boolean mTwoPaneLayout;
    private ImageButton mClearIsbnButton;
    private ImageButton mScanIsbnButton;
    private TextView mIsbnTextView;
    private TextView mBookTitleTextView;
    private TextView mBookSubTitleTextView;
    private TextView mAuthorTextView;
    private String mPreviousIsbn = "";

    public AddBookFragment() {
    }

    public static AddBookFragment newInstance(boolean twoPane) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_BOOL_2PANE, twoPane);
        AddBookFragment fragment = new AddBookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Bundle fragmentArguments = getArguments();
        Intent intent = getActivity().getIntent();

        // Get arguments attached to this fragment (if any)
        if (fragmentArguments != null) {
            mTwoPaneLayout = fragmentArguments.getBoolean(EXTRA_BOOL_2PANE);
            Log.d(TAG, "onCreate() - Intent arguments received [2-pane layout: " + mTwoPaneLayout + "]");
        } else if (intent != null) {
            mTwoPaneLayout = intent.getBooleanExtra(EXTRA_BOOL_2PANE, false);
            Log.d(TAG, "onCreate() - Intent extra received [2-pane layout: " + mTwoPaneLayout + "]");
        }

        //Log.d(TAG, "onCreate() - Intent extra received [2-pane layout: " + mTwoPaneLayout + "]");
        // Maintain states between configuration changes (phone rotations, etc.)
        //setRetainInstance(true);

        // Required to get action bar back button to do something useful (go back to previous view)
        //setHasOptionsMenu(true);

        // Make the dialog modal so it does not accept input outside the dialog area
        // http://stackoverflow.com/questions/12322356/how-to-make-dialogfragment-modal
        setStyle(STYLE_NO_FRAME, 0);

        // Extract float values from dimens.xml explained here:
        // http://stackoverflow.com/questions/3282390/add-floating-point-value-to-android-resources-values#8780360
        // This is done only if NowPlayingFragment is a dialog (as opposed to fullscreen activity)
        if (mTwoPaneLayout) {
            TypedValue width = new TypedValue();
            TypedValue height = new TypedValue();
            TypedValue dim = new TypedValue();

            getResources().getValue(R.dimen.about_dialog_window_width, width, true);
            getResources().getValue(R.dimen.about_dialog_window_height, height, true);
            getResources().getValue(R.dimen.about_dialog_dim_behind_amount, dim, true);

            mWidthMultiplier = width.getFloat();
            mHeightMultiplier = height.getFloat();
            mDimAmount = dim.getFloat();
        }
    }

    // See section: Showing a Dialog Fullscreen or as an Embedded Fragment from
    // http://developer.android.com/guide/topics/ui/dialogs.html
    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        // Hide this dialog's custom title if we are in the 1-pane scenario
        int visibility = mTwoPaneLayout? View.VISIBLE : View.GONE;
        rootView.findViewById(R.id.add_book_title_frame).setVisibility(visibility);

        // Widgets references
        mClearIsbnButton = (ImageButton)rootView.findViewById(R.id.clear_isbn_imageButton);
        mScanIsbnButton = (ImageButton)rootView.findViewById(R.id.scan_isbn_imageButton);
        mIsbnTextView = (TextView)rootView.findViewById(R.id.isbn_editText);
        mBookTitleTextView = (TextView)rootView.findViewById(R.id.book_title_textView);
        mBookSubTitleTextView = (TextView)rootView.findViewById(R.id.book_subtitle_textView);
        mAuthorTextView = (TextView)rootView.findViewById(R.id.author_textView);

        // Widgets events handlers

        mClearIsbnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearWidgets();
            }
        });

        mScanIsbnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make sure to use the code for a fragment (not the same as the code for activity)
                IntentIntegrator.forSupportFragment(AddBookFragment.this).initiateScan();
            }
        });

        mIsbnTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged(): " + editable + " [Length: " + editable.length() + "]");
                String isbn = editable.toString();

                if (isbn.length() < 13 || isbn.equals(mPreviousIsbn)) {
                    return;
                }

                Toast.makeText(getActivity(), "Downloading book data for ISBN: " + isbn,
                        Toast.LENGTH_LONG).show();
                //sendLoadBookCommandToService(ean);
                mPreviousIsbn = isbn;
            }
        });

        return rootView;
    }

    // Clear search field and result widgets
    private void clearWidgets() {
        mIsbnTextView.setText("");
        mBookTitleTextView.setText("");
        mBookSubTitleTextView.setText("");
        mAuthorTextView.setText("");
        mPreviousIsbn = "";
    }

    // The system calls this only when creating the layout in a dialog
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog()");

        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Resize dialog window (a dialog window is only used in a 2-pane configuration,
        // in a 1-pane configuration we use a fullscreen activity)
        // Resizing of window must be done in onStart() or onResume() as explained here:
        // http://w3facility.org/question/how-to-set-dialogfragments-width-and-height/?r=3#answer-21966763
        if (mTwoPaneLayout) {
            Window dialogWindow = getDialog().getWindow();
            int dimFlag = WindowManager.LayoutParams.FLAG_DIM_BEHIND;

            // Get screen dimensions and other display metrics, code from:
            // http://developer.android.com/reference/android/util/DisplayMetrics.html
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            // Resize dialog window so it takes maximum advantage of each device's screen size
            dialogWindow.setLayout((int) (mWidthMultiplier * metrics.widthPixels),
                    (int)(mHeightMultiplier * metrics.heightPixels));

            // Dim behind this dialog (must be called after dialog is created and view is set)
            dialogWindow.setFlags(dimFlag, dimFlag);
            dialogWindow.setDimAmount(mDimAmount);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scan = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (scan != null) {
            mIsbnTextView.setText(scan.getContents());
        }
    }

    //http://stackoverflow.com/questions/12433397/android-dialogfragment-disappears-after-orientation-change#12434038
    // Whitout this code, the fragment disapears when there's a configuration change
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
