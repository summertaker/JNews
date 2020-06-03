package com.summertaker.jnews

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.content_download.*

class DownloadActivity : AppCompatActivity() {

    private val logTag = Config.logPrefix + this.javaClass.simpleName

    private var mArticleId: String? = null
    private var mArticleTitle: String? = null
    private var mArticleFile: String? = null
    private var mDisplayName: String? = null

    private var downloadId: Long = -1L
    private lateinit var downloadManager: DownloadManager

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                if (downloadId == id) {
                    val query: DownloadManager.Query = DownloadManager.Query()
                    query.setFilterById(id)
                    val cursor = downloadManager.query(query)
                    if (!cursor.moveToFirst()) {
                        return
                    }

                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(columnIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        //Toast.makeText(context, "Download succeeded", Toast.LENGTH_SHORT).show()
                        doFinish()
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        Toast.makeText(
                            context,
                            getString(R.string.download_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED == intent.action) {
                Toast.makeText(context, "Notification clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        setSupportActionBar(findViewById(R.id.toolbar))

        val actionBar = supportActionBar
        if (actionBar != null) {
            //actionbar.title = "Title"
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val intent = intent
        mArticleId = intent.getStringExtra("id")
        mArticleTitle = intent.getStringExtra("title")
        mArticleFile = intent.getStringExtra("file")
        //Toast.makeText(this, mFile, Toast.LENGTH_SHORT).show()

        tvDownloadTitle.text = mArticleTitle

        if (mArticleFile != null) {
            downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val intentFilter = IntentFilter()
            intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
            registerReceiver(onDownloadComplete, intentFilter)

            downloadBtn.setOnClickListener {
                doDownload()
            }

            statusBtn.setOnClickListener {
                val status = getStatus(downloadId)
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
            }

            cancelBtn.setOnClickListener {
                if (downloadId != -1L) {
                    downloadManager.remove(downloadId)
                }
            }

            doDownload()
        } else {
            Toast.makeText(this, "mArticleFile is null.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //onBackPressed()
        doFinish()
        return true
    }

    /*
    private fun getPrivateAlbumStorageDir(): File? {
        // Get the directory for the app's private pictures directory.
        val url = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val file = File(getExternalFilesDir(url.toString()), Config.localDownloadSubPath)
        // val file = File(url.toString(), Config.localDownloadSubPath)
        Log.e(logTag, ">> file.path: " + file.path)

        if (!file.isDirectory) {
            if (!file.mkdirs()) {
                Log.e(logTag, "Directory not created")
            } else {
                Log.e(logTag, "mkdirs")
            }
        } else {
            Log.e(logTag, "is Directory")
        }
        return file
    }
    */

    /*
    https://codechacha.com/ko/android-downloadmanager/
     */
    private fun doDownload() {
        //if (mArticleFile != null) {
        /*
        val localPath = File(
            getExternalFilesDir(null),
            Config.localDownloadSubPath
        ) // /android/data/com.summertaker.jnews/files/日本語
        //val downloadPath = File(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString(), getString(R.string.japanese)) // mkdirs 실패한다.
        if (!localPath.isDirectory) {
            if (!localPath.mkdirs()) {
                Log.e(logTag, ">> Directory not created. $localPath")
            }
        }
         */

        //val url = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        //val localPath = File(url.toString(), Config.localDownloadSubPath)
        //Log.e(logTag, ">> localPath: $localPath")

        //val localPath = getPrivateAlbumStorageDir()
        //if (localPath.isDirectory) {
        //val arr = mArticleFile.split("/")

        mDisplayName = mArticleFile?.substring(mArticleFile!!.lastIndexOf('/') + 1)
        val destinationName = Config.localDownloadSubPath + "/" + mDisplayName

        //val localFile = File(Environment.DIRECTORY_MOVIES, mStorageFile.toString()) // 미완성
        //if (localFile.exists()) localFile.delete()

        //val encodedFileName: String = URLEncoder.encode(fileName, "utf-8")
        val downloadUrl = Config.remoteBaseUrl + mArticleFile
        //Log.d(tAg, ">> downloadPath: $downloadPath")
        //Log.d(tAg, ">> downloadFile: $downloadFile")
        //Log.d(tAg, ">> downloadUrl: $downloadUrl")

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(mArticleTitle)
            .setDescription(mDisplayName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            //.setDestinationUri(Uri.fromFile(mStorageFile))
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, destinationName)
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        downloadId = downloadManager.enqueue(request)
        //} else {
        //    Log.e(logTag, ">> Directory not exists. $localPath")
        //}
        //}
    }

    private fun getStatus(id: Long): String {
        val query: DownloadManager.Query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        if (!cursor.moveToFirst()) {
            Log.e(logTag, "Empty row")
            return "Wrong downloadId"
        }

        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(columnIndex)
        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason = cursor.getInt(columnReason)
        val statusText: String

        statusText = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Successful"
            DownloadManager.STATUS_FAILED -> {
                "Failed: $reason"
            }
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Running"
            DownloadManager.STATUS_PAUSED -> {
                "Paused: $reason"
            }
            else -> "Unknown"
        }

        return statusText
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }

    private fun doFinish() {
        //Toast.makeText(this, "onDestroy().mStorageFile: $mStorageFile", Toast.LENGTH_SHORT).show()
        /*
         * onDestory()에 아래 코드를 넣으면 putExtra() 값과 Activity.RESULT_OK 값을 이전 Activity에서 가져오지 못 한다.
         */
        val intent = Intent(this, DownloadActivity::class.java)
        intent.putExtra("id", mArticleId)
        intent.putExtra("displayName", mDisplayName)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}