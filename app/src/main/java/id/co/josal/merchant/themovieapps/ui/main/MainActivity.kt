package id.co.josal.merchant.themovieapps.ui.main

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.co.josal.merchant.themovieapps.R
import id.co.josal.merchant.themovieapps.base.BaseActivity
import id.co.josal.merchant.themovieapps.databinding.ActivityMainBinding
import id.co.josal.merchant.themovieapps.ui.main.homefragment.HomeFragment
import id.co.josal.merchant.themovieapps.ui.maintenance.MaintenanceActivity
import id.co.josal.merchant.themovieapps.ui.profile.ProfileFragment

class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge: header tiap halaman digambar sampai ke belakang status bar.
        // SystemBarStyle.dark -> ikon status bar putih karena background kita gelap.
        // BottomNavigationView otomatis menambah padding bawah setinggi navigation bar.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
    }

    override fun onViewInit() {
        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        binding.vpMain.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = PAGE_COUNT

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    PAGE_HOME -> HomeFragment()
                    PAGE_PROFILE -> ProfileFragment()
                    else -> throw IllegalStateException("Invalid position: $position")
                }
            }
        }

        // Nonaktifkan swipe, navigasi hanya lewat BottomNav
        binding.vpMain.isUserInputEnabled = false
    }

    private fun setupBottomNavigation() {
        binding.bnvMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_film -> {
                    binding.vpMain.setCurrentItem(PAGE_HOME, false)
                    true
                }
                R.id.menu_profile -> {
                    binding.vpMain.setCurrentItem(PAGE_PROFILE, false)
                    true
                }
                R.id.menu_schedule -> {
                    // Jadwal tayang belum tersedia -> buka halaman maintenance.
                    // Return false: tab yang aktif tidak berpindah, tetap di tab sebelumnya.
                    MaintenanceActivity.start(this, getString(R.string.menu_schedule))
                    false
                }
                else -> false
            }
        }
    }

    companion object {
        private const val PAGE_HOME = 0
        private const val PAGE_PROFILE = 1
        private const val PAGE_COUNT = 2
    }
}