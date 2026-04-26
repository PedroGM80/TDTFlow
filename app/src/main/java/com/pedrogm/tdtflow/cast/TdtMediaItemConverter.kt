package com.pedrogm.tdtflow.cast

import android.net.Uri
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata as CastMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import org.json.JSONObject

/**
 * Converts Media3 MediaItems to Cast MediaQueueItems for the Default Media Receiver.
 *
 * The default DefaultMediaItemConverter uses STREAM_TYPE_UNKNOWN (treated as buffered
 * by the receiver) and does not set MediaMetadata.mediaType, causing two issues on
 * Sony Bravia / DMR:
 *   - Black screen: live HLS needs STREAM_TYPE_LIVE so the receiver does not try to
 *     seek to position 0 (which doesn't exist for live streams).
 *   - Music player UI: without MEDIA_TYPE_MOVIE the DMR falls back to music layout.
 */
class TdtMediaItemConverter : MediaItemConverter {

    companion object {
        private const val KEY_MEDIA_ITEM = "mediaItem"
        private const val KEY_URI = "uri"
        private const val KEY_MIME = "mimeType"
        private const val KEY_MEDIA_TYPE = "mediaType"
    }

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val uri = mediaItem.localConfiguration?.uri?.toString()
            ?: mediaItem.mediaId

        val isRadio = mediaItem.mediaMetadata.mediaType == MediaMetadata.MEDIA_TYPE_MUSIC
        val isHls = uri.contains("m3u8", ignoreCase = true)

        val castMetadata = CastMetadata(
            if (isRadio) CastMetadata.MEDIA_TYPE_MUSIC_TRACK else CastMetadata.MEDIA_TYPE_MOVIE
        ).apply {
            mediaItem.mediaMetadata.title?.toString()?.takeIf { it.isNotEmpty() }?.let {
                putString(CastMetadata.KEY_TITLE, it)
            }
            mediaItem.mediaMetadata.artworkUri?.let { addImage(WebImage(it)) }
        }

        val contentType = if (isHls) "application/x-mpegurl" else "video/mp4"

        // Round-trip payload so toMediaItem() can reconstruct the original MediaItem.
        val customData = JSONObject().apply {
            put(KEY_URI, uri)
            put(KEY_MIME, contentType)
            put(KEY_MEDIA_TYPE, mediaItem.mediaMetadata.mediaType ?: MediaMetadata.MEDIA_TYPE_TV_SHOW)
        }

        val mediaInfo = MediaInfo.Builder(uri)
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            // Explicit contentUrl required by Cast SDK v3+ — contentId alone is not
            // fetched by all receivers; without it the DMR stalls after the initial buffer.
            .setContentUrl(uri)
            .setContentType(contentType)
            // UNKNOWN_DURATION (-1) tells the receiver this is an unbounded live stream.
            // Without this the DMR defaults to 0 ms duration and freezes once the
            // initially buffered segments are consumed.
            .setStreamDuration(MediaInfo.UNKNOWN_DURATION)
            .setMetadata(castMetadata)
            .setCustomData(customData)
            .build()

        // startTime(-1) anchors playback at the live edge instead of DVR window start.
        return MediaQueueItem.Builder(mediaInfo)
            .setStartTime(MediaQueueItem.CURRENT_TIME)
            .build()
    }

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
        val info = mediaQueueItem.media ?: return MediaItem.EMPTY
        val custom = info.customData

        val uri = custom?.optString(KEY_URI)?.takeIf { it.isNotEmpty() }
            ?: info.contentUrl
            ?: info.contentId
            ?: return MediaItem.EMPTY
        val mimeType = custom?.optString(KEY_MIME) ?: info.contentType
        val mediaType = custom?.optInt(KEY_MEDIA_TYPE, MediaMetadata.MEDIA_TYPE_TV_SHOW)
            ?: MediaMetadata.MEDIA_TYPE_TV_SHOW

        val title = info.metadata?.getString(CastMetadata.KEY_TITLE)
        val artworkUri = info.metadata?.images?.firstOrNull()?.url

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtworkUri(artworkUri)
            .setMediaType(mediaType)
            .build()

        return MediaItem.Builder()
            .setUri(uri)
            .setMimeType(mimeType)
            .setMediaId(uri)
            .setMediaMetadata(metadata)
            .build()
    }
}
