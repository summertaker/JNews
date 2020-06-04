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
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.content_articles.*
import org.json.JSONObject

class ArticlesActivity : AppCompatActivity(), DataInterface, ArticlesInterface {

    private val logTag = Config.logPrefix + this.javaClass.simpleName

    private var mArticles: ArrayList<Article> = ArrayList()

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

        //mAdapter = ArticlesAdapter(this, mArticles)
        recyclerView.adapter = ArticlesAdapter(this, mArticles)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        if (isInternetConnected(this)) {
            getRemoteData()
        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    //override fun onBackPressed() {
    //    super.onBackPressed()
    //    finish()
    //}

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
        val stringRequest = StringRequest(Request.Method.GET, Config.remoteDataUrl,
            Response.Listener { response ->
                //Log.e(logTag, ">> Response.Listener: $response")
                val dataManager = DataManager(this)
                val success = dataManager.saveFile(response)
                if (success) {
                    val videos = dataManager.getVideoFiles()
                    val jsonObject = JSONObject(response)
                    val jsonArray = jsonObject.getJSONArray("articles")
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val file = obj.getString("file")
                        if (file.isNullOrEmpty()) {
                            continue
                        }
                        //Log.e(logTag, file) // upload/I6PxDCRxJEQ.mp4

                        val fileName = file.substring(file.lastIndexOf('/') + 1)
                        var displayName: String? = null
                        for (video in videos) {
                            if (fileName == video.displayName) {
                                displayName = video.displayName.toString()
                                break
                            }
                        }

                        val article = Article(
                            obj.getString("id"),
                            obj.getString("yid"),
                            obj.getString("title"),
                            file,
                            displayName
                        )
                        mArticles.add(article)
                    }
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            },
            Response.ErrorListener { error ->
                Log.e(logTag, error.toString())
            })
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
    }

    /*private fun getRemoteData() {
        if (isInternetConnected(this)) {
            RetrofitFactory.getService().requestArticles(id = 0, title = "").enqueue(object :
                Callback<ArticleModel> {
                override fun onResponse(
                    call: Call<ArticleModel>,
                    response: Response<ArticleModel>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.let {
                            //val success = saveFile(it.articles.toString())
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
                        Toast.makeText(mContext, "서버 응답 실패", Toast.LENGTH_SHORT).show()
                        Log.e(logTag, getString(R.string.response_failed))
                    }
                }

                override fun onFailure(call: Call<ArticleModel>, t: Throwable) {
                    Toast.makeText(mContext, "서버 요청 실패: " + t.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                    Log.e(logTag, t.message.toString())
                }
            })
        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG)
                .show()
        }
    }*/

    override fun getLocalDataCallback(videos: ArrayList<Video>) {
        //mArticles.clear()
        //mArticles.addAll(article)
    }

    private fun setAdapter() {
        //mAdapter = ArticlesAdapter(this, mArticles)
        //recyclerView.adapter = mAdapter
        //recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onArticleSelected(article: Article) {
        //Toast.makeText(it.context, article.title, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DownloadActivity::class.java)
        intent.putExtra("id", article.id)
        intent.putExtra("title", article.title)
        intent.putExtra("file", article.file)
        startActivityForResult(intent, Config.activityRequestCodeDownload)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Config.activityRequestCodeDownload) {
            if (resultCode == Activity.RESULT_OK) {
                val id = data?.getStringExtra("id")
                val displayName = data?.getStringExtra("displayName")
                //Toast.makeText(this, "displayName: $displayName", Toast.LENGTH_SHORT).show()

                if (displayName != null) {
                    for (article in mArticles) {
                        if (article.id == id) {
                            article.displayName = displayName
                            break
                        }
                    }
                    setResult(Activity.RESULT_OK)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    /*private fun doFinish() {
        Toast.makeText(this, "mDataChanged: $mDataChanged", Toast.LENGTH_SHORT).show()
        //val intent = Intent()
        //intent.putExtra("dataChanged", mDataChanged)
        finish()
    }*/
}