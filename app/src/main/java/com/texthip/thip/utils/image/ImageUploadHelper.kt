package com.texthip.thip.utils.image

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.texthip.thip.BuildConfig
import com.texthip.thip.data.model.feed.request.ImageMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploadHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val s3Client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    }
                )
            }
        }
        .build()

    suspend fun uploadImageToS3(
        uri: Uri,
        presignedUrl: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}")

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw IllegalStateException("Failed to open input stream for URI: $uri")

                val requestBody = tempFile.readBytes().toRequestBody(mimeType.toMediaType())
                val request = Request.Builder()
                    .url(presignedUrl)
                    .put(requestBody)
                    .build()

                val response = s3Client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("S3 upload failed: ${response.code} ${response.message}")
                }
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        }
    }

    suspend fun getImageMetadata(uri: Uri): ImageMetadata? = withContext(Dispatchers.IO) {
        runCatching {
            val mimeType = context.contentResolver.getType(uri) ?: return@withContext null
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/jpeg", "image/jpg" -> "jpg"
                "image/gif" -> "gif"
                else -> return@withContext null
            }

            // 성능 최적화된 파일 크기 계산
            val size = getFileSize(uri) ?: return@withContext null

            ImageMetadata(
                extension = extension,
                size = size
            )
        }.getOrNull()
    }

    private fun getFileSize(uri: Uri): Long? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex >= 0) {
                            val size = cursor.getLong(sizeIndex)
                            if (size > 0) return size
                        }
                    }
                }

            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val size = pfd.statSize
                if (size > 0) return size
            }

            null
        } catch (e: Exception) {
            null
        }
    }
}