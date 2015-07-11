package com.example.android.spotstream;

/**
 * Created by mgay on 6/28/2015.
 *
 * This is a trivial data structure to store information about a single spotify song
 */
public class Song implements java.io.Serializable {
    String mTitle;
    String mImageUrl;

    Song(String title, String imageURL) {
        mTitle = title;
        mImageUrl = imageURL;
    }
}
