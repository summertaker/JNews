package com.summertaker.jnews

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_article.view.*

class RecyclerAdapter(private val articles: ArrayList<Article>, private val context: Context) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun getItemCount() = articles.size

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        val article = articles[position]
        val listener = View.OnClickListener {it ->
            Toast.makeText(it.context, article.title, Toast.LENGTH_SHORT).show()
        }
        holder.apply {
            bind(listener, article)
            itemView.tag = article
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerAdapter.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_article, parent, false)
        return RecyclerAdapter.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private var view: View = v

        fun bind(listener: View.OnClickListener, article: Article) {

            if (view.thumbnail != null) {
                val yid = article.yid.toString()
                val url = "https://i.ytimg.com/vi/$yid/maxresdefault.jpg"
                //Log.e(">>", "url: $url")
                Glide.with(view.context).load(url).into(view.thumbnail)
            };

            view.title.text = article.title

            if (TextUtils.isEmpty(article.contentUri)) {
                view.tick.visibility = View.GONE
            } else {
                view.tick?.visibility = View.VISIBLE
            }

            view.setOnClickListener(listener)
        }
    }
}
/*
class RecyclerAdapter(private val articles: ArrayList<Article>, private val context: Context) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return RecyclerAdapter.ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.list_item_article, parent, false)
        )
    }

    //override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    //    holder.bind(articles[position], context)
    //}

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        val item = articles[position]
        val listener = View.OnClickListener {it ->
            Toast.makeText(it.context, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
        }
        holder.apply {
            bind(item, context)
            //bind(item, context)
            itemView.tag = item
        }
    }


    override fun getItemCount(): Int {
        return articles.count()
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        private val thumbnail = itemView?.findViewById<ImageView>(R.id.thumbnail)
        private val title = itemView?.findViewById<TextView>(R.id.title)
        private val tick = itemView?.findViewById<ImageView>(R.id.tick)

        fun bind(article: Article?, context: Context) {
            if (thumbnail != null) {
                val yid = article?.yid.toString()
                val url = "https://i.ytimg.com/vi/$yid/maxresdefault.jpg"
                //Log.e(">>", "url: $url")
                Glide.with(context).load(url).into(thumbnail)
            };

            title?.text = article?.title

            if (article?.contentUri == null) {
                tick?.visibility = View.GONE
            } else {
                tick?.visibility = View.VISIBLE
            }
            //size?.append("x${article?.height}")
        }
    }
}
*/