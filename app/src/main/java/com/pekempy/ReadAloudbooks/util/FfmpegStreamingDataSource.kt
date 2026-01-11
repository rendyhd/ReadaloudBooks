package com.pekempy.ReadAloudbooks.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFmpegSession
import java.io.FileInputStream
import java.io.IOException

class FfmpegStreamingDataSource(
    private val context: Context,
    private val streamUri: String,
    private val durationMs: Long = 0L
) : BaseDataSource(true) {

    private var ffmpegSession: FFmpegSession? = null
    private var pipePath: String? = null
    private var inputStream: FileInputStream? = null
    private var opened = false
    private val bitrateBps = 192000L

    override fun open(dataSpec: DataSpec): Long {
        Log.d("FfmpegDataSource", "Opening stream: $streamUri at position: ${dataSpec.position}")
        transferInitializing(dataSpec)
        
        close()
        
        pipePath = FFmpegKitConfig.registerNewFFmpegPipe(context)
        
        val token = com.pekempy.ReadAloudbooks.data.api.AppContainer.apiClientManager.token
        val isRemote = streamUri.startsWith("http")
        val headers = if (isRemote && token != null) "-headers \"Authorization: Bearer $token\r\n\" " else ""
        val seekOffsetMs = if (dataSpec.position > 0) {
            (dataSpec.position * 8 * 1000) / bitrateBps
        } else 0L
        
        val ssl = if (isRemote) "-reconnect 1 -reconnect_at_eof 1 -reconnect_streamed 1 -reconnect_on_network_error 1 -reconnect_on_http_4xx_errors 1 -reconnect_delay_max 5 " else ""
        val ss = if (seekOffsetMs > 0) "-ss ${seekOffsetMs / 1000.0} " else ""
        
        val command = "-y $headers$ssl$ss-fflags +genpts+discardcorrupt -i \"$streamUri\" -map 0:a:0 -vn -c:a libmp3lame -b:a ${bitrateBps/1000}k -ar 44100 -ac 2 -f mp3 -buffer_size 16M \"$pipePath\""
        
        Log.d("FfmpegDataSource", "Executing command: $command")
        ffmpegSession = FFmpegKit.executeAsync(command) { session ->
            Log.d("FfmpegDataSource", "FFmpeg session finished with state: ${session.state} and return code: ${session.returnCode}")
        }
 
        try {
            inputStream = FileInputStream(pipePath)
            opened = true
            transferStarted(dataSpec)
            
            return C.LENGTH_UNSET.toLong()
        } catch (e: Exception) {
            Log.e("FfmpegDataSource", "Failed to open pipe: ${e.message}")
            throw IOException(e)
        }
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (!opened || length == 0) return 0
        
        val bytesRead = try {
            inputStream?.read(buffer, offset, length) ?: -1
        } catch (e: IOException) {
            Log.e("FfmpegDataSource", "IO Error during pipe read: ${e.message}")
            -1
        }
        
        if (bytesRead > 0) {
            bytesTransferred(bytesRead)
            if (System.currentTimeMillis() % 5000 < 500) { 
                Log.v("FfmpegDataSource", "Reading data: $bytesRead bytes")
            }
        } else if (bytesRead == -1) {
            ffmpegSession?.let { session ->
                if (session.state == com.arthenica.ffmpegkit.SessionState.RUNNING) {
                    Log.w("FfmpegDataSource", "Read returned EOF but FFmpeg is still running!")
                }
            }
        }
        
        return bytesRead
    }

    override fun getUri(): Uri? = Uri.parse(streamUri)

    override fun close() {
        if (opened) {
            opened = false
            transferEnded()
        }
        
        try {
            inputStream?.close()
        } catch (e: IOException) {
        } finally {
            inputStream = null
        }

        ffmpegSession?.let {
            FFmpegKit.cancel(it.sessionId)
            ffmpegSession = null
        }
        
        pipePath?.let {
        }
    }
}
