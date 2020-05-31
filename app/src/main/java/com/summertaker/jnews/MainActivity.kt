package com.summertaker.jnews

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import android.net.ConnectivityManager as ConnectivityManager1

class MainActivity : AppCompatActivity() {

    val tAg = ">> MainActivity"

    private val recordRequestCode = 1000

    var videos: ArrayList<Video> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
    }

    private fun checkPermissions() {
        val permission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
        } else {
            getLocalData()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            recordRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            recordRequestCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
                } else {
                    getLocalData()
                }
                return
            }
        }
    }

    private fun getLocalData() {
        /*val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
        )*/
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Video.VideoColumns.DISPLAY_NAME
        )

        if (cursor != null) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val relativePathColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val relativePath = cursor.getString(relativePathColumn)
                val ok = relativePath.contains(getString(R.string.japanese), ignoreCase = false)
                if (ok) {
                        val contentUri =
                            Uri.withAppendedPath(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id.toString()
                            )
                        //Log.e(tAg, relativePath)
                        //Log.e(tAg, "- $contentUri")
                        //Log.e(tAg, "- $displayName")

                        val video = Video(id, contentUri.toString(), relativePath, displayName)
                        videos.add(video)
                    }
            }
            cursor.close()
        }

        getRemoteData()
    }

    private fun isInternetConnected(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager1
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                result = true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                result = true
            }
        }
        return result
    }

    private fun getRemoteData() {
        if (isInternetConnected(this)) {
            RetrofitFactory.getService().requestSearchImage(id = 0, title = "").enqueue(object :
                Callback<ArticleModel> {
                override fun onFailure(call: Call<ArticleModel>, t: Throwable) {
                    Log.e(tAg, t.message.toString())
                }

                override fun onResponse(call: Call<ArticleModel>, response: Response<ArticleModel>) {
                    if (response.isSuccessful) {
                        //Log.e(TAG, response.body().toString())
                        val body = response.body()
                        body?.let {
                            for (article in it.articles) {
                                val arr = article.file.split("/") // "upload/abc.mp4"
                                val file = arr[arr.size - 1]
                                for (video in videos) {
                                    if (file == video.displayName) {
                                        article.contentUri = video.contentUri
                                        break
                                    }
                                }
                            }
                            setAdapter(it.articles)
                        }
                    } else {
                        Log.e(tAg, getString(R.string.response_unsuccessful))
                    }
                }
            })
        } else {
            Toast.makeText(this, getString(R.string.no_inertnet_connection), Toast.LENGTH_LONG).show()
        }
    }

    private fun setAdapter(articles: ArrayList<Article>) {
        val adapter = RecyclerAdapter(articles, this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

}