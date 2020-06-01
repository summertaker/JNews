package com.summertaker.jnews

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.content_articles.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArticlesActivity : AppCompatActivity(), DataInterface, ArticlesInterface {

    val logTag = Config.logPrefix + this.javaClass.simpleName

    var mVideos: ArrayList<Video> = ArrayList()
    var mArticles: ArrayList<Article> = ArrayList()

    private var mAdapter: ArticlesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles)
        setSupportActionBar(findViewById(R.id.toolbar))

        val actionBar = supportActionBar
        if (actionBar != null) {
            //actionbar.title = "Title"
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        getLocalData()
    }

    override fun onSupportNavigateUp(): Boolean {
        //onBackPressed()
        finish()
        return true
    }

    private fun getLocalData() {
        val dataManager = DataManager(this@ArticlesActivity, this@ArticlesActivity)
        dataManager.getLocalData()

        /*
        val dir = File(getExternalFilesDir(null), getString(R.string.japanese))
        if (dir.isDirectory) {
            val fs = dir.listFiles()
            fs?.forEach {
                Log.d(logTag, ">> ${it.name}")
                val video = Video(it.toURI().toString(), it.path, it.name)
                mVideos.add(video)
            }
        } else {
            Log.e(logTag, getString(R.string.no_directory) + ": " + dir)
        }
        */

        /*
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Video.VideoColumns.DISPLAY_NAME
        )

        if (cursor == null) {
            Log.e(logTag, ">> cursor is null.")
        } else {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val relativePathColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val relativePath = cursor.getString(relativePathColumn)
                val ok = relativePath.contains(Config.localDownloadSubPath, ignoreCase = false)
                if (ok) {
                    val contentUri =
                        Uri.withAppendedPath(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )
                    //Log.e(logTag, ">> $displayName")
                    val video = Video(contentUri, relativePath, displayName)
                    mVideos.add(video)
                }
            }
            cursor.close()
        }
        */

        getRemoteData()
    }

    private fun isInternetConnected(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
            RetrofitFactory.getService().requestArticles(id = 0, title = "").enqueue(object :
                Callback<ArticleModel> {
                override fun onFailure(call: Call<ArticleModel>, t: Throwable) {
                    Log.e(logTag, t.message.toString())
                }

                override fun onResponse(
                    call: Call<ArticleModel>,
                    response: Response<ArticleModel>
                ) {
                    if (response.isSuccessful) {
                        //Log.e(TAG, response.body().toString())
                        val body = response.body()
                        body?.let {
                            for (article in it.articles) {
                                if (article.file.isNullOrEmpty()) {
                                    continue
                                }
                                val fileName =
                                    article.file.substring(article.file.lastIndexOf('/') + 1)
                                for (video in mVideos) {
                                    if (fileName == video.displayName) {
                                        article.storageFile = video.contentUri.toString()
                                        break
                                    }
                                }
                                mArticles.add(article)
                            }
                            //setAdapter(it.articles)
                            setAdapter(mArticles)
                        }
                    } else {
                        Log.e(logTag, getString(R.string.response_failed))
                    }
                }
            })
        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun setAdapter(articles: ArrayList<Article>) {
        mAdapter = ArticlesAdapter(this, articles)
        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onArticleSelected(article: Article) {
        //Toast.makeText(it.context, article.title, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DownloadActivity::class.java)
        intent.putExtra("id", article.id)
        intent.putExtra("yid", article.yid)
        intent.putExtra("title", article.title)
        intent.putExtra("file", article.file)
        startActivityForResult(intent, Config.requestCodeDownload)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Config.requestCodeDownload) {
            if (resultCode == Activity.RESULT_OK) {
                val id = data?.getStringExtra("id")
                val storageFile = data?.getStringExtra("storageFile")
                if (storageFile != null) {
                    mArticles.forEach {
                        if (it.id == id) {
                            it.storageFile = storageFile
                        }
                    }
                    mAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun getLocalDataCallback(videos: ArrayList<Video>) {
        mVideos = videos
    }
}