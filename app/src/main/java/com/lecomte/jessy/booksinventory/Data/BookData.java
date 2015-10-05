package com.lecomte.jessy.booksinventory.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jessy on 2015-10-05.
 */
public class BookData implements Parcelable {
    private String Id;
    private String mTitle;
    private String mSubTitle;
    private String mDescription;
    private String mAuthors;
    private String mImageUrl;
    private String mCategories;

    public BookData() {
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getAuthors() {
        return mAuthors;
    }

    public void setAuthors(String authors) {
        mAuthors = authors;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getCategories() {
        return mCategories;
    }

    public void setCategories(String categories) {
        mCategories = categories;
    }

    protected BookData(Parcel in) {
        Id = in.readString();
        mTitle = in.readString();
        mSubTitle = in.readString();
        mDescription = in.readString();
        mAuthors = in.readString();
        mImageUrl = in.readString();
        mCategories = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Id);
        dest.writeString(mTitle);
        dest.writeString(mSubTitle);
        dest.writeString(mDescription);
        dest.writeString(mAuthors);
        dest.writeString(mImageUrl);
        dest.writeString(mCategories);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BookData> CREATOR = new Parcelable.Creator<BookData>() {
        @Override
        public BookData createFromParcel(Parcel in) {
            return new BookData(in);
        }

        @Override
        public BookData[] newArray(int size) {
            return new BookData[size];
        }
    };
}