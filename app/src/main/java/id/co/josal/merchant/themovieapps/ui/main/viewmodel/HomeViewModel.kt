package id.co.josal.merchant.themovieapps.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.co.josal.merchant.themovieapps.api.model.Movie
import id.co.josal.merchant.themovieapps.api.repository.MovieRepository
import id.co.josal.merchant.themovieapps.helper.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val movies: List<Movie> = emptyList(),
    val isInitialLoading: Boolean = false, // Loading halaman pertama (progress di tengah)
    val isLoadingMore: Boolean = false, // Loading halaman berikutnya (progress di bawah)
    val errorMessage: String? = null,
    val isLastPage: Boolean = false
) {
    // Error saat list masih kosong -> tampilkan layout error penuh + tombol Coba Lagi
    val showFullScreenError get() = errorMessage != null && movies.isEmpty()

    // Berhasil tapi tidak ada data
    val showEmpty get() = !isInitialLoading && errorMessage == null && movies.isEmpty()
}

class HomeViewModel(private val repository: MovieRepository = MovieRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var job: Job? = null

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        // Cegah request ganda saat masih loading, dan berhenti di halaman terakhir
        if (job?.isActive == true || _uiState.value.isLastPage) return

        job = viewModelScope.launch {
            val isFirstPage = currentPage == 1
            _uiState.update {
                it.copy(isInitialLoading = isFirstPage, isLoadingMore = !isFirstPage, errorMessage = null)
            }

            when (val result = repository.getNowPlaying(currentPage)) {
                is Resource.Success -> {
                    val response = result.data ?: return@launch
                    _uiState.update {
                        it.copy(
                            // distinctBy: TMDB kadang mengirim film yang sama di dua halaman
                            movies = (it.movies + response.results).distinctBy { movie -> movie.id },
                            isInitialLoading = false,
                            isLoadingMore = false,
                            isLastPage = currentPage >= response.totalPages
                        )
                    }
                    currentPage++
                }
                is Resource.Error -> {
                    // Data lama TIDAK dihapus, hanya pesan error yang diisi
                    _uiState.update {
                        it.copy(isInitialLoading = false, isLoadingMore = false, errorMessage = result.message)
                    }
                }
                else -> Unit
            }
        }
    }

    fun retry() = loadNextPage()

    fun refresh() {
        job?.cancel()
        currentPage = 1
        _uiState.value = HomeUiState()
        loadNextPage()
    }

    // Dipanggil setelah Snackbar error ditampilkan, supaya tidak muncul lagi saat kembali dari halaman detail
    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}