package com.summertaker.jnews;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class AudioService extends Service {
    private final IBinder mBinder = new AudioServiceBinder();
    private MediaPlayer mMediaPlayer;
    private boolean isPrepared;
    private int mCurrentPosition = 0;
    private ArrayList<Video> mVideos = new ArrayList<>();
    //private NotificationPlayer mNotificationPlayer;

    public class AudioServiceBinder extends Binder {
        AudioService getService() {
            sendBroadcast(new Intent(BroadcastActions.CREATED)); // created 전송
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;
            mp.setLooping(true);
            mp.start();
            sendBroadcast(new Intent(BroadcastActions.PREPARED)); // prepared 전송
            //updateNotificationPlayer();
        });
        mMediaPlayer.setOnCompletionListener(mp -> {
            isPrepared = false;
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경 전송
            //updateNotificationPlayer();
        });
        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            isPrepared = false;
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경 전송
            //updateNotificationPlayer();
            return false;
        });
        mMediaPlayer.setOnSeekCompleteListener(mp -> {

        });

        //mNotificationPlayer = new NotificationPlayer(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /*private void queryAudioItem(int position) {
        mCurrentPosition = position;
        long audioId = mAudioIds.get(position);
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] selectionArgs = {String.valueOf(audioId)};
        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                //mAudioItem = AudioAdapter.AudioItem.bindCursor(cursor);
            }
            cursor.close();
        }
    }*/

    /*public void setPlayList(ArrayList<Long> audioIds) {
        if (!mAudioIds.equals(audioIds)) {
            mAudioIds.clear();
            mAudioIds.addAll(audioIds);
        }
    }*/

    public void setPlayList(ArrayList<Video> videos) {
        //if (!mVideos.equals(videos)) {
        mVideos.clear();
        mVideos.addAll(videos);
        //}
    }

    private void prepare() {
        try {
            //Log.e("TAG", ">> prepare()");
            //mMediaPlayer.setDataSource(mAudioItem.mDataPath);
            //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (mVideos.size() > 0) {
                Video video = mVideos.get(mCurrentPosition);
                if (video.getContentUri() != null) {
                    mMediaPlayer.setDataSource(getApplicationContext(), video.getContentUri());
                    mMediaPlayer.prepareAsync();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play(int position) {
        //queryAudioItem(position);
        mCurrentPosition = position;
        if (isPlaying()) {
            stop();
        }
        prepare();
    }

    public void play() {
        if (isPrepared) {
            mMediaPlayer.start();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경 전송
            //updateNotificationPlayer();
        }
    }

    public void pause() {
        if (isPrepared) {
            mMediaPlayer.pause();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // 재생상태 변경 전송
            //updateNotificationPlayer();
        }
    }

    public void stop() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }

    public void forward() {
        if (mVideos.size() > 1) {
            if (mVideos.size() - 1 > mCurrentPosition) {
                mCurrentPosition++; // 다음 포지션으로 이동.
            } else {
                mCurrentPosition = 0; // 처음 포지션으로 이동.
            }
            play(mCurrentPosition);
        }
    }

    public void rewind() {
        if (mVideos.size() > 1) {
            if (mCurrentPosition > 0) {
                mCurrentPosition--; // 이전 포지션으로 이동.
            } else {
                mCurrentPosition = mVideos.size() - 1; // 마지막 포지션으로 이동.
            }
            play(mCurrentPosition);
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public Video getPlayingItem() {
        if (mVideos.size() > 0) {
            return mVideos.get(mCurrentPosition);
        } else {
            return null;
        }
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    //private void updateNotificationPlayer() {
    //    if (mNotificationPlayer != null) {
    //        mNotificationPlayer.updateNotificationPlayer();
    //    }
    //}

    //private void removeNotificationPlayer() {
    //    if (mNotificationPlayer != null) {
    //        mNotificationPlayer.removeNotificationPlayer();
    //    }
    //}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (CommandActions.TOGGLE_PLAY.equals(action)) {
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            } else if (CommandActions.REWIND.equals(action)) {
                rewind();
            } else if (CommandActions.FORWARD.equals(action)) {
                forward();
            } else if (CommandActions.CLOSE.equals(action)) {
                pause();
                //removeNotificationPlayer();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}