package com.example.android.spotstream;

/**
 * Created by mgay on 6/28/2015.
 *
 * This is a trivial data structure to store information about a single spotify song
 */
public class Song implements java.io.Serializable {
    String mTitle;
    String mArtist;
    String mAlbum;
    String mImageUrl;

    Song(String title, String artist, String album, String imageURL) {
        mTitle = title;
        mArtist = artist;
        mAlbum = album;
        mImageUrl = imageURL;
    }
}
