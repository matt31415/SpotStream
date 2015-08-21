package com.example.android.spotstream;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class SongsActivityFragment extends Fragment {
    private SongArrayAdapter mSongArrayAdapter;
    private final String LOG_TAG = SongsActivityFragment.class.getSimpleName();

    public SongsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO: Make sure that when you hit the back button from the player the song list reloads
        View view =  inflater.inflate(R.layout.fragment_songs, container, false);

        //Set up the list of artists
        mSongArrayAdapter = new SongArrayAdapter(
                getActivity(),
                R.layout.list_item_song,
                new ArrayList<Song>(),
                R.id.list_item_song_name,
                R.id.list_item_album_name,
                R.id.list_item_song_image);
        ListView songListView = (ListView)view.findViewById(R.id.list_view_songs);
        songListView.setAdapter(mSongArrayAdapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // We're going to pass all of the songs into the intent.  This is maybe a bit waasteful,
                // but each song is really cheap (in terms of data), and we're limited to 10 songs
                // by the current implementation of the API.  So, passing in an array to the intent
                // is much simpler than doing something like creating a content provider.
                ArrayList<Song> songs = new ArrayList<Song>(mSongArrayAdapter.getCount());
                for (int i=0; i < mSongArrayAdapter.getCount(); i++) {
                    songs.add(mSongArrayAdapter.getItem(i));
                }

                Intent playSongsIntent = new Intent(getActivity(), PlayerActivity.class);
                playSongsIntent.putExtra(getString(R.string.player_songs_key), songs);
                playSongsIntent.putExtra(getString(R.string.player_song_position_key), position);
                startActivity(playSongsIntent);
            }
        });

        //We may already have the list of songs
        if(savedInstanceState != null) {
            if(savedInstanceState.getSerializable(getString(R.string.songs_activity_saved_songs_key)) != null) {
                ArrayList<Song> savedSongs = (ArrayList<Song>) savedInstanceState.getSerializable(getString(R.string.songs_activity_saved_songs_key));
                mSongArrayAdapter.addAll(savedSongs);
            }
        }
        else {
            String spotifyArtistId = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
            new FetchSongsTask().execute(spotifyArtistId);
        }

        return view;
    }

    /**
     * When we need to save the instance state, we'll store the list of songs.  Fortunately the list
     * is trivial to serialize!
     * @param state State bundle into which the state should be written
     */
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        //Convert list of songs to an arrayList
        ArrayList<Song> songs = new ArrayList<>();
        for (int songIdx = 0; songIdx < mSongArrayAdapter.getCount(); songIdx++) {
            songs.add(mSongArrayAdapter.getItem(songIdx));
        }

        //save songs in bundle
        state.putSerializable(getString(R.string.songs_activity_saved_songs_key), songs);
    }

    private class FetchSongsTask extends AsyncTask<String, Void, Song[]> {
        private final String LOG_TAG = FetchSongsTask.class.getSimpleName();

        @Override
        protected Song[] doInBackground (String... artistSpotifyIds) {
            ArrayList<Song> songs = new ArrayList<>();

            //This is where we conduct our search for the artists using the Spotify API
            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String country = preferences.getString(getString(R.string.pref_country_key), getString(R.string.pref_country_default));

            Map<String,Object> options = new HashMap<>();
            options.put("country", country);

            Tracks tracks = new Tracks();
            try {
                tracks = spotify.getArtistTopTrack(artistSpotifyIds[0], options);
            }
            catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.e(LOG_TAG, spotifyError.toString());
            }

            if( tracks.tracks != null) {
                for (Track t : tracks.tracks) {
                    String songImageUrl;

                    if (!t.album.images.isEmpty()) {
                        songImageUrl = t.album.images.get(0).url;
                    } else {
                        songImageUrl = null;
                    }

                    songs.add(new Song(t.name, t.artists.get(0).name, t.album.name, songImageUrl, t.preview_url));

                }
            }

            return songs.toArray(new Song[songs.size()]);
        }

        @Override
        protected void onPostExecute( Song[] songs) {
            Activity activity = getActivity();

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
