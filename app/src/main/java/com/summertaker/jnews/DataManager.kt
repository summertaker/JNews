package com.summertaker.jnews

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

class DataManager(val context: Context, private val listener: DataInterface) {

    private val logTag = Config.logPrefix + this.javaClass.simpleName

    fun getLocalData() {
        val videos: ArrayList<Video> = ArrayList()

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

        val cursor = context.contentResolver.query(
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
                    videos.add(video)
                }
            }
            cursor.close()
        }

        listener.getLocalDataCallback(videos)
    }
}