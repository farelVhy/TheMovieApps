package id.co.josal.merchant.themovieapps.ui.main.homefragment

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import id.co.josal.merchant.themovieapps.databinding.FragmentHomeBinding
import id.co.josal.merchant.themovieapps.ui.detail.DetailActivity
import id.co.josal.merchant.themovieapps.ui.main.adapter.MovieAdapter
import id.co.josal.merchant.themovieapps.ui.main.viewmodel.HomeUiState
import id.co.josal.merchant.themovieapps.ui.main.viewmodel.HomeViewModel
import id.co.themovieapps.ui.base.BaseFragment
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>({ inflater, parent, attach -> FragmentHomeBinding.inflate(inflater, parent, attach) }) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var movieAdapter: MovieAdapter

    override fun onViewInit() {
        setupRecyclerView()
        setupRetryButton()
        observeData()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter()
        movieAdapter.onItemClick = { movie ->
            DetailActivity.start(requireContext(), movie.id, movie.title)
        }

        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 Kolom
            adapter = movieAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    if (dy > 0) { // Hanya cek saat scroll ke bawah
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
        binding.progressBar.isVisible = state.isInitialLoading
        binding.progressLoadMore.isVisible = state.isLoadingMore
        binding.layoutError.isVisible = state.showFullScreenError
        binding.tvEmpty.isVisible = state.showEmpty
        binding.rvMovies.isVisible = state.movies.isNotEmpty()

        binding.tvErrorMessage.text = state.errorMessage
        movieAdapter.submitList(state.movies)

        // Error saat memuat halaman berikutnya (list sudah ada isinya) -> cukup Snackbar
        val errorMessage = state.errorMessage
        if (errorMessage != null && state.movies.isNotEmpty()) {
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                .setAction("Coba Lagi") { viewModel.retry() }
                .show()
            viewModel.consumeError()
        }
    }
}