package com.example.android.spotstream;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {
    private final String LOG_TAG = SearchFragment.class.getSimpleName();

    private ArtistArrayAdapter mArtistAdapter;
    private Callback mCallback;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

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

                mCallback.onItemSelected(artist.mSpotifyId, artist.mName);
            }
        });

        //We may already have the list of artists
        if(savedInstanceState != null) {
            if(savedInstanceState.getSerializable(getString(R.string.main_activity_saved_artists_key)) != null) {
                ArrayList<Artist> savedArtists = (ArrayList<Artist>) savedInstanceState.getSerializable(getString(R.string.main_activity_saved_artists_key));
                mArtistAdapter.addAll(savedArtists);
            }
        }


        // Set up a handler for the search string
        ((EditText)view.findViewById(R.id.edit_text_artist_search)).setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            new FetchArtistsTask().execute(v.getText().toString());

                            // The EditText was really inconsistent about closing the keyboard,
                            // so we're going to do it manually
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                            return false;
                        }
                        return false;
                    }
                }
        );

        return view;
    }

    /**
     * When we need to save the instance state, we'll store the list of artists.  Fortunately the list
     * is trivial to serialize!
     * @param state State bundle into which the state should be written
     */
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        //Convert list of artists to an arrayList
        ArrayList<Artist> artists = new ArrayList<>();
        for (int artistIdx = 0; artistIdx < mArtistAdapter.getCount(); artistIdx++) {
            artists.add(mArtistAdapter.getItem(artistIdx));
        }

        //save songs in bundle
        state.putSerializable(getString(R.string.main_activity_saved_artists_key), artists);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * SearchFragment Callback for when an item has been selected.
         */
        public void onItemSelected(String spotifyArtistId, String artistName);
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

    private class FetchArtistsTask extends AsyncTask<String, Void, Artist[]> {
        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected Artist[] doInBackground (String... searchStr) {
            ArrayList<Artist> artists = new ArrayList<>();

            //This is where we conduct our search for the artists using the Spotify API
            SpotifyApi api = new SpotifyApi();
            ArtistsPager artistsPager = new ArtistsPager();

            try {
                SpotifyService spotify = api.getService();
                artistsPager = spotify.searchArtists(searchStr[0]);
            }
            catch(RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.e(LOG_TAG, "Spotify API Error: " + spotifyError.toString());
            }

            if(artistsPager.artists != null) {
                for (kaaes.spotify.webapi.android.models.Artist a : artistsPager.artists.items) {
                    String artistName = a.name;
                    String artistSpotifyId = a.id;
                    String artistImageURL;
                    if (!a.images.isEmpty()) {
                        artistImageURL = a.images.get(0).url;
                    } else {
                        artistImageURL = null;
                    }

                    artists.add(new Artist(artistName, artistImageURL, artistSpotifyId));
                }
            }
            //noinspection RedundantCast,RedundantCast
            return artists.toArray(new Artist[artists.size()]);
        }

        /**
         * After the task has completed, we'll want to update the artist adapter with the new search results.
         * @param artists - List of artists returned
         */
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
