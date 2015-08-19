package com.example.android.spotstream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by mgay on 6/28/2015.
 *
 * This Song Array Adapter takes a list of of Song objects, and converts them
 * to views.  This is very similar to the ArtistArrayAdapter which is also used in
 * this project.
 */
public class SongArrayAdapter extends ArrayAdapter<Song>{
    private final String LOG_TAG = ArtistArrayAdapter.class.getSimpleName();

    private Context mContext;
    private int mLayoutResource;
    private int mTitleTextResource;
    private int mAlbumTextResource;
    private int mImageResource;

    private ArrayList<Song> mSongData;

    /**
     * A basic constructor.
     *
     * @param context The context
     * @param resource The layout resource that we'll be using
     * @param objects The list of songs
     * @param titleTextResource The layout id of the song title
     * @param imageResource The layout id of the song image
     */
    @SuppressWarnings("SameParameterValue")
    public SongArrayAdapter (
            Context context,
            int resource,
            ArrayList<Song> objects,
            int titleTextResource,
            int albumTextResource,
            int imageResource
    ) {
        super(context,resource,objects);

        mContext = context;
        mLayoutResource = resource;
        mSongData = objects;
        mTitleTextResource = titleTextResource;
        mAlbumTextResource = albumTextResource;
        mImageResource = imageResource;
    }

    /**
     * Overrides the getView function from ArrayAdapter.  Generates and returns a View of the data
     * at the passed in position.  We'll be populating the image resource with the Song's imageUrl,
     * and the titleTextResource with the song's title and album name.
     *
     * @param position index of the data item to render
     * @param convertView The view for the list element (might be null if it hasn't been created yet
     * @param parent The parent ViewGroup
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, parent, false);
        }

        Song songInfo = mSongData.get(position);

        ImageView songImageView = (ImageView) convertView.findViewById(mImageResource);

        if (songInfo.mImageUrl != null) {
            Picasso.with(mContext)
                    .load(songInfo.mImageUrl)
                    .fit()
                    .centerCrop()
                    .into(songImageView);
        }
        else {
            Picasso.with(mContext)
                    .load(R.drawable.note)
                    .fit()
                    .centerCrop()
                    .into(songImageView);
        }

        TextView nameText = (TextView)convertView.findViewById(mTitleTextResource);
        nameText.setText(songInfo.mTitle);

        TextView albumText = (TextView)convertView.findViewById(mAlbumTextResource);
        albumText.setText(songInfo.mAlbum);

        return convertView;

    }
}
