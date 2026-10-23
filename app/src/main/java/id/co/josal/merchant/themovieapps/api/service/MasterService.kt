package id.co.josal.merchant.themovieapps.api.service

import id.co.josal.merchant.themovieapps.api.model.MovieDetailResponse
import id.co.josal.merchant.themovieapps.api.model.MovieResponse
import id.co.josal.merchant.themovieapps.api.model.ReviewResponse
import id.co.josal.merchant.themovieapps.api.model.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MasterService {

    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("page") page: Int = 1
    ): MovieResponse

    // Pencarian film. Bentuk responsnya sama dengan now_playing, jadi MovieResponse bisa dipakai ulang
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): MovieResponse

    // Informasi utama film (judul, sinopsis, durasi, genre, dll)
    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int
    ): MovieDetailResponse

    // Daftar video film, trailer YouTube diambil dari sini
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int
    ): VideoResponse

    // Ulasan dari user
    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): ReviewResponse
}