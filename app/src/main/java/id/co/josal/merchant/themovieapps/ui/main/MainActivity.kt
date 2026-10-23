package id.co.josal.merchant.themovieapps.ui.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.co.josal.merchant.themovieapps.databinding.ActivityMainBinding
import id.co.josal.merchant.themovieapps.ui.main.homefragment.HomeFragment // Sesuaikan package HomeFragment Anda
import id.co.themovieapps.ui.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) {

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

        // Opsional: Nonaktifkan swipe jika Anda ingin navigasi hanya lewat BottomNav
        binding.vpMain.isUserInputEnabled = false
    }
}