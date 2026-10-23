package id.co.josal.merchant.themovieapps.api

import id.co.josal.merchant.themovieapps.BuildConfig
import id.co.josal.merchant.themovieapps.api.service.MasterService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object ApiClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    // Ambil token dari BuildConfig
    private val token = BuildConfig.TMDB_TOKEN

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    // Inisialisasi Retrofit
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // PERBAIKAN: Gunakan tipe TmdbService, bukan MasterService
    val service: MasterService by lazy {
        retrofit.create(MasterService::class.java)
    }
}