package id.co.josal.merchant.themovieapps.ui.main

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.co.josal.merchant.themovieapps.databinding.ActivityMainBinding
import id.co.josal.merchant.themovieapps.ui.main.homefragment.HomeFragment
import id.co.themovieapps.ui.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge: header HomeFragment digambar sampai ke belakang status bar.
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
    }

    private fun setupViewPager() {
        // Membuat Adapter untuk ViewPager
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 1 // Untuk sementara hanya 1 halaman (Home)

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> HomeFragment()
                    else -> throw IllegalStateException("Invalid position")
                }
            }
        }

        // Memasang adapter ke ViewPager2
        binding.vpMain.adapter = pagerAdapter

        // Nonaktifkan swipe, navigasi hanya lewat BottomNav
        binding.vpMain.isUserInputEnabled = false
    }
}