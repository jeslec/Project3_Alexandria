package com.lecomte.jessy.booksinventory.Data;

import android.content.Context;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lecomte.jessy.booksinventory.R;
import com.lecomte.jessy.booksinventory.Services.DownloadImage;

import java.util.ArrayList;

/**
 * Created by Jessy on 2015-10-05.
 */
public class BookListAdapter extends ArrayAdapter<BookData> {

    private LayoutInflater mInflater;

    public BookListAdapter(Context context, int resource, BookData[] objects) {
        super(context, resource, objects);
        // Get layout inflater from context
        // http://stackoverflow.com/questions/10685116/getlayoutinflater-inside-custom-simplecursoradapter#10685309
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // If a view was not created already, create it.
        // Once created reuse it at each call to getView()
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.book_list_item, parent, false);
        }
        // Get the data from the selected list item
        BookData data = getItem(position);

        ImageView image = (ImageView)convertView.findViewById(R.id.book_list_item_Image);
        TextView title = (TextView)convertView.findViewById(R.id.book_list_item_Title);
        TextView subTitle = (TextView)convertView.findViewById(R.id.book_list_item_SubTitle);

        /*if(Patterns.WEB_URL.matcher(data.getImageUrl()).matches()){
            new DownloadImage(image).execute(data.getImageUrl());
        }*/

        title.setText(data.getTitle());
        subTitle.setText(data.getSubTitle());

        return convertView;
    }
}
