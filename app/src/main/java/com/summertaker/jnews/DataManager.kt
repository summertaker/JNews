package com.summertaker.jnews

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import java.io.File
import java.io.PrintWriter

class DataManager(val context: Context) {

    private val logTag = Config.logPrefix + this.javaClass.simpleName

    fun getVideoFiles(): ArrayList<Video> {
        val videos: ArrayList<Video> = ArrayList()

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
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val relativePathColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)

            val resolver = context.contentResolver
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val relativePath = cursor.getString(relativePathColumn)

                val ok = relativePath.contains(
                    Config.localDownloadSubPath,
                    ignoreCase = false
                ) // "日本語" 디렉토리
                if (ok) {
                    val contentUri =
                        Uri.withAppendedPath(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )
                    resolver.openFileDescriptor(contentUri, "r")
                    val thumbnail = resolver.loadThumbnail(contentUri, Size(160, 90), null)

                    val video = Video(id, displayName, contentUri, thumbnail)
                    videos.add(video)
                }
            }
            cursor.close()
        }

        return videos
    }

    /*fun getLocalData(response: String?) {
        //val dir = File(getExternalFilesDir(null), getString(R.string.japanese))
        //if (dir.isDirectory) {
        //    val fs = dir.listFiles()
        //    fs?.forEach {
        //        Log.d(logTag, ">> ${it.name}")
        //        val video = Video(it.toURI().toString(), it.path, it.name)
        //        mVideos.add(video)
        //    }
        //} else {
        //    Log.e(logTag, getString(R.string.no_directory) + ": " + dir)
        //}

        var jsonString = ""
        if (response == null) {
            val dir = File(context.getExternalFilesDir(null), Config.localDownloadSubPath)
            val src = File(dir, Config.localDataFileName)
            if (src.exists()) {
                jsonString = src.readText()
                //Log.e(logTag, jsonString)
            }
        } else {
            jsonString = response
        }

        val allVideos: ArrayList<Video> = ArrayList()
        val localVideos: ArrayList<Video> = ArrayList()

        if (jsonString.isNotEmpty()) {
            //val articles: ArrayList<Article> = ArrayList()
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("articles")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val file = obj.getString("file")
                if (file.isNullOrEmpty()) {
                    continue
                }
                //Log.e(logTag, file) // upload/I6PxDCRxJEQ.mp4

                //val fileName = file.substring(file.lastIndexOf('/') + 1)
                //var storageFile = ""
                //for (video in videos) {
                //    if (fileName == video.storageFile) {
                //        storageFile = video.contentUri.toString()
                //        break
                //    }
                //}

                val video = Video(
                    obj.getString("id"),
                    obj.getString("yid"),
                    obj.getString("title"),
                    file,
                    null,
                    null
                )
                allVideos.add(video)
            }

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
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val relativePathColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)

                val resolver = context.contentResolver

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val relativePath = cursor.getString(relativePathColumn)

                    val ok = relativePath.contains(
                        Config.localDownloadSubPath,
                        ignoreCase = false
                    ) // 日本語 디렉토리
                    if (ok) {
                        val contentUri =
                            Uri.withAppendedPath(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id.toString()
                            )

                        for (video in allVideos) {
                            val serverFile =
                                video.file?.substring(video.file.lastIndexOf('/') + 1)
                            if (displayName == serverFile) {
                                val storageFile = Config.localDownloadSubPath + displayName
                                resolver.openFileDescriptor(contentUri, "r")
                                val thumbnail =
                                    resolver.loadThumbnail(contentUri, Size(160, 90), null)

                                val v =
                                    Video(
                                        video.id,
                                        video.yid,
                                        video.title,
                                        video.file,
                                        storageFile,
                                        thumbnail
                                    )
                                localVideos.add(v)
                                break
                            }
                        }
                    }
                }
                cursor.close()
            }
        }
        listener.getLocalDataCallback(localVideos)
    }*/

    fun saveFile(response: String): Boolean {
        var success = true
        val dir = File(context.getExternalFilesDir(null), Config.localDownloadSubPath)
        if (!dir.isDirectory) {
            success = dir.mkdir()
        }
        if (success) {
            val dest = File(dir, Config.localDataFileName)
            if (dest.exists()) dest.delete()

            try {
                // response is the data written to file
                PrintWriter(dest).use { out -> out.println(response) }
            } catch (e: Exception) {
                success = false
                Toast.makeText(context, "파일 저장 실패", Toast.LENGTH_SHORT).show()
                Log.e(logTag, e.message.toString())
            }
        } else {
            success = false
            Toast.makeText(context, "디렉토리 만들기 실패", Toast.LENGTH_SHORT).show()
        }
        return success
    }

/*fun parseJson(response: String, videos: ArrayList<Video>): ArrayList<Article> {
    val articles: ArrayList<Article> = ArrayList()

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
        var storageFile = ""
        for (video in videos) {
            if (fileName == video.displayName) {
                storageFile = video.contentUri.toString()
                break
            }
        }

        val article = Article(
            obj.getString("id"),
            obj.getString("yid"),
            obj.getString("title"),
            file,
            storageFile
        )
        articles.add(article)
    }
    return articles
}*/
}