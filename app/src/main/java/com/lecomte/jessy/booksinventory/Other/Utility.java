package com.lecomte.jessy.booksinventory.Other;

/**
 * Created by Jessy on 2015-09-24.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.lecomte.jessy.booksinventory.R;
import com.lecomte.jessy.booksinventory.Services.BookService;

/**
 * Created by Jessy on 2015-09-22.
 */
public class Utility {

    private static final String TAG = Utility.class.getSimpleName();

    // Check if app has access to Internet either using Wifi or Mobile
    // Returns:
    //  -true: has access to Internet (wifi or mobile)
    //  -false: does not have access to Internet
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Return true if app is using a 2-pane layout
    public static boolean isTwoPaneLayout(Context context) {
        boolean bTwoPane = false;
        try {
            bTwoPane = context.getResources().getBoolean(R.bool.twoPaneLayout);
        } catch (Resources.NotFoundException e) {
            // An exception means there is no value in file so it's a 1-pane layout
            Log.d(TAG, "isTwoPaneLayout() - NotFoundException!");
        }
        return bTwoPane;
    }

    @SuppressWarnings("ResourceType")
    static public @BookService.FetchResult int getFetchStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(context.getString(R.string.pref_fetch_result),
                BookService.FETCH_RESULT_UNKNOWN);
    }

    // Download (if not already in cache) then load the image at url into the specified view
    // On error (e.g. no network), set a default image
    public static void loadImage(final Context context, String url, final ImageView view) {

        if (context == null || view == null) {
            return;
        }

        // If no url is specified, just load the default image
        if (url == null) {
            Glide.with(context).load(R.drawable.no_image).into(view);
            return;
        }

        if (url != null && view != null) {
            Glide.with(context)
                    .load(url)
                    // If image can't be downloaded, set a default image
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Glide.with(context).load(R.drawable.no_image).into(view);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(view);
        }
    }
}

