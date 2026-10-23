package id.co.themovieapps.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(private val inflate: (layoutInflater: android.view.LayoutInflater) -> VB) : AppCompatActivity() {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)

        // Panggil method abstract untuk setup awal di Child Activity
        onViewInit()
    }

    // Method yang wajib diimplementasikan di Child Activity
    abstract fun onViewInit()
}