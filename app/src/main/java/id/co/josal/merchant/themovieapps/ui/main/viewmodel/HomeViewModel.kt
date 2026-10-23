package id.co.josal.merchant.themovieapps.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.co.josal.merchant.themovieapps.api.model.Movie
import id.co.josal.merchant.themovieapps.api.repository.MovieRepository
import id.co.josal.merchant.themovieapps.helper.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val query: String = "", // Kata kunci yang sedang aktif ("" = mode Now Playing)
    val movies: List<Movie> = emptyList(),
    val totalResults: Int = 0,
    val isInitialLoading: Boolean = false, // Loading halaman pertama (progress di tengah)
    val isLoadingMore: Boolean = false, // Loading halaman berikutnya (progress di bawah)
    val isSearchPending: Boolean = false, // User sedang mengetik, menunggu 2 detik sebelum mencari
    val errorMessage: String? = null,
    val isLastPage: Boolean = false
) {
    val isSearching get() = query.isNotBlank()

    // Error saat list masih kosong -> tampilkan layout error penuh + tombol Coba Lagi
    val showFullScreenError get() = errorMessage != null && movies.isEmpty()

    // Berhasil tapi tidak ada data (mis. hasil pencarian kosong)
    val showEmpty get() = !isInitialLoading && errorMessage == null && movies.isEmpty()
}

class HomeViewModel(private val repository: MovieRepository = MovieRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var currentQuery = ""
    private var job: Job? = null // Request daftar film (now playing / search)
    private var searchJob: Job? = null // Timer debounce pencarian

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        // Cegah request ganda saat masih loading, dan berhenti di halaman terakhir
        if (job?.isActive == true || _uiState.value.isLastPage) return

        val query = currentQuery
        job = viewModelScope.launch {
            val isFirstPage = currentPage == 1
            _uiState.update {
                it.copy(isInitialLoading = isFirstPage, isLoadingMore = !isFirstPage, errorMessage = null)
            }

            // Query kosong -> Now Playing, ada query -> Search
            val result = if (query.isBlank()) {
                repository.getNowPlaying(currentPage)
            } else {
                repository.searchMovies(query, currentPage)
            }

            when (result) {
                is Resource.Success -> {
                    val response = result.data ?: return@launch
                    _uiState.update {
                        it.copy(
                            // distinctBy: TMDB kadang mengirim film yang sama di dua halaman
                            movies = (it.movies + response.results).distinctBy { movie -> movie.id },
                            totalResults = response.totalResults,
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

    /**
     * Dipanggil setiap kali teks di search bar berubah.
     * Debounce: setiap ketikan baru membatalkan timer sebelumnya,
     * jadi pencarian baru dijalankan 2 detik setelah user BERHENTI mengetik.
     */
    fun onSearchQueryChanged(text: String) {
        val query = text.trim()
        searchJob?.cancel()

        // Teks sama dengan pencarian aktif (mis. hanya menambah spasi) -> tidak perlu request
        if (query == currentQuery) {
            _uiState.update { it.copy(isSearchPending = false) }
            return
        }

        // Search bar dikosongkan -> langsung kembali ke Now Playing tanpa menunggu
        if (query.isEmpty()) {
            applyQuery("")
            return
        }

        _uiState.update { it.copy(isSearchPending = true) }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            applyQuery(query)
        }
    }

    // Tombol "Search" di keyboard ditekan -> langsung cari tanpa menunggu 2 detik
    fun submitSearch(text: String) {
        searchJob?.cancel()
        val query = text.trim()
        if (query == currentQuery) {
            _uiState.update { it.copy(isSearchPending = false) }
            return
        }
        applyQuery(query)
    }

    private fun applyQuery(query: String) {
        currentQuery = query
        currentPage = 1
        job?.cancel() // Batalkan request lama supaya hasilnya tidak tercampur dengan query baru
        _uiState.value = HomeUiState(query = query)
        loadNextPage()
    }

    fun retry() = loadNextPage()

    fun refresh() = applyQuery(currentQuery)

    // Dipanggil setelah Snackbar error ditampilkan, supaya tidak muncul lagi saat kembali dari halaman detail
    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 2_000L
    }
}