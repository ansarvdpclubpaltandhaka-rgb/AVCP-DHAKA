package com.example.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.model.OrganizationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class CompressedImagePayload(
    val bytes: ByteArray,
    val originalKb: Int,
    val compressedKb: Int,
    val width: Int,
    val height: Int
)

object ImageUploadService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun compressImage(context: Context, uri: Uri, maxDimension: Int = 1080, quality: Int = 80): Result<CompressedImagePayload> =
        withContext(Dispatchers.IO) {
            try {
                val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(IllegalStateException("ছবি পড়া সম্ভব হয়নি"))

                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOpts)

                var sampleSize = 1
                val maxSide = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
                while (maxSide / sampleSize > maxDimension * 2) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts)
                    ?: return@withContext Result.failure(IllegalStateException("ছবি ডিকোড করা যায়নি"))

                val scaled = if (decoded.width > maxDimension || decoded.height > maxDimension) {
                    val ratio = minOf(
                        maxDimension.toFloat() / decoded.width,
                        maxDimension.toFloat() / decoded.height
                    )
                    val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
                    val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
                    Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
                        if (it !== decoded) decoded.recycle()
                    }
                } else {
                    decoded
                }

                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                val outBytes = out.toByteArray()
                val w = scaled.width
                val h = scaled.height
                scaled.recycle()

                Result.success(
                    CompressedImagePayload(
                        bytes = outBytes,
                        originalKb = (rawBytes.size / 1024).coerceAtLeast(1),
                        compressedKb = (outBytes.size / 1024).coerceAtLeast(1),
                        width = w,
                        height = h
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun uploadImage(
        payload: CompressedImagePayload,
        config: OrganizationConfig?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (config == null || config.imageUploadProvider == "none") {
                return@withContext Result.failure(
                    IllegalStateException("এক্সটার্নাল ইমেজ হোস্টিং কনফিগার করা হয়নি। সুপার অ্যাডমিন সেটিংস থেকে Cloudinary (Unsigned Preset) বা কাস্টম HTTPS আপলোড URL যুক্ত করুন, অথবা সরাসরি HTTPS ছবির লিংক দিন।")
                )
            }

            when (config.imageUploadProvider) {
                "cloudinary" -> {
                    val cloudName = config.cloudinaryCloudName.trim()
                    val preset = config.cloudinaryUploadPreset.trim()
                    if (cloudName.isBlank() || preset.isBlank()) {
                        return@withContext Result.failure(
                            IllegalStateException("Cloudinary Cloud Name বা Upload Preset খালি রয়েছে।")
                        )
                    }
                    val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "file",
                            "upload_${System.currentTimeMillis()}.jpg",
                            payload.bytes.toRequestBody("image/jpeg".toMediaType())
                        )
                        .addFormDataPart("upload_preset", preset)
                        .build()

                    val request = Request.Builder().url(url).post(requestBody).build()
                    client.newCall(request).execute().use { response ->
                        val bodyText = response.body?.string().orEmpty()
                        if (!response.isSuccessful) {
                            return@withContext Result.failure(
                                IllegalStateException("আপলোড ব্যর্থ হয়েছে (${response.code}): $bodyText")
                            )
                        }
                        val json = JSONObject(bodyText)
                        val secureUrl = json.optString("secure_url").ifBlank { json.optString("url") }
                        if (secureUrl.startsWith("https://")) {
                            Result.success(secureUrl)
                        } else {
                            Result.failure(IllegalStateException("সার্ভার থেকে বৈধ HTTPS URL পাওয়া যায়নি"))
                        }
                    }
                }

                "custom" -> {
                    val endpoint = config.customUploadUrl.trim()
                    if (!endpoint.startsWith("https://")) {
                        return@withContext Result.failure(
                            IllegalStateException("কাস্টম আপলোড URL অবশ্যই https:// দিয়ে শুরু হতে হবে।")
                        )
                    }
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "image",
                            "upload_${System.currentTimeMillis()}.jpg",
                            payload.bytes.toRequestBody("image/jpeg".toMediaType())
                        )
                        .build()

                    val request = Request.Builder().url(endpoint).post(requestBody).build()
                    client.newCall(request).execute().use { response ->
                        val bodyText = response.body?.string().orEmpty()
                        if (!response.isSuccessful) {
                            return@withContext Result.failure(
                                IllegalStateException("আপলোড ব্যর্থ হয়েছে (${response.code})")
                            )
                        }
                        val json = JSONObject(bodyText)
                        val direct = json.optString("secure_url")
                            .ifBlank { json.optString("url") }
                            .ifBlank { json.optJSONObject("data")?.optString("url").orEmpty() }
                        if (direct.startsWith("https://")) {
                            Result.success(direct)
                        } else {
                            Result.failure(IllegalStateException("রেসপন্সে HTTPS URL পাওয়া যায়নি"))
                        }
                    }
                }

                else -> Result.failure(IllegalStateException("অসমর্থিত ইমেজ হোস্টিং প্রোভাইডার"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
