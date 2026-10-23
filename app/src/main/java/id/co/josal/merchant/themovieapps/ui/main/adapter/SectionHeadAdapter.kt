package id.co.josal.merchant.themovieapps.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.co.josal.merchant.themovieapps.databinding.ItemSectionHeaderBinding

/**
 * Adapter berisi SATU item judul section, digabung dengan MovieAdapter lewat ConcatAdapter.
 * Dengan begitu judul berada di dalam grid dan ikut ter-scroll bersama poster.
 */
class SectionHeaderAdapter : RecyclerView.Adapter<SectionHeaderAdapter.HeaderViewHolder>() {

    private var title = ""
    private var subtitle = ""

    fun setHeader(title: String, subtitle: String) {
        // Hindari bind ulang jika teks tidak berubah
        if (this.title == title && this.subtitle == subtitle) return
        this.title = title
        this.subtitle = subtitle
        notifyItemChanged(0)
    }

    inner class HeaderViewHolder(private val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvSectionTitle.text = title
            binding.tvSectionSubtitle.text = subtitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val binding = ItemSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1
}