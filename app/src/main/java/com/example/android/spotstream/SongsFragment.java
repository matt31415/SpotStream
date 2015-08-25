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
public class SongsFragment extends Fragment {
    private SongArrayAdapter mSongArrayAdapter;
    private final String LOG_TAG = SongsFragment.class.getSimpleName();
    private Callback mCallback;

    private static final String SPOTIFY_ARTIST_ID_KEY = "SpotifyArtistId";

    public SongsFragment() {
    }

    public static SongsFragment newInstance(String spotifyArtistId) {
        SongsFragment songsFrag = new SongsFragment();

        Bundle args = new Bundle();
        args.putString(SPOTIFY_ARTIST_ID_KEY, spotifyArtistId);
        songsFrag.setArguments(args);

        return songsFrag;
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
                for (int i = 0; i < mSongArrayAdapter.getCount(); i++) {
                    songs.add(mSongArrayAdapter.getItem(i));
                }

                mCallback.startPlayer(songs, position);
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
            String spotifyArtistId;

            // The artist ID could be in the fragment arguments (if we're in dual-pane mode), or it
            // could be in the intent (if we're in single-pane mode).
            Bundle fragArgs = getArguments();
            if (fragArgs != null) {
                spotifyArtistId = fragArgs.getString(SPOTIFY_ARTIST_ID_KEY);
            }
            else {
                spotifyArtistId = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
            }

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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * SongsFragment Callback for when an item has been selected.
         */
        public void startPlayer(ArrayList<Song> songs, int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Make sure that the parent activity has implemented the onItemSelected callback interface
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback");
        }

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
