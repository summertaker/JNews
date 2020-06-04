package com.summertaker.jnews;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;

public class AudioServiceInterface {
    private ServiceConnection mServiceConnection;
    private AudioService mService;
    private Context mContext;

    public AudioServiceInterface(Context context) {
        mContext = context;

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((AudioService.AudioServiceBinder) service).getService();
                //Log.e(">>", "AudioServiceInterface.onServiceConnected()");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceConnection = null;
                mService = null;
                //Log.e(">>", "AudioServiceInterface.onServiceDisconnected()");
            }
        };
        context.bindService(new Intent(context, AudioService.class)
                .setPackage(context.getPackageName()), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //public void unbindService() {
    //    if (mServiceConnection != null) {
    //        mContext.unbindService(mServiceConnection);
    //        mServiceConnection = null;
    //        mService = null;
    //   }
    //}

    /*public void setPlayList(ArrayList<Long> audioIds) {
        if (mService != null) {
            mService.setPlayList(audioIds);
        }
    }*/

    public ArrayList<Video> getPlayList() {
        if (mService != null) {
            return mService.getPlayList();
        } else {
            return null;
        }
    }

    public void setPlayList(ArrayList<Video> videos) {
        if (mService != null) {
            mService.setPlayList(videos);
        }
    }

    public void play(int position) {
        if (mService != null) {
            mService.play(position);
        } else {
            Toast.makeText(mContext, "mService is null.", Toast.LENGTH_SHORT).show();
        }
    }

    //public void play() {
    //    if (mService != null) {
    //        mService.play();
    //    }
    //}

    //public void pause() {
    //    if (mService != null) {
    //        mService.pause();
    //    }
    //}

    public void stop() {
        if (mService != null) {
            mService.stop();
        }
    }

    public void forward() {
        if (mService != null) {
            mService.forward();
        }
    }

    public void rewind() {
        if (mService != null) {
            mService.rewind();
        }
    }

    public void togglePlay() {
        if (isPlaying()) {
            mService.pause();
        } else {
            if (mService != null) {
                mService.play();
            }
        }
    }

    public boolean isPlaying() {
        if (mService != null) {
            return mService.isPlaying();
        }
        return false;
    }

    public Video getPlayingItem() {
        if (mService != null) {
            return mService.getPlayingItem();
        }
        return null;
    }

    public int getCurrentPosition() {
        if (mService != null) {
            return mService.getCurrentPosition();
        } else {
            return 0;
        }
    }
}
