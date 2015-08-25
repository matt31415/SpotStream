package com.example.android.spotstream;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class SongsActivity extends ActionBarActivity
    implements SongsFragment.Callback{
    private final String LOG_TAG = SongsActivity.class.getSimpleName();
    private static final String PLAYER_FRAGMENT_TAG = "PlayerFragment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the action bar subtitle as the artist's name (stored in EXTRA_TITLE)
        String artistName = getIntent().getStringExtra(Intent.EXTRA_TITLE);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setSubtitle(artistName);
        }
        setContentView(R.layout.activity_songs);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_songs, menu);
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
    public void startPlayer(ArrayList<Song> songs, int position) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DialogFragment playerFrag = PlayerFragment.newInstance(songs, position);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.add(android.R.id.content, playerFrag)
                .addToBackStack(null).commit();
    }
}
