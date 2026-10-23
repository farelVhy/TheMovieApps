package id.co.josal.merchant.themovieapps.ui.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.co.josal.merchant.themovieapps.api.model.MovieDetailResponse
import id.co.josal.merchant.themovieapps.api.model.Review
import id.co.josal.merchant.themovieapps.api.model.Video
import id.co.josal.merchant.themovieapps.api.repository.MovieRepository
import id.co.josal.merchant.themovieapps.helper.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null, // Detail gagal dimuat -> layout error penuh
    val detail: MovieDetailResponse? = null,
    val trailer: Video? = null,
    val trailerMessage: String? = null, // Pesan jika trailer tidak ada / gagal dimuat
    val reviews: List<Review> = emptyList(),
    val totalReviews: Int = 0,
    val reviewMessage: String? = null // Pesan jika ulasan kosong / gagal dimuat
)

class DetailViewModel(private val repository: MovieRepository = MovieRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var movieId = 0
    private var job: Job? = null

    fun loadDetail(id: Int) {
        // Saat layar diputar, Activity dibuat ulang tapi ViewModel tetap -> tidak perlu request ulang
        if (id == movieId && (_uiState.value.detail != null || job?.isActive == true)) return
        movieId = id
        fetchAll()
    }

    fun retry() = fetchAll()

    private fun fetchAll() {
        job?.cancel()
        job = viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)

            // Tiga request berjalan PARALEL.
            // Total waktu tunggu = request yang paling lama, bukan dijumlahkan.
            val detailDeferred = async { repository.getMovieDetail(movieId) }
            val videosDeferred = async { repository.getMovieVideos(movieId) }
            val reviewsDeferred = async { repository.getMovieReviews(movieId) }

            // Detail wajib berhasil. Kalau gagal, tampilkan error penuh dan batalkan request lain.
            val detailResult = detailDeferred.await()
            val detail = detailResult.data
            if (detail == null) {
                videosDeferred.cancel()
                reviewsDeferred.cancel()
                _uiState.value = DetailUiState(
                    errorMessage = detailResult.message ?: "Gagal memuat detail film"
                )
                return@launch
            }

            // Video & ulasan boleh gagal, halaman tetap tampil dengan pesan di section masing-masing
            val videosResult = videosDeferred.await()
            val reviewsResult = reviewsDeferred.await()

            val trailer = videosResult.data?.results?.let { pickTrailer(it) }
            val trailerMessage = when {
                videosResult is Resource.Error -> "Trailer gagal dimuat"
                trailer == null -> "Trailer belum tersedia"
                else -> null
            }

            val reviewResponse = reviewsResult.data
            val reviews = reviewResponse?.results.orEmpty()
            val reviewMessage = when {
                reviewsResult is Resource.Error -> reviewsResult.message ?: "Ulasan gagal dimuat"
                reviews.isEmpty() -> "Belum ada ulasan untuk film ini"
                else -> null
            }

            _uiState.value = DetailUiState(
                detail = detail,
                trailer = trailer,
                trailerMessage = trailerMessage,
                reviews = reviews,
                totalReviews = reviewResponse?.totalResults ?: 0,
                reviewMessage = reviewMessage
            )
        }
    }

    // Prioritas: trailer resmi -> trailer apa saja -> teaser -> video YouTube apa saja
    private fun pickTrailer(videos: List<Video>): Video? {
        val youtubeVideos = videos.filter { it.site == "YouTube" }
        return youtubeVideos.firstOrNull { it.type == "Trailer" && it.official }
            ?: youtubeVideos.firstOrNull { it.type == "Trailer" }
            ?: youtubeVideos.firstOrNull { it.type == "Teaser" }
            ?: youtubeVideos.firstOrNull()
    }
}