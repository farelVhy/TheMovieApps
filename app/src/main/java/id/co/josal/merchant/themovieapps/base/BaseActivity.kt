package id.co.josal.merchant.themovieapps.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(
    private val inflate: (layoutInflater: LayoutInflater) -> VB
) : AppCompatActivity() {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)

        // Setup awal di Child Activity
        onViewInit()
    }

    // Wajib diimplementasikan di Child Activity
    abstract fun onViewInit()
}