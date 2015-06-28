package com.example.android.spotstream;

/**
 * The Artist class is a pure data storage class that will be used to store information
 * about each artist.
 */
public class Artist {
    public String mName;
    public String mImageUrl;
    public String mSpotifyId;

    Artist(String name, String imageURL, String spotifyId) {
        mName = name;
        mImageUrl = imageURL;
        mSpotifyId = spotifyId;
    }
}
