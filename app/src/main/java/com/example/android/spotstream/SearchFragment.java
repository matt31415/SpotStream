package com.example.android.spotstream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

        // Make the X on the search edit text clear out the search string.  The X is in the
        // drawable right position
        // This code was heavily inspired by:
        //   http://stackoverflow.com/questions/3554377/handling-click-events-on-a-drawable-within-an-edittext
        final EditText searchEdit = ((EditText) view.findViewById(R.id.edit_text_artist_search));
        searchEdit.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_RIGHT = 2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // This logic relies on the fact that the we already know that the click event is
                // taking place inside the EditText, so bounds-checking is super-easy
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    //The X Icon (drawable_right) may not actually exist.  If that's the case we can
                    // skip this whole exercise
                    Drawable xIconDrawable = searchEdit.getCompoundDrawables()[DRAWABLE_RIGHT];
                    if(xIconDrawable == null) {
                        return false;
                    }

                    float rawX = event.getRawX();
                    float editRightEdge = searchEdit.getRight();
                    float drawableWidth = xIconDrawable.getBounds().width();

                    if(rawX > editRightEdge - drawableWidth) {
                        searchEdit.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        // Add and remove the "clear" X to the edit text
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 0) {
                    searchEdit.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_clear_black_18dp,0);
                }
                else {
                    searchEdit.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                }
            }
        });

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

    /**
     * By overriding the onStart method, we can check to see if there's any text in the search field
     * and use it to re-run the search.  (This is perhaps a bit wasteful, since we could just store
     * the state of the list view, but I don't think it's a major issue, since this app is going to
     * be used heavily for searching, so if the network connection is slow the app will be annoying
     * all the time, not just when orientation changes.)
     */
    @Override
    public void onStart() {
        EditText searchEdit = ((EditText) getView().findViewById(R.id.edit_text_artist_search));
        String searchString = searchEdit.getText().toString();

        if(searchString.length() > 0) {
            new FetchArtistsTask().execute(searchString);
        }

        super.onStart();
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

        /**
         * After the task has completed, we'll want to update the artist adapter with the new search results.
         * @param artists - List of artists returned
         */
        //TODO: Handle the case where no results are returned
        @Override
        protected void onPostExecute( Artist[] artists) {
            Activity activity = getActivity();

            ListView artistsListView = (ListView)activity.findViewById(R.id.list_view_artists);

            // Clear the list adapter, jump to the top, and then add the newly found artists
            mArtistAdapter.clear();
            artistsListView.smoothScrollToPosition(0);
            mArtistAdapter.addAll(artists);

            // If no artists were found, display a toast
            if (artists.length == 0) {
                //Find the bottom of the search box
                int searchEditCoords[] = new int[2];
                EditText searchEdit = ((EditText) activity.findViewById(R.id.edit_text_artist_search));
                searchEdit.getLocationOnScreen(searchEditCoords);

                int toastOffset = searchEditCoords[1] + searchEdit.getMeasuredHeight()/2;//+R.dimen.edit_text_artist_search_height/2;

                Toast noArtistToast = Toast.makeText(activity, activity.getString(R.string.no_artists_found_toast), Toast.LENGTH_SHORT) ;
                noArtistToast.setGravity(Gravity.TOP,0,toastOffset);
                noArtistToast.show();
            }
        }
    }
}
