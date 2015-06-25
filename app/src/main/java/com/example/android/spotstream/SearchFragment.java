package com.example.android.spotstream;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


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
        ArrayList<ArtistInfo> artists = new ArrayList<ArtistInfo>();

        artists.add(new ArtistInfo("Beyonce", "https://i.scdn.co/image/79e91d3cd4a7c15e0c219f4e6c941d282fe87a3d"));
        artists.add(new ArtistInfo("Slipknot", "https://i.scdn.co/image/88462cd5cf14c073c153e3aa604fb22ba14a81cc"));
        artists.add(new ArtistInfo("Weezer", "https://i.scdn.co/image/3d5059ea7707a7abb69763542adc345eb209769b"));
        artists.add(new ArtistInfo("Metallica", "https://i.scdn.co/image/2cfbc8c8e6af445b4323a3eda3ae97fe1bba8935"));
        artists.add(new ArtistInfo("Beyonce", "https://i.scdn.co/image/79e91d3cd4a7c15e0c219f4e6c941d282fe87a3d"));
        artists.add(new ArtistInfo("Slipknot", "https://i.scdn.co/image/88462cd5cf14c073c153e3aa604fb22ba14a81cc"));
        artists.add(new ArtistInfo("Weezer", "https://i.scdn.co/image/3d5059ea7707a7abb69763542adc345eb209769b"));
        artists.add(new ArtistInfo("Metallica", "https://i.scdn.co/image/2cfbc8c8e6af445b4323a3eda3ae97fe1bba8935"));
        artists.add(new ArtistInfo("Beyonce", "https://i.scdn.co/image/79e91d3cd4a7c15e0c219f4e6c941d282fe87a3d"));
        artists.add(new ArtistInfo("Slipknot", "https://i.scdn.co/image/88462cd5cf14c073c153e3aa604fb22ba14a81cc"));
        artists.add(new ArtistInfo("Weezer", "https://i.scdn.co/image/3d5059ea7707a7abb69763542adc345eb209769b"));
        artists.add(new ArtistInfo("Metallica", "https://i.scdn.co/image/2cfbc8c8e6af445b4323a3eda3ae97fe1bba8935"));

        mArtistAdapter = new ArtistArrayAdapter(getActivity(), R.layout.list_item_artist, artists);
        ListView artistsListView = (ListView)view.findViewById(R.id.list_view_artists);
        artistsListView.setAdapter(mArtistAdapter);

        return view;
    }

    /**
     * The ArtistInfo class is a pure data storage class that will be used to store information
     * about each artist.  This is a pure data storage class, so I'm not bothering with
     * getters and setters
     */
    public class ArtistInfo {
        public String mName;
        public String mImageURL;

        ArtistInfo (String name, String imageURL) {
            mName = name;
            mImageURL = imageURL;
        }
    }

    /**
     * The ArtistArrayAdapter is used to populate the image and name of the artist for each artist
     * in a ListView.  This was inspired by:
     *   http://www.ezzylearning.com/tutorial/customizing-android-listview-items-with-custom-arrayadapter
     * although I didn't like that authors use of theView tag to store random data.
     */
    public class ArtistArrayAdapter extends ArrayAdapter<ArtistInfo> {
        private final String LOG_TAG = ArtistArrayAdapter.class.getSimpleName();

        private Context mContext;
        private int mLayoutResource;
        private ArrayList<ArtistInfo> mArtistInfoData;

        /**
         * Basic cosnstructor to override Array Adapeter.  The arguments are identical to those in the
         * ArrayAdapter consturtor, except for the fact that we are requiring the data to be an
         * ArrayList of ArtistInfo objects
         *
         * TODO: Pass in resource IDs for the imageview and textview as args to constructor
         */
        public ArtistArrayAdapter(Context context, int resource, ArrayList<ArtistInfo> objects) {
            super (context,resource,objects);

            mContext = context;
            mLayoutResource = resource;
            mArtistInfoData = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mLayoutResource, parent, false);
            }

            ArtistInfo artistInfo = mArtistInfoData.get(position);

            //TextView urlText = (TextView)convertView.findViewById(R.id.list_item_artist_image);
            //urlText.setText(artistInfo.mImageURL);

            ImageView artistImageView = (ImageView)convertView.findViewById(R.id.list_item_artist_image);
            Picasso.with(mContext)
                    .load(mArtistInfoData.get(position).mImageURL)
                    .resize(200,200)
                    .centerCrop()
                    .into(artistImageView);

            TextView nameText = (TextView)convertView.findViewById(R.id.list_item_artist_name);
            nameText.setText(artistInfo.mName);

            return convertView;
        }
    }
}
