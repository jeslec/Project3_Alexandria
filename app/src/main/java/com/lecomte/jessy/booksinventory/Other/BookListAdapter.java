package com.lecomte.jessy.booksinventory.Other;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lecomte.jessy.booksinventory.Data.AlexandriaContract;
import com.lecomte.jessy.booksinventory.R;

/**
 * Created by saj on 11/01/15.
 */
public class BookListAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;

        public ViewHolder(View view) {
            bookCover = (ImageView) view.findViewById(R.id.book_list_item_Image);
            bookTitle = (TextView) view.findViewById(R.id.book_list_item_Title);
            bookSubTitle = (TextView) view.findViewById(R.id.book_list_item_SubTitle);
        }
    }

    public BookListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (viewHolder != null) {
            int columnIndex = cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL);
            String imageUrl = cursor.getString(columnIndex);

            if (imageUrl == null) {
                Glide.with(context).load(R.drawable.no_image).into(viewHolder.bookCover);
            }
            else {
                Glide.with(context).load(imageUrl).into(viewHolder.bookCover);
            }

            String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
            if (bookTitle != null) {
                viewHolder.bookTitle.setText(bookTitle);
            }

            String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
            if (bookSubTitle != null) {
                viewHolder.bookSubTitle.setText(bookSubTitle);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
}
