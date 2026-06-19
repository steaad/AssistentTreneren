package com.example.assistenttreneren.feature.upload.data.worker

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val mediaType: MediaType,
) : RequestBody() {
    override fun contentType(): MediaType = mediaType

    override fun contentLength(): Long =
        contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
            descriptor.length
        } ?: -1L

    override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) {
                    break
                }
                sink.write(buffer, 0, read)
            }
        } ?: error("Kunne ikke lese opptaksfil.")
    }
}
