package com.example.android.spotstream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {
    ArtistArrayAdapter mArtistAdapter;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        // Create some fake data
        // TODO: Replace this with real data pulled from Spotify
        ArrayList<Artist> artists = new ArrayList<Artist>();

        mArtistAdapter = new ArtistArrayAdapter(getActivity(), R.layout.list_item_artist, artists,R.id.list_item_artist_name, R.id.list_item_artist_image);
        ListView artistsListView = (ListView)view.findViewById(R.id.list_view_artists);
        artistsListView.setAdapter(mArtistAdapter);

        new FetchArtistsTask().execute("Boys");

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
            mArtistAdapter.clear();
            mArtistAdapter.addAll(artists);
        }
    }
}
