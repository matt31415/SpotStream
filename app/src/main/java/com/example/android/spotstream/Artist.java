package com.example.android.spotstream;

/**
 * The Artist class is a pure data storage class that will be used to store information
 * about each artist.
 */
public class Artist {
    public String mName;
    public String mImageURL;
    public String mSpotifyId;

    Artist(String name, String imageURL, String spotifyId) {
        mName = name;
        mImageURL = imageURL;
        mSpotifyId = spotifyId;
    }
}
