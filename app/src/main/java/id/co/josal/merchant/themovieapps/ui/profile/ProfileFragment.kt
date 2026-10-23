package id.co.josal.merchant.themovieapps.ui.profile

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import id.co.josal.merchant.themovieapps.base.BaseFragment
import id.co.josal.merchant.themovieapps.databinding.FragmentProfileBinding

/**
 * Halaman profil. Untuk sementara berisi placeholder,
 * konten akan ditambahkan di tahap berikutnya.
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding>({ inflater, parent, attach -> FragmentProfileBinding.inflate(inflater, parent, attach) }) {

    override fun onViewInit() {
        setupWindowInsets()
    }

    private fun setupWindowInsets() {
        // Edge-to-edge: background bar atas memanjang sampai ke belakang status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutTopBar) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }
    }
}