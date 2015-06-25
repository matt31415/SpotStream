package com.example.android.spotstream;

/**
 * The TextPlusImage class is a pure data storage class that will be used to store information
 * about each artist.
 */
public class TextPlusImage {
    public String mText;
    public String mImageURL;

    TextPlusImage(String name, String imageURL) {
        mText = name;
        mImageURL = imageURL;
    }
}
