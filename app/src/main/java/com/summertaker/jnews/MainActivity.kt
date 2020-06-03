package com.summertaker.jnews

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {

    //private val logTag = Config.logPrefix + this.javaClass.simpleName

    private val mContext: Context = this

    private var mSavedPosition = 0
    private val mSavedPositionKey = "playingPositionKey"

    private val permissionRequestCode = 1000

    private var mVideos: ArrayList<Video> = ArrayList()
    private var mArticles: ArrayList<Article> = ArrayList()

    private var mMediaController: MediaController? = null
    private var mPlayingCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://developer.android.com/guide/components/activities/activity-lifecycle?hl=ko
        if (savedInstanceState != null) {
            mSavedPosition = savedInstanceState.getInt(mSavedPositionKey, 0)
        }

        setContentView(R.layout.activity_main)
        checkPermissions()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        //Toast.makeText(this, "mSavedPosition: $mSavedPosition", Toast.LENGTH_SHORT).show()
        mSavedPosition = savedInstanceState.getInt(mSavedPositionKey, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //Toast.makeText(this, "mSavedPosition: $mSavedPosition", Toast.LENGTH_SHORT).show()
        outState.run {
            putInt(mSavedPositionKey, mSavedPosition)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shuffle -> {
                doShuffle()
                return true
            }
            R.id.download -> {
                goDownload()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun checkPermissions() {
        val permission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
        } else {
            initUI()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            permissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permissionRequestCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG)
                        .show()
                } else {
                    initUI()
                }
                return
            }
        }
    }

    private fun initUI() {
        mMediaController = MediaController(this)

        /*videoView.setMediaController(mMediaController)
        videoView.setOnPreparedListener {
            videoView.start()
            mMediaController!!.setAnchorView(videoView) // start() 할 때 지정해야 함
        }
        videoView.setOnCompletionListener {
            mPlayingCount++
            if (mPlayingCount >= mArticles.size) {
                mPlayingCount = 0
            }
            startPlay()
            videoView.resume()
        }
        //videoView.setOnErrorListener { _, _, _ ->
        //    Toast.makeText(this@MainActivity, "videoView.onError()", Toast.LENGTH_SHORT).show()
        //    false
        //}

        videoView.setOnClickListener {
            //val video = mVideos[mPlayingCount]
            //Toast.makeText(this@MainActivity, video.displayName, Toast.LENGTH_SHORT).show()
        }*/

        rewind.setOnClickListener(this)
        playPause.setOnClickListener(this)
        forward.setOnClickListener(this)

        registerBroadcast()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rewind -> // 이전 곡
                AudioApplication.getInstance().serviceInterface.rewind()
            R.id.playPause -> { // 재생 또는 일시 정지
                //Toast.makeText(this, "Play or Pause", Toast.LENGTH_SHORT).show()
                AudioApplication.getInstance().serviceInterface.togglePlay()
            }
            R.id.forward -> // 다음 곡
                AudioApplication.getInstance().serviceInterface.forward()
        }
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Toast.makeText(this@MainActivity, intent.action, Toast.LENGTH_SHORT).show()
            when (intent.action) {
                BroadcastActions.CREATED -> {
                    loadVideos()
                }
                BroadcastActions.PREPARED -> {
                    updateUI()
                }
                BroadcastActions.PLAY_STATE_CHANGED
                -> {
                    updateUI()
                }
            }
        }
    }

    private fun registerBroadcast() {
        //Toast.makeText(this, "registerBroadcast()", Toast.LENGTH_SHORT).show()
        val filter = IntentFilter()
        filter.addAction(BroadcastActions.CREATED)
        filter.addAction(BroadcastActions.PREPARED)
        filter.addAction(BroadcastActions.PLAY_STATE_CHANGED)
        registerReceiver(mBroadcastReceiver, filter)
    }

    private fun unregisterBroadcast() {
        unregisterReceiver(mBroadcastReceiver)
    }

    private fun loadVideos() {
        val dataManager = DataManager(this)
        val videos = dataManager.getVideoFiles()

        mVideos.clear()
        mVideos.addAll(videos)

        startPlay()
    }

    private fun startPlay() {
        if (mVideos.size > 0) {
            AudioApplication.getInstance().serviceInterface.setPlayList(mVideos) // 재생목록등록
            AudioApplication.getInstance().serviceInterface.play(0)
        }
    }

    private fun doShuffle() {
        if (mVideos.size > 1) {
            //videoView.stopPlayback()
            val seed = System.nanoTime()
            mVideos.shuffle(Random(seed))
            startPlay()
        }
    }

    private fun updateUI() {
        //val isPlaying = AudioApplication.getInstance().serviceInterface.isPlaying
        //Toast.makeText(this, "isPlaying: $isPlaying", Toast.LENGTH_SHORT).show()
        if (AudioApplication.getInstance().serviceInterface.isPlaying) {
            playPause.setImageResource(R.drawable.ic_pause)
        } else {
            playPause.setImageResource(R.drawable.ic_play)
        }

        mSavedPosition = AudioApplication.getInstance().serviceInterface.currentPosition
        if (mVideos.size > 0) {
            mSavedPosition += 1
        }
        val counter = mSavedPosition.toString() + " / " + mVideos.size
        track.text = counter

        val video: Video? = AudioApplication.getInstance().serviceInterface.playingItem
        if (video != null) {
            Glide.with(this).load(video.thumbnail).into(albumArt)
        } else {
            albumArt.setImageResource(R.drawable.placeholder)
        }
    }

    private fun goDownload() {
        //AudioApplication.getInstance().serviceInterface.stop()
        val intent = Intent(this, ArticlesActivity::class.java)
        startActivityForResult(intent, Config.activityRequestCodeArticles)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Config.activityRequestCodeArticles) {
            if (resultCode == Activity.RESULT_OK) {
                loadVideos()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }
}