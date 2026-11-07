package com.nexamusic.air.network.innertube

import com.nexamusic.air.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class InnerTubeClient {
    companion object {
        private const val API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
        private const val USER_AGENT = "com.google.android.youtube/17.31.35 (Linux; U; Android 12)"
    }

    suspend fun searchSongs(query: String): List<Track> = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("query", query)
            .put("context", androidContext())
            .toString()

        val response = postJson("https://www.youtube.com/youtubei/v1/search?key=$API_KEY", payload)
        val root = JSONObject(response)

        val sections = root
            .optJSONObject("contents")
            ?.optJSONObject("twoColumnSearchResultsRenderer")
            ?.optJSONObject("primaryContents")
            ?.optJSONObject("sectionListRenderer")
            ?.optJSONArray("contents") ?: JSONArray()

        buildList {
            for (i in 0 until sections.length()) {
                val items = sections.optJSONObject(i)
                    ?.optJSONObject("itemSectionRenderer")
                    ?.optJSONArray("contents") ?: continue
                for (j in 0 until items.length()) {
                    val video = items.optJSONObject(j)?.optJSONObject("videoRenderer") ?: continue
                    val videoId = video.optString("videoId")
                    if (videoId.isBlank()) continue
                    val title = video.optJSONObject("title")
                        ?.optJSONArray("runs")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        .orEmpty()
                    val artist = video.optJSONObject("ownerText")
                        ?.optJSONArray("runs")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        .orEmpty()
                    val duration = parseDurationMs(
                        video.optJSONObject("lengthText")?.optString("simpleText").orEmpty()
                    )
                    val artwork = video.optJSONObject("thumbnail")
                        ?.optJSONArray("thumbnails")
                        ?.optJSONObject(0)
                        ?.optString("url")

                    add(
                        Track(
                            id = videoId,
                            title = if (title.isBlank()) "Unknown title" else title,
                            artist = if (artist.isBlank()) "Unknown artist" else artist,
                            durationMs = duration,
                            artworkUrl = artwork
                        )
                    )
                }
            }
        }
    }

    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("videoId", videoId)
            .put("contentCheckOk", true)
            .put("racyCheckOk", true)
            .put("context", androidContext())
            .toString()

        val response = postJson("https://www.youtube.com/youtubei/v1/player?key=$API_KEY", payload)
        val root = JSONObject(response)
        val formats = root.optJSONObject("streamingData")?.optJSONArray("adaptiveFormats") ?: return@withContext null

        var bestUrl: String? = null
        var bestBitrate = -1
        for (i in 0 until formats.length()) {
            val format = formats.optJSONObject(i) ?: continue
            val mimeType = format.optString("mimeType")
            if (!mimeType.startsWith("audio/")) continue
            val url = format.optString("url")
            if (url.isBlank()) continue
            val bitrate = format.optInt("bitrate", 0)
            if (bitrate > bestBitrate) {
                bestBitrate = bitrate
                bestUrl = url
            }
        }
        bestUrl
    }

    private fun androidContext(): JSONObject = JSONObject()
        .put("client", JSONObject()
            .put("clientName", "ANDROID")
            .put("clientVersion", "17.31.35")
            .put("androidSdkVersion", 33)
            .put("hl", "en")
            .put("gl", "US")
        )

    private fun parseDurationMs(duration: String): Long {
        val parts = duration.split(':').mapNotNull { it.toLongOrNull() }
        if (parts.isEmpty()) return 0L
        val seconds = parts.fold(0L) { acc, value -> acc * 60 + value }
        return seconds * 1000L
    }

    private fun postJson(endpoint: String, payload: String): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "application/json")
        }

        connection.outputStream.bufferedWriter().use { it.write(payload) }
        val code = connection.responseCode
        val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()

        if (code !in 200..299) {
            error("InnerTube request failed ($code): $body")
        }
        return body
    }
}
