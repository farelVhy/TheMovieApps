package id.co.josal.merchant.themovieapps.ui.maintenance

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import id.co.josal.merchant.themovieapps.R
import id.co.josal.merchant.themovieapps.base.BaseActivity
import id.co.josal.merchant.themovieapps.databinding.ActivityMaintenanceBinding

/**
 * Halaman untuk fitur yang masih dalam pengerjaan.
 * Bisa dipakai ulang untuk fitur apa saja: cukup kirim nama fiturnya lewat start().
 */
class MaintenanceActivity : BaseActivity<ActivityMaintenanceBinding>({ ActivityMaintenanceBinding.inflate(it) }) {

    private var wobbleAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
    }

    override fun onViewInit() {
        val featureName = intent.getStringExtra(EXTRA_FEATURE_NAME).orEmpty()

        setupWindowInsets()

        binding.toolbar.title = featureName
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvMessage.text = getString(R.string.maintenance_message, featureName)
        binding.btnBackHome.setOnClickListener { finish() }

        startWobbleAnimation()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.layoutTopBar.updatePadding(top = bars.top)
            binding.root.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            insets
        }
    }

    // Ikon kunci pas bergoyang pelan supaya halaman terasa "hidup", bukan halaman error
    private fun startWobbleAnimation() {
        wobbleAnimator = ObjectAnimator.ofFloat(binding.ivMaintenanceIcon, View.ROTATION, -12f, 12f).apply {
            duration = 900
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    override fun onDestroy() {
        wobbleAnimator?.cancel() // Hentikan animasi tak terbatas agar tidak bocor memori
        wobbleAnimator = null
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_FEATURE_NAME = "extra_feature_name"

        fun start(context: Context, featureName: String) {
            val intent = Intent(context, MaintenanceActivity::class.java).apply {
                putExtra(EXTRA_FEATURE_NAME, featureName)
            }
            context.startActivity(intent)
        }
    }
}