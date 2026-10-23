package id.co.josal.merchant.themovieapps.api.repository

import id.co.josal.merchant.themovieapps.api.ApiClient
import id.co.josal.merchant.themovieapps.api.model.MovieDetailResponse
import id.co.josal.merchant.themovieapps.api.model.MovieResponse
import id.co.josal.merchant.themovieapps.api.model.ReviewResponse
import id.co.josal.merchant.themovieapps.api.model.VideoResponse
import id.co.josal.merchant.themovieapps.api.service.MasterService
import id.co.josal.merchant.themovieapps.helper.Resource

// Service di-inject lewat constructor supaya nanti bisa diganti dengan fake/mock saat Unit Test
class MovieRepository(private val service: MasterService = ApiClient.service) {

    suspend fun getNowPlaying(page: Int): Resource<MovieResponse> =
        ApiHelper.safeApiCall { service.getNowPlaying(page) }

    suspend fun getMovieDetail(movieId: Int): Resource<MovieDetailResponse> =
        ApiHelper.safeApiCall { service.getMovieDetail(movieId) }

    suspend fun getMovieVideos(movieId: Int): Resource<VideoResponse> =
        ApiHelper.safeApiCall { service.getMovieVideos(movieId) }

    suspend fun getMovieReviews(movieId: Int, page: Int = 1): Resource<ReviewResponse> =
        ApiHelper.safeApiCall { service.getMovieReviews(movieId, page) }
}