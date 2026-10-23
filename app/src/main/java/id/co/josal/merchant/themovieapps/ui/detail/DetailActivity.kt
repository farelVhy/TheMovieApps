package id.co.josal.merchant.themovieapps.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import id.co.josal.merchant.themovieapps.R
import id.co.josal.merchant.themovieapps.api.model.Genre
import id.co.josal.merchant.themovieapps.api.model.MovieDetailResponse
import id.co.josal.merchant.themovieapps.databinding.ActivityDetailBinding
import id.co.josal.merchant.themovieapps.helper.loadPoster
import id.co.josal.merchant.themovieapps.ui.detail.adapter.ReviewAdapter
import id.co.josal.merchant.themovieapps.ui.detail.viewmodel.DetailUiState
import id.co.josal.merchant.themovieapps.ui.detail.viewmodel.DetailViewModel
import id.co.themovieapps.ui.base.BaseActivity
import kotlinx.coroutines.launch
import java.util.Locale

class DetailActivity : BaseActivity<ActivityDetailBinding>({ ActivityDetailBinding.inflate(it) }) {

    private val viewModel: DetailViewModel by viewModels()
    private val reviewAdapter = ReviewAdapter()

    private var isPlayerInitialized = false
    private var isPlayerFailed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge: konten digambar sampai ke belakang status bar & navigation bar.
        // SystemBarStyle.dark -> ikon status bar berwarna putih karena background kita gelap.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
    }

    override fun onViewInit() {
        val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, 0)
        val movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE).orEmpty()

        // Negative case: ID tidak valid, tutup halaman
        if (movieId == 0) {
            Toast.makeText(this, "Film tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWindowInsets()
        setupToolbar(movieTitle)
        setupReviewList()
        setupYoutubePlayer()
        binding.btnRetry.setOnClickListener { viewModel.retry() }

        observeData()
        viewModel.loadDetail(movieId)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Bar atas mendapat padding setinggi status bar -> background bar terlihat sampai paling atas,
            // tapi tombol back & judul tetap di bawah status bar
            binding.layoutTopBar.updatePadding(top = bars.top)
            // Konten paling bawah tidak tertutup navigation bar
            binding.scrollContent.updatePadding(bottom = bars.bottom)
            // Mode landscape: hindari konten tertutup notch / navigation bar samping
            binding.root.updatePadding(left = bars.left, right = bars.right)
            insets
        }
    }

    private fun setupToolbar(title: String) {
        // Judul langsung tampil dari data grid, sambil menunggu detail lengkap dari API
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupReviewList() {
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(this@DetailActivity)
            adapter = reviewAdapter
        }
    }

    private fun setupYoutubePlayer() {
        // Player otomatis pause saat Activity di-background dan di-release saat Activity dihancurkan
        lifecycle.addObserver(binding.youtubePlayerView)
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: DetailUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.layoutError.isVisible = state.errorMessage != null
        binding.tvErrorMessage.text = state.errorMessage
        binding.groupContent.isVisible = state.detail != null

        val detail = state.detail ?: return
        renderInfo(detail)
        renderTrailer(state)
        renderReviews(state)
    }

    // ===== INFO UTAMA =====

    private fun renderInfo(detail: MovieDetailResponse) {
        binding.toolbar.title = detail.title
        binding.ivPoster.loadPoster(detail.posterPath, size = "w342")
        binding.tvTitle.text = detail.title

        // "2025 • 2j 17m" (bagian yang kosong tidak ditampilkan)
        binding.tvMeta.text = listOf(detail.releaseDate.take(4), formatRuntime(detail.runtime))
            .filter { it.isNotBlank() }
            .joinToString(" • ")

        binding.tvRating.text = String.format(Locale.US, "★ %.1f", detail.voteAverage)
        binding.tvVoteCount.text = "(${detail.voteCount} suara)"

        binding.tvTagline.isVisible = !detail.tagline.isNullOrBlank()
        binding.tvTagline.text = "\"${detail.tagline}\""

        binding.tvOverview.text = detail.overview.ifBlank { "Sinopsis belum tersedia." }

        renderGenres(detail.genres)
    }

    private fun renderGenres(genres: List<Genre>) {
        binding.chipGroupGenre.removeAllViews()
        binding.chipGroupGenre.isVisible = genres.isNotEmpty()

        val strokeColor = ContextCompat.getColor(this, R.color.tmdb_light_blue)
        val backgroundColor = ContextCompat.getColor(this, R.color.tmdb_bar)

        genres.forEach { genre ->
            val chip = Chip(this).apply {
                text = genre.name
                isClickable = false
                setTextColor(Color.WHITE)
                chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
                chipStrokeColor = ColorStateList.valueOf(strokeColor)
                chipStrokeWidth = resources.displayMetrics.density // 1dp
            }
            binding.chipGroupGenre.addView(chip)
        }
    }

    // 137 -> "2j 17m", null/0 -> ""
    private fun formatRuntime(runtime: Int?): String {
        if (runtime == null || runtime <= 0) return ""
        val hours = runtime / 60
        val minutes = runtime % 60
        return if (hours > 0) "${hours}j ${minutes}m" else "${minutes}m"
    }

    // ===== TRAILER =====

    private fun renderTrailer(state: DetailUiState) {
        binding.ivBackdrop.loadPoster(state.detail?.backdropPath, size = "w780")

        // Jika player sudah gagal, pertahankan tampilan fallback "Tonton di YouTube"
        if (isPlayerFailed) return

        val trailer = state.trailer
        if (trailer != null) {
            binding.youtubePlayerView.isVisible = true
            binding.layoutTrailerMessage.isVisible = false
            initYoutubePlayer(trailer.key)
        } else {
            // Negative case: tidak ada trailer / gagal dimuat -> tampilkan backdrop + pesan
            binding.youtubePlayerView.isVisible = false
            binding.layoutTrailerMessage.isVisible = state.trailerMessage != null
            binding.tvTrailerMessage.text = state.trailerMessage
            binding.btnOpenYoutube.isVisible = false
        }
    }

    private fun initYoutubePlayer(videoKey: String) {
        // initialize() hanya boleh dipanggil sekali per YouTubePlayerView
        if (isPlayerInitialized) return
        isPlayerInitialized = true

        val options = IFramePlayerOptions.Builder(this)
            .controls(1)
            .build()

        binding.youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                // cueVideo: video siap tapi tidak autoplay, user yang menekan play
                youTubePlayer.cueVideo(videoKey, 0f)
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                // Negative case: video tidak bisa diputar di dalam app (dibatasi pemilik video, dll)
                showTrailerFallback(videoKey)
            }
        }, options)
    }

    private fun showTrailerFallback(videoKey: String) {
        isPlayerFailed = true
        binding.youtubePlayerView.isVisible = false
        binding.layoutTrailerMessage.isVisible = true
        binding.tvTrailerMessage.text = "Trailer tidak bisa diputar di aplikasi"
        binding.btnOpenYoutube.isVisible = true
        binding.btnOpenYoutube.setOnClickListener { openYoutube(videoKey) }
    }

    private fun openYoutube(videoKey: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoKey"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoKey"))
        try {
            startActivity(appIntent) // Buka di aplikasi YouTube
        } catch (e: ActivityNotFoundException) {
            startActivity(webIntent) // Aplikasi YouTube tidak terpasang -> buka di browser
        }
    }

    // ===== ULASAN =====

    private fun renderReviews(state: DetailUiState) {
        binding.tvReviewTitle.text =
            if (state.totalReviews > 0) "Ulasan (${state.totalReviews})" else "Ulasan"

        reviewAdapter.submitList(state.reviews)
        binding.rvReviews.isVisible = state.reviews.isNotEmpty()

        binding.tvReviewMessage.isVisible = state.reviewMessage != null
        binding.tvReviewMessage.text = state.reviewMessage
    }

    companion object {
        private const val EXTRA_MOVIE_ID = "extra_movie_id"
        private const val EXTRA_MOVIE_TITLE = "extra_movie_title"

        // Satu pintu untuk membuka DetailActivity, supaya key extra tidak tersebar di banyak tempat
        fun start(context: Context, movieId: Int, movieTitle: String) {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_MOVIE_ID, movieId)
                putExtra(EXTRA_MOVIE_TITLE, movieTitle)
            }
            context.startActivity(intent)
        }
    }
}