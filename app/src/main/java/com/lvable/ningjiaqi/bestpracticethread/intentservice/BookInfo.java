package com.lvable.ningjiaqi.bestpracticethread.intentservice;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ningjiaqi on 16/4/20.
 */
public class BookInfo implements Parcelable {
    public String author;
    public String title;
    public String imgUrl;
    public String summary;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.title);
        dest.writeString(this.imgUrl);
        dest.writeString(this.summary);
    }

    public BookInfo() {
    }

    protected BookInfo(Parcel in) {
        this.author = in.readString();
        this.title = in.readString();
        this.imgUrl = in.readString();
        this.summary = in.readString();
    }

    public static final Parcelable.Creator<BookInfo> CREATOR = new Parcelable.Creator<BookInfo>() {
        @Override
        public BookInfo createFromParcel(Parcel source) {
            return new BookInfo(source);
        }

        @Override
        public BookInfo[] newArray(int size) {
            return new BookInfo[size];
        }
    };
}
