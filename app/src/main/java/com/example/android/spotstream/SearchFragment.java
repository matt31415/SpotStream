package com.example.android.spotstream;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {
    TextPlusImageArrayAdapter mArtistAdapter;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        // Create some fake data
        // TODO: Replace this with real data pulled from Spotify
        ArrayList<TextPlusImage> artists = new ArrayList<TextPlusImage>();

        artists.add(new TextPlusImage("Beyonce", "https://i.scdn.co/image/79e91d3cd4a7c15e0c219f4e6c941d282fe87a3d"));
        artists.add(new TextPlusImage("Slipknot", "https://i.scdn.co/image/88462cd5cf14c073c153e3aa604fb22ba14a81cc"));
        artists.add(new TextPlusImage("Weezer", "https://i.scdn.co/image/3d5059ea7707a7abb69763542adc345eb209769b"));
        artists.add(new TextPlusImage("Metallica", "https://i.scdn.co/image/2cfbc8c8e6af445b4323a3eda3ae97fe1bba8935"));
        artists.add(new TextPlusImage("Beyonce", "https://i.scdn.co/image/79e91d3cd4a7c15e0c219f4e6c941d282fe87a3d"));
        artists.add(new TextPlusImage("Slipknot", "https://i.scdn.co/image/88462cd5cf14c073c153e3aa604fb22ba14a81cc"));
        artists.add(new TextPlusImage("Weezer", "https://i.scdn.co/image/3d5059ea7707a7abb69763542adc345eb209769b"));
        artists.add(new TextPlusImage("Metallica", "https://i.scdn.co/image/2cfbc8c8e6af445b4323a3eda3ae97fe1bba8935"));
        artists.add(new TextPlusImage("Beyonce", "https://i.scdn.co/image/79e91d3cd4a7c15e0c219f4e6c941d282fe87a3d"));
        artists.add(new TextPlusImage("Slipknot", "https://i.scdn.co/image/88462cd5cf14c073c153e3aa604fb22ba14a81cc"));
        artists.add(new TextPlusImage("Weezer", "https://i.scdn.co/image/3d5059ea7707a7abb69763542adc345eb209769b"));
        artists.add(new TextPlusImage("Metallica", "https://i.scdn.co/image/2cfbc8c8e6af445b4323a3eda3ae97fe1bba8935"));

        mArtistAdapter = new TextPlusImageArrayAdapter(getActivity(), R.layout.list_item_artist, artists,R.id.list_item_artist_name, R.id.list_item_artist_image);
        ListView artistsListView = (ListView)view.findViewById(R.id.list_view_artists);
        artistsListView.setAdapter(mArtistAdapter);

        return view;
    }

}
