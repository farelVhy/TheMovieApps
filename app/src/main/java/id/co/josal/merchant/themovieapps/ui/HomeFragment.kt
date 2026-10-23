package id.co.josal.merchant.themovieapps.ui.main.homefragment

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import id.co.josal.merchant.themovieapps.R
import id.co.josal.merchant.themovieapps.base.BaseFragment
import id.co.josal.merchant.themovieapps.databinding.FragmentHomeBinding
import id.co.josal.merchant.themovieapps.ui.detail.DetailActivity
import id.co.josal.merchant.themovieapps.ui.main.adapter.MovieAdapter
import id.co.josal.merchant.themovieapps.ui.main.adapter.SectionHeaderAdapter
import id.co.josal.merchant.themovieapps.ui.main.viewmodel.HomeUiState
import id.co.josal.merchant.themovieapps.ui.main.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>({ inflater, parent, attach -> FragmentHomeBinding.inflate(inflater, parent, attach) }) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var movieAdapter: MovieAdapter
    private lateinit var headerAdapter: SectionHeaderAdapter

    // Dipakai untuk mendeteksi pergantian kata kunci -> scroll grid kembali ke atas
    private var lastRenderedQuery: String? = null

    override fun onViewInit() {
        setupWindowInsets()
        setupHeaderFade()
        setupSearchBar()
        setupRecyclerView()
        setupRetryButton()
        observeData()
    }

    private fun setupWindowInsets() {
        // Edge-to-edge: header gradient memanjang sampai ke belakang status bar,
        // tapi isinya diberi padding setinggi status bar supaya tidak tertimpa jam & ikon
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }
    }

    private fun setupHeaderFade() {
        // Judul memudar sesuai seberapa jauh ia sudah ter-scroll ke atas:
        // offset 0 -> terlihat penuh, offset maksimal -> transparan
        binding.appBar.addOnOffsetChangedListener { appBar, verticalOffset ->
            val range = appBar.totalScrollRange
            if (range == 0) return@addOnOffsetChangedListener
            val progress = -verticalOffset.toFloat() / range // 0f = terbuka, 1f = tertutup
            binding.layoutGreeting.alpha = 1f - progress
        }
    }

    // ===== SEARCH BAR =====

    private fun setupSearchBar() {
        binding.etSearch.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            binding.btnClearSearch.isVisible = value.isNotEmpty()
            viewModel.onSearchQueryChanged(value) // Debounce 2 detik diatur di ViewModel
        }

        // Tombol search di keyboard -> cari langsung tanpa menunggu
        binding.etSearch.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.submitSearch(view.text.toString())
                hideKeyboard()
                true
            } else {
                false
            }
        }

        // Tombol X -> kosongkan teks, otomatis kembali ke Now Playing
        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        // Garis tepi search bar menyala saat sedang fokus
        binding.etSearch.setOnFocusChangeListener { _, hasFocus -> updateSearchBarStyle(hasFocus) }
        updateSearchBarStyle(hasFocus = false)
    }

    private fun updateSearchBarStyle(hasFocus: Boolean) {
        val lightBlue = ContextCompat.getColor(requireContext(), R.color.tmdb_light_blue)
        val density = resources.displayMetrics.density
        binding.cardSearch.strokeColor =
            if (hasFocus) lightBlue else ColorUtils.setAlphaComponent(lightBlue, 90)
        binding.cardSearch.strokeWidth = ((if (hasFocus) 2 else 1) * density).toInt()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        binding.etSearch.clearFocus()
    }

    // ===== GRID FILM =====

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter()
        movieAdapter.onItemClick = { movie ->
            DetailActivity.start(requireContext(), movie.id, movie.title)
        }

        headerAdapter = SectionHeaderAdapter()

        // Grid 2 kolom, tapi item pertama (judul section) mengambil lebar penuh 2 kolom
        val gridLayoutManager = GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = if (position == 0) 2 else 1
            }
        }

        binding.rvMovies.apply {
            layoutManager = gridLayoutManager
            // ConcatAdapter: judul section + daftar film digabung dalam satu RecyclerView
            adapter = ConcatAdapter(headerAdapter, movieAdapter)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    if (dy > 0) { // Hanya cek saat scroll ke bawah
                        // User mulai scroll hasil -> tutup keyboard supaya grid terlihat penuh
                        if (binding.etSearch.hasFocus()) hideKeyboard()

                        val layoutManager = rv.layoutManager as GridLayoutManager
                        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                        val totalItemCount = layoutManager.itemCount

                        // Sisa 4 item sebelum akhir list -> muat halaman berikutnya.
                        // Pengecekan loading & halaman terakhir sudah ditangani di ViewModel.
                        if (lastVisibleItem >= totalItemCount - 4) {
                            viewModel.loadNextPage()
                        }
                    }
                }
            })
        }
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener { viewModel.retry() }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: HomeUiState) {
        renderSearchIndicator(state)
        renderSectionHeader(state)

        binding.progressBar.isVisible = state.isInitialLoading
        binding.progressLoadMore.isVisible = state.isLoadingMore
        binding.layoutError.isVisible = state.showFullScreenError
        binding.layoutEmpty.isVisible = state.showEmpty
        binding.rvMovies.isVisible = state.movies.isNotEmpty()

        binding.tvErrorMessage.text = state.errorMessage
        binding.tvEmpty.text = if (state.isSearching) {
            getString(R.string.home_search_empty, state.query)
        } else {
            getString(R.string.home_empty)
        }

        movieAdapter.submitList(state.movies)

        // Kata kunci berganti -> kembali ke paling atas & tampilkan header lagi
        if (lastRenderedQuery != null && lastRenderedQuery != state.query) {
            binding.rvMovies.scrollToPosition(0)
            binding.appBar.setExpanded(true, true)
        }
        lastRenderedQuery = state.query

        // Error saat memuat halaman berikutnya (list sudah ada isinya) -> cukup Snackbar
        val errorMessage = state.errorMessage
        if (errorMessage != null && state.movies.isNotEmpty()) {
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_retry) { viewModel.retry() }
                .show()
            viewModel.consumeError()
        }
    }

    // Ikon kaca pembesar berubah jadi loading kecil saat menunggu / sedang mencari
    private fun renderSearchIndicator(state: HomeUiState) {
        val showProgress = state.isSearchPending || (state.isSearching && state.isInitialLoading)
        binding.progressSearch.isVisible = showProgress
        binding.ivSearchIcon.isVisible = !showProgress
    }

    private fun renderSectionHeader(state: HomeUiState) {
        val title = if (state.isSearching) {
            getString(R.string.section_search_result)
        } else {
            getString(R.string.section_now_playing)
        }
        val subtitle = when {
            state.isSearchPending -> getString(R.string.section_search_pending)
            state.isSearching && state.isInitialLoading -> getString(R.string.section_search_loading, state.query)
            state.isSearching -> getString(R.string.section_search_count, state.query, state.totalResults)
            else -> getString(R.string.section_now_playing_subtitle)
        }
        headerAdapter.setHeader(title, subtitle)
    }
}