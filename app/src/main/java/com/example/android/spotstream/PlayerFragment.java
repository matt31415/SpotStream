package com.example.android.spotstream;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {
    private final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private ArrayList<Song> mSongList;
    private int mCurrSongPosition;

    private PlayerService mPlayerService;
    private Intent mPlayerIntent;
    private ServiceConnection mPlayerConnection;

    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private ImageButton mPlayPauseButton;

    private TextView mArtistText;
    private TextView mAlbumText;
    private TextView mSongText;
    private ImageView mSongImageView;

    Timer mSeekTimer;
    private SeekBar mSeekBar;

    private TextView mSeekEndTimeText;
    private TextView mSeekCurrTimeText;

    // The PlayPauseState is the current icon displayed on the play/pause button
    private PlayPauseState mPlayPauseState;
    private enum PlayPauseState {
        PLAY,
        PAUSE,
    }

    private static final String SONGS_LIST_KEY = "SongsList";
    private static final String POSITION_KEY = "Position";


    private enum Direction {
        NEXT,
        PREV
    }

    public PlayerFragment() {
        mPlayerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
                mPlayerService = binder.getService();

                Song serviceSong = mPlayerService.getCurrentSong();
                Song currSong = mSongList.get(mCurrSongPosition);
                if(serviceSong == null || !serviceSong.mPreviewUrl.matches(currSong.mPreviewUrl)) {
                    mPlayerService.resetAndStartNewSong(currSong);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }


    /**
     * Returns a new copy of a PlayerFragment
     * @param songs ArrayList of song items for the songs to be included in the player
     * @param position the index of the song that should be displayed as the current song.
     * @return
     */
    public static PlayerFragment newInstance(ArrayList<Song> songs, int position) {
        PlayerFragment playerFrag = new PlayerFragment();

        Bundle args = new Bundle();
        args.putSerializable(SONGS_LIST_KEY, songs);
        args.putInt(POSITION_KEY, position);

        playerFrag.setArguments(args);

        playerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);

        return playerFrag;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_player, container, false);

        Bundle fragArgs = getArguments();
        if(fragArgs != null) {
            mSongList = (ArrayList<Song>) fragArgs.getSerializable(SONGS_LIST_KEY);
            mCurrSongPosition = fragArgs.getInt(POSITION_KEY);
        }
        else {
            mSongList = (ArrayList<Song>) getActivity().getIntent().getSerializableExtra(getString(R.string.player_songs_key));
            mCurrSongPosition = getActivity().getIntent().getIntExtra(getString(R.string.player_song_position_key), 0);
        }

        if(savedInstanceState != null) {
            mCurrSongPosition = savedInstanceState.getInt(getString(R.string.player_fragment_curr_pos_key));
        }

        mPlayPauseButton = (ImageButton) view.findViewById(R.id.player_play_pause_button);

        mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_pause));
        mPlayPauseState = PlayPauseState.PAUSE;

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPlayPauseState == PlayPauseState.PLAY) {
                    mPlayPauseState = PlayPauseState.PAUSE;
                    mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_pause));
                    mPlayerService.play();
                }
                else if(mPlayPauseState == PlayPauseState.PAUSE) {
                    mPlayPauseState = PlayPauseState.PLAY;
                    mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_play));
                    mPlayerService.pause();
                }
            }
        });

        mNextButton = (ImageButton) view.findViewById(R.id.player_next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSong(v, Direction.NEXT);
            }
        });

        if(mCurrSongPosition == mSongList.size() - 1) {
            mNextButton.setEnabled(false);
            mNextButton.setImageAlpha(50);
        }


        mPrevButton = (ImageButton) view.findViewById(R.id.player_prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSong(v, Direction.PREV);
            }
        });
        if(mCurrSongPosition == 0) {
            mPrevButton.setEnabled(false);
            mPrevButton.setImageAlpha(50);
        }

        mArtistText = (TextView) view.findViewById(R.id.player_artist_text);
        mAlbumText = (TextView) view.findViewById(R.id.player_album_text);
        mSongText = (TextView) view.findViewById(R.id.player_song_text);

        mSongImageView = (ImageView) view.findViewById(R.id.player_album_image);

        mSeekEndTimeText = (TextView) view.findViewById(R.id.player_seek_end);
        mSeekCurrTimeText = (TextView) view.findViewById(R.id.player_seek_curr);
        mSeekBar = (SeekBar) view.findViewById(R.id.player_seek_bar);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser && mPlayerService != null) {
                    mPlayerService.seekTo(mSeekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Inspired by: http://stackoverflow.com/questions/10848960/run-thread-periodically
        // We'll poll the service periodically to figure out the surrent seek bat position.  We're
        // also going to use this time to update things that would change on the seek bar when we
        // move to a new song.  (This is maybe a bit wasteful, but I don't want tom implement messaging
        // back from the service)
        mSeekTimer = new Timer();
        mSeekTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mPlayerService == null) {
                    return;
                }

                int currentPosition = mPlayerService.getCurrentPosition();
                int trackLength = mPlayerService.getTrackLength();

                // Update progress
                mSeekBar.setProgress(currentPosition);

                //Set up the seek bar and its labels
                mSeekBar.setMax(trackLength);

                //Set the end time
                long endTimeSecs = TimeUnit.MILLISECONDS.toSeconds(trackLength);
                final String endTimeStr = String.format("%d:%02d", endTimeSecs / 60, endTimeSecs % 60);

                // Set the current time
                long currTimeSecs = TimeUnit.MILLISECONDS.toSeconds(currentPosition);
                final String currTimeStr = String.format("%d:%02d", currTimeSecs / 60, currTimeSecs % 60);

                // Do we need to change the state of the play/pause button?
                final boolean isPaused = mPlayerService.isPaused();

                Activity parentActivity = getActivity();

                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSeekEndTimeText.setText(endTimeStr);
                        mSeekCurrTimeText.setText(currTimeStr);

                        // Modify the play/pause button if it doesn't match the state of the player service
                        if(isPaused && mPlayPauseState != PlayPauseState.PLAY) {
                            mPlayPauseState = PlayPauseState.PLAY;
                            mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_play));
                        }
                        else if( !isPaused && mPlayPauseState != PlayPauseState.PAUSE) {
                            mPlayPauseState = PlayPauseState.PAUSE;
                            mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_pause));
                        }

                    }
                });
            }
        }, 0, 100);

        if(mPlayerIntent == null) {
            mPlayerIntent = new Intent(getActivity(), PlayerService.class);
            getActivity().bindService(mPlayerIntent, mPlayerConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mPlayerIntent);
        }

        recreateView(view);

        return view;
    }

    /**
     * Redraws (or draws for the first time) the view based on the value of mCurrSongPosition
     */
    private void recreateView() {
        recreateView(getView());
    }

    /**
     * Redraws (or draws for the first time) the view based on the value of mCurrSongPosition
     *
     * @param view
     */
    private void recreateView(View view) {
        Song song = mSongList.get(mCurrSongPosition);
        mArtistText.setText(song.mArtist);
        mAlbumText.setText(song.mAlbum);
        mSongText.setText(song.mTitle);

        if (song.mImageUrl != null) {
            Picasso.with(view.getContext())
                    .load(song.mImageUrl)
                    .fit()
                    .centerCrop()
                    .into(mSongImageView);
        }
        else {
            Picasso.with(view.getContext())
                    .load(R.drawable.note)
                    .fit()
                    .centerCrop()
                    .into(mSongImageView);
        }

        return;
    }

    private void changeSong(View view, Direction direction) {
        // If we already in the middle of resetting the player, we aren't allowed to change songs, so
        // return without doing anything.
        if(!mPlayerService.canReset()) {
            return;
        }

        // Update the current song
        if (direction == Direction.NEXT) {
            mCurrSongPosition++;
        }
        else if (direction == Direction.PREV) {
            mCurrSongPosition--;
        }

        // Redraw
        recreateView();

        // Start playing
        mPlayerService.resetAndStartNewSong(mSongList.get(mCurrSongPosition));

        // Since next and prev result in autoplaying, we should update the play/pause button accordingly.
        mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_pause));
        mPlayPauseState = PlayPauseState.PAUSE;

        // Can we use the next button?
        if(mCurrSongPosition < mSongList.size() - 1) {
            mNextButton.setEnabled(true);
            mNextButton.setImageAlpha(255);
        }
        else {
            mNextButton.setEnabled(false);
            mNextButton.setImageAlpha(50);
        }

        // Can we use the previous button?
        if(mCurrSongPosition > 0) {
            mPrevButton.setEnabled(true);
            mPrevButton.setImageAlpha(255);
        }
        else {
            mPrevButton.setEnabled(false);
            mPrevButton.setImageAlpha(50);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save current song position
        state.putInt(mPlayerService.getString(R.string.player_fragment_curr_pos_key), mCurrSongPosition);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onStop() {
        super.onStop();

        mSeekTimer.cancel();
        getActivity().unbindService(mPlayerConnection);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
