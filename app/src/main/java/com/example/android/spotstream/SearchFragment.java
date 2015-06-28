package com.example.android.spotstream;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {
    private final String LOG_TAG = SearchFragment.class.getSimpleName();


    ArtistArrayAdapter mArtistAdapter;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        //TODO: Is there a way to make it so that when we come back from the song listings, the search results are still there?

        //Set up the list of artists
        mArtistAdapter = new ArtistArrayAdapter(
                getActivity(),
                R.layout.list_item_artist,
                new ArrayList<Artist>(),
                R.id.list_item_artist_name,
                R.id.list_item_artist_image);
        ListView artistsListView = (ListView)view.findViewById(R.id.list_view_artists);
        artistsListView.setAdapter(mArtistAdapter);

        artistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtistAdapter.getItem(position);

                Intent viewSongsIntent = new Intent(getActivity(), SongsActivity.class);
                viewSongsIntent.putExtra(Intent.EXTRA_TEXT, artist.mSpotifyId);
                viewSongsIntent.putExtra(Intent.EXTRA_TITLE, artist.mName);
                startActivity(viewSongsIntent);
            }
        });

        // Set up a handler for the search string
        ((EditText)view.findViewById(R.id.edit_text_artist_search)).setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        boolean handled = false;

                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            Log.d(LOG_TAG, "Searching: " + v.getText().toString());

                            new FetchArtistsTask().execute(v.getText().toString());

                            // The EditText was really inconsistent about closing the keyboard,
                            // so we're going to do it manually
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                            handled = false;
                        }
                        return handled;
                    }
                }
        );

        return view;
    }


    public class FetchArtistsTask extends AsyncTask<String, Void, Artist[]> {
        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected Artist[] doInBackground (String... searchStr) {
            ArrayList<Artist> artists = new ArrayList<Artist>();

            //This is where we conduct our serach for the artists using the Spotify API
            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();
            ArtistsPager artistsPager = spotify.searchArtists(searchStr[0]);

            for (kaaes.spotify.webapi.android.models.Artist a : artistsPager.artists.items) {
                String artistName = a.name;
                String artistSpotifyId = a.id;
                String artistImageURL;
                if(!a.images.isEmpty()) {
                    artistImageURL = a.images.get(0).url;
                }
                else {
                    artistImageURL = null;
                }

                artists.add(new Artist(artistName, artistImageURL, artistSpotifyId));
            }
            return (Artist[]) artists.toArray(new Artist[artists.size()]);
        }

        @Override
        protected void onPostExecute( Artist[] artists) {
            ListView artistsListView = (ListView)getActivity().findViewById(R.id.list_view_artists);

            //Clear the list adapter, jump to the top, and then add the newly found artists
            mArtistAdapter.clear();
            artistsListView.smoothScrollToPosition(0);
            mArtistAdapter.addAll(artists);
        }
    }
}
