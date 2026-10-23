package id.co.josal.merchant.themovieapps.api

import id.co.josal.merchant.themovieapps.BuildConfig
import id.co.josal.merchant.themovieapps.api.service.MasterService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val TIMEOUT_SECONDS = 15L

    // Token dibaca dari BuildConfig (diisi dari local.properties), tidak ditulis langsung di kode
    private val token = BuildConfig.TMDB_TOKEN

    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: MasterService by lazy {
        retrofit.create(MasterService::class.java)
    }
}