package com.example.android.spotstream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends Fragment {

    public PlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        Song song = (Song)getActivity().getIntent().getSerializableExtra(getString(R.string.player_songs_key));

        TextView artistText = (TextView) view.findViewById(R.id.player_artist_text);
        artistText.setText(song.mArtist);

        TextView albumText = (TextView) view.findViewById(R.id.player_album_text);
        albumText.setText(song.mAlbum);

        TextView songText = (TextView) view.findViewById(R.id.player_song_text);
        songText.setText(song.mTitle);

        ImageView songImageView = (ImageView) view.findViewById(R.id.player_album_image);

        if (song.mImageUrl != null) {
            Picasso.with(view.getContext())
                    .load(song.mImageUrl)
                    .fit()
                    .centerCrop()
                    .into(songImageView);
        }
        else {
            Picasso.with(view.getContext())
                    .load(R.drawable.note)
                    .fit()
                    .centerCrop()
                    .into(songImageView);
        }

        return view;
    }
}
