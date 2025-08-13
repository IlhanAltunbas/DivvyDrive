package com.ilhanaltunbas.divvydrive.data.network

import android.content.ContentValues.TAG
import android.os.Looper
import androidx.compose.foundation.ExperimentalFoundationApi
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.io.InputStream
import android.os.Handler
import android.util.Log
import java.io.ByteArrayInputStream

class ProgressRequestBody(
    private val fileBytes: ByteArray, // InputStream yerine ByteArray al
    private val listener: UploadCallbacks?
) : RequestBody() {

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8 * 1024
    }
    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int)
        fun onError(e: Exception)
        fun onFinish()
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun contentType(): MediaType? {
        return "application/octet-stream".toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        val length = fileBytes.size.toLong() // Doğrudan ByteArray'in boyutunu kullan
        Log.d(TAG, "contentLength() called, returning: $length")
        return length
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val currentFileSize = fileBytes.size.toLong() // Her çağrıda doğru boyutu al
        Log.d(TAG, "writeTo() started. Expected fileSize: $currentFileSize")

        var uploaded: Long = 0
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var read: Int
        var loopCount = 0

        // HER writeTo ÇAĞRISINDA YENİ BİR InputStream OLUŞTUR
        val inputStream: InputStream = ByteArrayInputStream(fileBytes) // EKLENDİ

        Log.d(TAG, "writeTo(): Fresh InputStream created. Available: ${inputStream.available()}")


        try {
            inputStream.use { stream -> // stream (yeni oluşturulan ByteArrayInputStream)
                Log.d(TAG, "InputStream.use block entered.")
                while (stream.read(buffer).also { read = it } != -1) {
                    loopCount++
                    if (read > 0) {
                        uploaded += read.toLong()
                        sink.write(buffer, 0, read)
                        val progress = ((uploaded.toDouble() / currentFileSize.toDouble()) * 100).toInt()
                        handler.post { listener?.onProgressUpdate(progress) }
                    } else if (read == 0) {
                        Log.w(TAG, "Loop $loopCount: stream.read(buffer) returned 0.")
                    }
                }
                Log.d(TAG, "Finished reading from stream. Total loops: $loopCount, Total uploaded: $uploaded")
            }

            if (uploaded == currentFileSize) {
                Log.d(TAG, "Successfully uploaded all bytes. Calling onFinish().")
                handler.post { listener?.onFinish() }
            } else {
                Log.e(TAG, "UPLOAD SIZE MISMATCH! Expected: $currentFileSize, Actual: $uploaded. NOT calling onFinish(). Calling onError() instead.")
                val errorMsg = "File size mismatch: Expected $currentFileSize, got $uploaded"
                handler.post { listener?.onError(IOException(errorMsg)) }
                throw IOException(errorMsg)
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException in writeTo: ${e.message}", e)
            handler.post { listener?.onError(e) }
            throw e // Hatayı tekrar fırlat
        } catch (e: Exception) {
            Log.e(TAG, "Generic Exception in writeTo: ${e.message}", e)
            handler.post { listener?.onError(e) }
            throw IOException("Generic error in ProgressRequestBody: ${e.message}", e)
        }
    }
}