package com.pekempy.ReadAloudbooks.ui.player

import android.content.Intent
import android.media.MediaFormat
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.Format
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecAdapter
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.datasource.DefaultHttpDataSource
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.Executors

@UnstableApi
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var bitmapLoaderExecutor: java.util.concurrent.ExecutorService? = null

    override fun onCreate() {
        super.onCreate()
        
        val renderersFactory = DefaultRenderersFactory(this).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            setEnableDecoderFallback(true)
        }
        
        val trackSelector = DefaultTrackSelector(this)
        
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(20000, 60000, 2000, 3000)
            .build()
            
        val dataSourceFactory = androidx.media3.datasource.DataSource.Factory {
            val token = com.pekempy.ReadAloudbooks.data.api.AppContainer.apiClientManager.token
            val httpFactory = DefaultHttpDataSource.Factory()
            if (token != null) {
                httpFactory.setDefaultRequestProperties(mapOf("Authorization" to "Bearer $token"))
            }
            
            val defaultFactory = androidx.media3.datasource.DefaultDataSource.Factory(this, httpFactory)
            
            object : androidx.media3.datasource.DataSource {
                private var activeDataSource: androidx.media3.datasource.DataSource? = null
                
                override fun addTransferListener(transferListener: androidx.media3.datasource.TransferListener) {
                    defaultFactory.createDataSource().addTransferListener(transferListener)
                }

                override fun open(dataSpec: androidx.media3.datasource.DataSpec): Long {
                    val uri = dataSpec.uri.toString()
                    activeDataSource = if (uri.startsWith("ffmpeg://")) {
                        val realUri = uri.substring("ffmpeg://".length)
                        var finalStreamUri = realUri
                        var durationMs = 0L
                        
                        if (realUri.contains("//")) {
                            val parts = realUri.split("//", limit = 2)
                            val params = parts[0].split("&")
                            params.forEach { param ->
                                val kv = param.split("=")
                                if (kv.size == 2) {
                                    when (kv[0]) {
                                        "durationMs" -> durationMs = kv[1].toLongOrNull() ?: 0L
                                    }
                                }
                            }
                            finalStreamUri = parts[1]
                        }
                        com.pekempy.ReadAloudbooks.util.FfmpegStreamingDataSource(this@PlaybackService, finalStreamUri, durationMs)
                    } else {
                        defaultFactory.createDataSource()
                    }
                    return activeDataSource!!.open(dataSpec)
                }

                override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                    return activeDataSource?.read(buffer, offset, length) ?: -1
                }

                override fun getUri(): android.net.Uri? = activeDataSource?.getUri()

                override fun close() {
                    activeDataSource?.close()
                    activeDataSource = null
                }
            }
        }

        val extractorsFactory = androidx.media3.extractor.DefaultExtractorsFactory()
            .setAdtsExtractorFlags(androidx.media3.extractor.ts.AdtsExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)

        val player = ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this, extractorsFactory)
                    .setDataSourceFactory(dataSourceFactory)
            )
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        
        val leanPlayer = object : androidx.media3.common.ForwardingPlayer(player) {
            override fun getMediaMetadata(): androidx.media3.common.MediaMetadata {
                val original = super.getMediaMetadata()
                return androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(original.title)
                    .setArtist(original.artist)
                    .setSubtitle(original.subtitle)
                    .setArtworkUri(original.artworkUri)
                    .setExtras(original.extras)
                    .build()
            }
        }
        
        player.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                android.util.Log.d("PlaybackService", "=== Available Tracks ===")
                
                for (trackGroup in tracks.groups) {
                    android.util.Log.d("PlaybackService", "Track Group (type: ${trackGroup.type}):")
                    
                    for (i in 0 until trackGroup.length) {
                        val format = trackGroup.getTrackFormat(i)
                        val isSelected = trackGroup.isTrackSelected(i)
                        val isSupported = trackGroup.isTrackSupported(i)
                        
                        android.util.Log.d("PlaybackService", "  Track $i: ${format.sampleMimeType}")
                        android.util.Log.d("PlaybackService", "    - Codec: ${format.codecs}")
                        android.util.Log.d("PlaybackService", "    - Channels: ${format.channelCount}")
                        android.util.Log.d("PlaybackService", "    - Sample Rate: ${format.sampleRate}")
                        android.util.Log.d("PlaybackService", "    - Bitrate: ${format.bitrate}")
                        android.util.Log.d("PlaybackService", "    - Selected: $isSelected")
                        android.util.Log.d("PlaybackService", "    - Supported: $isSupported")
                    }
                }
                
                android.util.Log.d("PlaybackService", "======================")
            }
            
            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("PlaybackService", "Playback error (code ${error.errorCode}): ${error.message}")
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        android.util.Log.d("PlaybackService", "Playback ready")
                    }
                    Player.STATE_BUFFERING -> {
                        android.util.Log.d("PlaybackService", "Buffering...")
                    }
                    Player.STATE_ENDED -> {
                        android.util.Log.d("PlaybackService", "Playback ended")
                    }
                    Player.STATE_IDLE -> {
                        android.util.Log.d("PlaybackService", "Player idle")
                    }
                }
            }
        })
        
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .build()
        )
        
        val executor = Executors.newSingleThreadExecutor()
        bitmapLoaderExecutor = executor
        val bitmapLoader = DataSourceBitmapLoader(
            MoreExecutors.listeningDecorator(executor),
            dataSourceFactory
        )

        mediaSession = MediaSession.Builder(this, leanPlayer)
            .setBitmapLoader(bitmapLoader)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null) {
            if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
                stopSelf()
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        bitmapLoaderExecutor?.shutdown()
        mediaSession?.run {
            player.release()
            release()
        }
        super.onDestroy()
    }
}
