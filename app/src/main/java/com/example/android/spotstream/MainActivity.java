package com.example.android.spotstream;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
    implements SearchFragment.Callback,SongsFragment.Callback{

    boolean mTwoPane;
    private static final String SONGFRAGMENT_TAG = "SFTAG";
    private static final String PLAYER_FRAGMENT_TAG = "PFTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.songs_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent openSettingsIntent = new Intent(this, com.example.android.spotstream.SettingsActivity.class);
            startActivity(openSettingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected (String spotifyArtistId, String artistName) {
        if(mTwoPane) {
            SongsFragment songFrag = SongsFragment.newInstance(spotifyArtistId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.songs_container, songFrag, SONGFRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(this, SongsActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, spotifyArtistId);
            intent.putExtra(Intent.EXTRA_TITLE, artistName);

            startActivity(intent);
        }

    }

    @Override
    public void startPlayer(ArrayList<Song> songs, int position) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(PLAYER_FRAGMENT_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = PlayerFragment.newInstance(songs, position);
        newFragment.show(ft, PLAYER_FRAGMENT_TAG);


    }

}

