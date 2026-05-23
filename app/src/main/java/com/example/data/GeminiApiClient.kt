package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Helper to call the Gemini API.
     * Retries or fails gracefully with null if key is placeholder or request fails.
     */
    suspend fun analyzeSymptoms(prompt: String, base64Image: String? = null): String {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return "OF_LINE_FALLBACK"
        }

        val parts = mutableListOf<Part>()
        parts.add(Part(text = prompt))
        if (!base64Image.isNullOrEmpty()) {
            parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image)))
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = parts)),
            systemInstruction = Content(
                parts = listOf(Part(text = "Anda adalah Asisten Pakar Budidaya Semangka. Jawablah dalam Bahasa Indonesia yang sederhana, ramah, dan mudah dipahami oleh petani muda maupun tua. Berikan rekomendasi teknis yang akurat sesuai standar industri semangka."))
            )
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Gagal memproses hasil diagnosis. Silakan ulangi beberapa saat lagi."
        } catch (e: Exception) {
            e.printStackTrace()
            "Gagal menghubungi server AI (${e.localizedMessage}). Menggunakan sistem analisis luring..."
        }
    }
}
