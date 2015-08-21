package com.example.android.spotstream;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends Fragment {

    private MediaPlayer mMediaPlayer;
    private MediaPlayerState mMediaPlayerState;
    private ArrayList<Song> mSongList;
    private int mCurrSongPosition;

    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private ImageButton mPlayPauseButton;

    private SeekBar mSeekBar;

    private enum MediaPlayerState {
        IDLE,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED
    }

    private enum Direction {
        NEXT,
        PREV
    }

    public PlayerFragment() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayerState = MediaPlayerState.IDLE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_player, container, false);

        //TODO: Make this work after rotation

        mSongList = (ArrayList<Song>) getActivity().getIntent().getSerializableExtra(getString(R.string.player_songs_key));
        mCurrSongPosition = getActivity().getIntent().getIntExtra(getString(R.string.player_song_position_key), 0);

        recreateView(view);

        mPlayPauseButton = (ImageButton) view.findViewById(R.id.player_play_pause_button);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayerState == MediaPlayerState.IDLE) {
                    PrepareAndStartPlayer();
                } else if (mMediaPlayerState == MediaPlayerState.STARTED) {
                    mMediaPlayer.pause();
                    mMediaPlayerState = MediaPlayerState.PAUSED;
                    mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_play));
                } else if (mMediaPlayerState == MediaPlayerState.PAUSED) {
                    mMediaPlayer.start();
                    mMediaPlayerState = MediaPlayerState.STARTED;
                    mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_pause));
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

        mPrevButton = (ImageButton) view.findViewById(R.id.player_prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSong(v, Direction.PREV);
            }
        });

        mSeekBar = (SeekBar) view.findViewById(R.id.player_seek_bar);

        Timer scrubTimer = new Timer();
        scrubTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayerState == MediaPlayerState.STARTED) {
                    mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                }
            }
        }, 0, 1000);
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
        TextView artistText = (TextView) view.findViewById(R.id.player_artist_text);
        artistText.setText(song.mArtist);

        TextView albumText = (TextView) view.findViewById(R.id.player_album_text);
        albumText.setText(song.mAlbum);

        TextView songText = (TextView) view.findViewById(R.id.player_song_text);
        songText.setText(song.mTitle);

        ImageView songImageView = (ImageView) view.findViewById(R.id.player_album_image);

        if (song.mImageUrl != null) {
            Picasso.with(view.getContext())
                    .load(song.mImageUrl)
                    .fit()
                    .centerCrop()
                    .into(songImageView);
        }
        else {
            Picasso.with(view.getContext())
                    .load(R.drawable.note)
                    .fit()
                    .centerCrop()
                    .into(songImageView);
        }

        return;
    }

    private void changeSong(View view, Direction direction) {
        if (direction == Direction.NEXT)
            mCurrSongPosition++;
        else if (direction == Direction.PREV) {
            mCurrSongPosition--;
        }

        recreateView();

        // Enable and disable the next/previous buttons as appropriate
        if(mCurrSongPosition == mSongList.size() - 1) {
            mNextButton.setEnabled(false);
            mNextButton.setImageAlpha(50);
        }
        else if(mCurrSongPosition == mSongList.size() - 2) {
            mNextButton.setEnabled(true);
            mNextButton.setImageAlpha(255);
        }
        else if(mCurrSongPosition == 0) {
            mPrevButton.setEnabled(false);
            mPrevButton.setImageAlpha(50);
        }
        else if(mCurrSongPosition == 1) {
            mPrevButton.setEnabled(true);
            mPrevButton.setImageAlpha(255);
        }

        if (mMediaPlayerState == MediaPlayerState.IDLE || mMediaPlayerState == MediaPlayerState.PAUSED) {
            mMediaPlayer.reset();
            mMediaPlayerState = MediaPlayerState.IDLE;
        } else if (mMediaPlayerState == MediaPlayerState.STARTED) {
            mMediaPlayer.reset();
            mMediaPlayerState = MediaPlayerState.IDLE;
            PrepareAndStartPlayer();
        }

    }

    private void PrepareAndStartPlayer() {
        // This code was heavily influenced by:
        // http://stackoverflow.com/questions/23309857/android-correct-usage-of-prepareasync-in-media-player-activity
        try {
            mMediaPlayer.setDataSource(mSongList.get(mCurrSongPosition).mPreviewUrl);
            mMediaPlayer.prepareAsync();
            mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_pause));
        } catch (IOException e) {
            Toast.makeText(getView().getContext(), "Track preview not found", Toast.LENGTH_SHORT).show();
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayerState = MediaPlayerState.PREPARED;
                mp.start();
                mMediaPlayerState = MediaPlayerState.STARTED;
                mPlayPauseButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_pause));
                mSeekBar.setMax(mMediaPlayer.getDuration());
                mSeekBar.setProgress(0);

            }
        });
    }

}
