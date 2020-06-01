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
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener, DataInterface {

    private val logTag = Config.logPrefix + this.javaClass.simpleName

    private val permissionRequestCode = 1000

    private var mMediaController: MediaController? = null
    private var mPlayingCount = 0

    var mVideos: ArrayList<Video> = ArrayList()
    var mArticles: ArrayList<Article> = ArrayList()

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
        videoView.setMediaController(mMediaController)
        videoView.setOnPreparedListener {
            videoView.start()
            mMediaController!!.setAnchorView(videoView) // start() 할 때 지정해야 함
        }
        videoView.setOnCompletionListener {
            mPlayingCount++;
            if (mPlayingCount >= mArticles.size) {
                mPlayingCount = 0;
            }
            startPlay()
            videoView.resume();
        }
        videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this@MainActivity, "videoView.onError()", Toast.LENGTH_SHORT).show()
            false
        }

        videoView.setOnClickListener(View.OnClickListener {
            val video = mVideos[mPlayingCount]
            Toast.makeText(this@MainActivity, video.displayName, Toast.LENGTH_SHORT).show()
            //val uri = Uri.parse(video.getMediaUri())
            AudioApplication.getInstance().serviceInterface.play(video.contentUri)
        })

        loMiniPlayer.setOnClickListener(this)
        btPlayPause.setOnClickListener(this)
        btForward.setOnClickListener(this)
        btRewind.setOnClickListener(this)

        registerBroadcast()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.loMiniPlayer -> {
            }
            R.id.btRewind -> // 이전 곡
                AudioApplication.getInstance().serviceInterface.rewind()
            R.id.btPlayPause -> { // 재생 또는 일시 정지
                //Toast.makeText(this, "Play or Pause", Toast.LENGTH_SHORT).show()
                AudioApplication.getInstance().serviceInterface.togglePlay()
            }
            R.id.btForward -> // 다음 곡
                AudioApplication.getInstance().serviceInterface.forward()
        }
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Toast.makeText(this@MainActivity, intent.action, Toast.LENGTH_SHORT).show()
            when (intent.action) {
                BroadcastActions.CREATED -> {
                    getLocalData()
                }
                BroadcastActions.PREPARED -> {
                    tvTitle.text = mVideos[mPlayingCount].displayName
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

    private fun startPlay() {
        //AudioApplication.getInstance().getServiceInterface().setPlayList(getAudioIds()); // 재생목록등록

        //Article article =  mArticles.get(mPlayingCount);
        //Toast.makeText(MainActivity.this, article.getTitleKo(), Toast.LENGTH_SHORT).show();
        //String mediaUri = article.getMediaUri();
        val video: Video = mVideos[mPlayingCount]
        Toast.makeText(this, video.displayName, Toast.LENGTH_SHORT).show()

        //val uri = Uri.parse(video.contentUri)
        AudioApplication.getInstance().serviceInterface.play(video.contentUri)
    }

    private fun doShuffle() {
        if (mArticles.size > 1) {
            videoView.stopPlayback()
            val seed = System.nanoTime()
            mArticles.shuffle(Random(seed))
            startPlay()
        }
    }

    private fun updateUI() {
        if (AudioApplication.getInstance().serviceInterface.isPlaying) {
            btPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            btPlayPause.setImageResource(R.drawable.ic_play_arrow)
        }

        //AudioAdapter.AudioItem audioItem = AudioApplication.getInstance().getServiceInterface().getAudioItem();
        //if (audioItem != null) {
        //    Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), audioItem.mAlbumId);
        //    Picasso.with(getApplicationContext()).load(albumArtUri).error(R.drawable.empty_albumart).into(mImgAlbumArt);
        //    mTxtTitle.setText(audioItem.mTitle);
        //} else {
        //    mImgAlbumArt.setImageResource(R.drawable.empty_albumart);
        //    mTxtTitle.setText("재생중인 음악이 없습니다.");
        //}
    }

    private fun getLocalData() {
        val dataManager = DataManager(this@MainActivity, this@MainActivity)
        dataManager.getLocalData()
    }

    override fun getLocalDataCallback(videos: ArrayList<Video>) {
        //Toast.makeText(this, "videos.size = " + videos.size, Toast.LENGTH_SHORT).show();
        mVideos = videos
        startPlay()
    }

    private fun goDownload() {
        val intent = Intent(this, ArticlesActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        //Toast.makeText(MainActivity.this, "onDestroy()", Toast.LENGTH_SHORT).show();
        unregisterBroadcast()
    }
}