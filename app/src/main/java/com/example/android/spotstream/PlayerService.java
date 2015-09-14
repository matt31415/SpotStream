package com.example.android.spotstream;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by mgay on 8/26/2015.
 *
 * This class was heavily influenced by:
 *   http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
 */
public class PlayerService extends Service
    implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{

    private final String LOG_TAG = PlayerService.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    private MediaPlayerState mMediaPlayerState;
    private Song mSong;

    private final IBinder mPlayerBind = new PlayerBinder();

    private enum MediaPlayerState {
        IDLE,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSED,
        PLAYBACK_COMPLETED,
        STOPPED,
        END
    }

    private enum Direction {
        NEXT,
        PREV
    }

    public PlayerService() {
        Log.d(LOG_TAG, "constructor");
    }

    private void createMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);

        mMediaPlayerState = MediaPlayerState.IDLE;
    }

    public Song getCurrentSong () {
        return mSong;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");

        return mPlayerBind;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind");
//        if(mMediaPlayerState == MediaPlayerState.PAUSED || mMediaPlayerState == MediaPlayerState.STARTED) {
//            mMediaPlayer.stop();
//        }
//        if(mMediaPlayer != null) {
//            mMediaPlayer.release();
//        }
        return false;
    }

    public void resetAndStartNewSong (Song song) {
        Log.d(LOG_TAG, "resetAndStartNewSong");

        mSong = song;
        if(mMediaPlayer != null && mMediaPlayerState != MediaPlayerState.IDLE) {
            Log.d(LOG_TAG, "Resetting MediaPlayer");
            mMediaPlayer.reset();
            mMediaPlayerState = MediaPlayerState.IDLE;
        }
        else {
            createMediaPlayer();
        }

        try {
            mMediaPlayer.setDataSource(song.mPreviewUrl);
            mMediaPlayer.prepareAsync();
            mMediaPlayerState = MediaPlayerState.PREPARING;
        } catch (IOException e) {
            Log.d(LOG_TAG, "Unable to find preview");
        }
    }

    public void play() {
        Log.d(LOG_TAG, "Play");
        if (mMediaPlayerState == MediaPlayerState.PAUSED || mMediaPlayerState == MediaPlayerState.PLAYBACK_COMPLETED) {
            Log.d(LOG_TAG, "Starting from paused state");
            mMediaPlayer.start();
            mMediaPlayerState = MediaPlayerState.STARTED;
        }
        else if (mMediaPlayerState == MediaPlayerState.STARTED) {
            Log.e(LOG_TAG, "Attemped to call play when media player was already playing");
        }
        else {
            Log.e(LOG_TAG, "Attempted to call play when media player wasn't ready yet");
        }
    }

    public void pause() {
        if(mMediaPlayerState == MediaPlayerState.STARTED) {
            mMediaPlayer.pause();
            mMediaPlayerState = MediaPlayerState.PAUSED;
        }
        else if (mMediaPlayerState == MediaPlayerState.PAUSED) {
            Log.e(LOG_TAG, "Attemped to call pause when media player was already playing");
        }
        else {
            Log.e(LOG_TAG, "Attempted to call pause when media player wasn't ready yet");
        }
    }

    public boolean canReset() {
        return mMediaPlayerState != MediaPlayerState.PREPARING;
    }

    public boolean isPaused() {
        return (mMediaPlayerState == MediaPlayerState.PLAYBACK_COMPLETED ||
                mMediaPlayerState == MediaPlayerState.PAUSED);
    }

    public int getCurrentPosition() {
        if(mMediaPlayer != null && (mMediaPlayerState == MediaPlayerState.STARTED || mMediaPlayerState == MediaPlayerState.PAUSED)) {
            return mMediaPlayer.getCurrentPosition();
        }
        else {
            return 0;
        }
    }

    public int getTrackLength() {
        if(mMediaPlayer != null && (mMediaPlayerState == MediaPlayerState.STARTED || mMediaPlayerState == MediaPlayerState.PAUSED)) {
            return mMediaPlayer.getDuration();
        }
        else {
            return 0;
        }
    }

    public void seekTo(int newPosition) {
        if(mMediaPlayer != null && (mMediaPlayerState == MediaPlayerState.STARTED || mMediaPlayerState == MediaPlayerState.PAUSED)) {
            mMediaPlayer.seekTo(newPosition);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mMediaPlayer != null) {

            Log.d(LOG_TAG, "OnCompletion");

            mMediaPlayerState = MediaPlayerState.PLAYBACK_COMPLETED;

//            mMediaPlayer.stop();
//            mMediaPlayerState = MediaPlayerState.STOPPED;
//            mMediaPlayer.release();
//            mMediaPlayerState = MediaPlayerState.END;
//            mMediaPlayer = null;
//            mMediaPlayerState = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayerState = MediaPlayerState.PREPARED;
        mp.start();
        mMediaPlayerState = MediaPlayerState.STARTED;
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            Log.d(LOG_TAG, "PlayerService::getService");

            return PlayerService.this;
        }
    }

}
