package com.example.android.spotstream;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class SongsActivityFragment extends Fragment {

    SongArrayAdapter mSongArrayAdapter;

    public SongsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_songs, container, false);

        //Set up the list of artists
        mSongArrayAdapter = new SongArrayAdapter(
                getActivity(),
                R.layout.list_item_song,
                new ArrayList<Song>(),
                R.id.list_item_song_name,
                R.id.list_item_song_image);
        ListView songListView = (ListView)view.findViewById(R.id.list_view_songs);
        songListView.setAdapter(mSongArrayAdapter);

        String spotifyArtistId = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
        new FetchSongsTask().execute(spotifyArtistId);

        return view;
    }



    public class FetchSongsTask extends AsyncTask<String, Void, Song[]> {
        private final String LOG_TAG = FetchSongsTask.class.getSimpleName();

        @Override
        protected Song[] doInBackground (String... artistSpotifyIds) {
            ArrayList<Song> songs = new ArrayList<Song>();

            //This is where we conduct our serach for the artists using the Spotify API
            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();

            // TODO: Make "county" a setting in the preferneces menu
            String country = "us";
            Map<String,String> options = new HashMap<String, String>();
            options.put("country", country);

            Tracks tracks = spotify.getArtistTopTrack(artistSpotifyIds[0], options);

            for(Track t: tracks.tracks) {
                String songTitle = t.name;
                String songAlbumName = t.album.name;
                String songImageUrl;

                if(!t.album.images.isEmpty()) {
                    songImageUrl = t.album.images.get(0).url;
                }
                else {
                    songImageUrl = null;
                }

                songs.add(new Song(songTitle + "\n" + songAlbumName, songImageUrl));

            }
            return new Song[0];
            //return (Song[]) songs.toArray(new Song[songs.size()]);
        }

        @Override
        protected void onPostExecute( Song[] songs) {
            Activity activity = getActivity();

            ListView songListView = (ListView)activity.findViewById(R.id.list_view_songs);

            //Clear the list adapter, and then add the newly found artists
            mSongArrayAdapter.clear();
            mSongArrayAdapter.addAll(songs);

            // If no artists were found, display a toast
            if (songs.length == 0) {
                Toast noSongsToast = Toast.makeText(activity, activity.getString(R.string.no_songs_found_toast), Toast.LENGTH_SHORT) ;
                noSongsToast.show();
            }

        }
    }

}
