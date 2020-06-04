package com.summertaker.jnews

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {

    //private val mContext: Context = this

    private val permissionRequestCode = 1000

    private var mVideos: ArrayList<Video> = ArrayList()

    //private var mMediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        checkPermissions()
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

        val videos = AudioApplication.getInstance().serviceInterface.playList
        if (videos != null) {
            //Log.e(">>", "not null. " + AudioApplication.getInstance().serviceInterface.playList.size)
            mVideos.clear()
            mVideos.addAll(videos)
            updateUI()
        }
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rewind ->
                AudioApplication.getInstance().serviceInterface.rewind()
            R.id.playPause -> {
                //Toast.makeText(this, "Play or Pause", Toast.LENGTH_SHORT).show()
                AudioApplication.getInstance().serviceInterface.togglePlay()
            }
            R.id.forward ->
                AudioApplication.getInstance().serviceInterface.forward()
            R.id.download ->
                goDownload()
        }
    }

    private fun initUI() {
        //mMediaController = MediaController(this)

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
        download.setOnClickListener(this)

        registerBroadcast()
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Toast.makeText(this, intent.action, Toast.LENGTH_SHORT).show()
            when (intent.action) {
                BroadcastActions.CREATED -> {
                    //Toast.makeText(mContext, intent.action, Toast.LENGTH_SHORT).show()
                    loadVideos()
                }
                BroadcastActions.PREPARED -> {
                    //Log.e(logTag, ">> BroadcastActions.PREPARED")
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
        //Log.e(logTag, ">> unregisterBroadcast: $mBroadcastReceiver")
    }

    private fun loadVideos() {
        val dataManager = DataManager(this)
        val videos = dataManager.getVideoFiles()

        mVideos.clear()
        mVideos.addAll(videos)

        doShuffle()
        AudioApplication.getInstance().serviceInterface.playList = mVideos
        startPlay()
    }

    private fun startPlay() {
        AudioApplication.getInstance().serviceInterface.play(0)
    }

    private fun doShuffle() {
        if (mVideos.size > 1) {
            //videoView.stopPlayback()
            val seed = System.nanoTime()
            mVideos.shuffle(Random(seed))
            //startPlay()
        }
    }

    private fun updateUI() {
        val video: Video? = AudioApplication.getInstance().serviceInterface.playingItem
        if (video != null) {
            Glide.with(this).load(video.thumbnail).into(albumArt)
        }
        //albumArt.setImageResource(R.drawable.placeholder)

        var position = AudioApplication.getInstance().serviceInterface.currentPosition
        if (mVideos.size > 0) {
            position += 1
        }
        val trackText = position.toString() + "/" + mVideos.size
        track.text = trackText

        if (AudioApplication.getInstance().serviceInterface.isPlaying) {
            ivPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            ivPlayPause.setImageResource(R.drawable.ic_play)
        }

        if (video != null) {
            val html =
                video.style + video.furigana + "<hr>" + video.korean + "<hr>" + video.japanese
            webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "")
        }
    }

    private fun goDownload() {
        AudioApplication.getInstance().serviceInterface.stop()

        val intent = Intent(this, ArticlesActivity::class.java)
        startActivityForResult(intent, Config.activityRequestCodeArticles)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //if (requestCode == Config.activityRequestCodeArticles) {
        //    if (resultCode == Activity.RESULT_OK) {
        //        loadVideos()
        //    }
        //}
        loadVideos()
    }

    override fun onDestroy() {
        super.onDestroy()
        //AudioApplication.getInstance().serviceInterface.unbindService()
        //if (AudioApplication.getInstance().serviceInterface.isPlaying) {
        //    AudioApplication.getInstance().serviceInterface.stop()
        //}
        unregisterBroadcast()
    }
}
