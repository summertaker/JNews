package com.summertaker.jnews

object Config {
    const val logPrefix = ">> "

    const val remoteBaseUrl = "http://summertaker.cafe24.com/reader/"
    const val remoteDataUrl = remoteBaseUrl + "youtube_json.php"

    const val localDownloadSubPath = "日本語"
    const val localDataFileName = "japanese.json"

    const val activityRequestCodeArticles = 1  // The request code
    const val activityRequestCodeDownload = 1  // The request code
}