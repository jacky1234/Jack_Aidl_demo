package com.jack.jack_aidl_demo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jack on 2016/6/15.
 */

public class Book implements Parcelable {
    private int bookId;
    private int bookPrice;
    private String bookName;

    public Book(int bookId, int bookPrice, String bookName) {
        this.bookId = bookId;
        this.bookPrice = bookPrice;
        this.bookName = bookName;
    }

    protected Book(Parcel in) {
        bookId = in.readInt();
        bookPrice = in.readInt();
        bookName = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookId);
        dest.writeInt(bookPrice);
        dest.writeString(bookName);
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", bookPrice=" + bookPrice +
                ", bookName='" + bookName + '\'' +
                '}';
    }
}
