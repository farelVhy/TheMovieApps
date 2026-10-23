package id.co.josal.merchant.themovieapps.ui.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import id.co.josal.merchant.themovieapps.api.model.Review
import id.co.josal.merchant.themovieapps.databinding.ItemReviewBinding
import id.co.josal.merchant.themovieapps.helper.loadAvatar
import java.util.Locale

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val reviews = mutableListOf<Review>()

    // Menyimpan ID ulasan yang sedang dibuka penuh (expand)
    private val expandedIds = mutableSetOf<String>()

    fun submitList(newReviews: List<Review>) {
        reviews.clear()
        reviews.addAll(newReviews)
        notifyDataSetChanged()
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            val details = review.authorDetails

            binding.ivAvatar.loadAvatar(details.avatarPath)
            binding.tvAuthor.text = review.author.ifBlank { details.username }
            binding.tvDate.text = review.createdAt.take(10) // "2025-07-20T10:15:30Z" -> "2025-07-20"

            // Rating ulasan sering null, sembunyikan jika tidak ada
            val rating = details.rating
            binding.tvRating.isVisible = rating != null
            binding.tvRating.text = String.Companion.format(Locale.US, "★ %.0f/10", rating ?: 0.0)

            // Ulasan panjang dipotong 4 baris, tap untuk membuka penuh
            val isLongContent = review.content.length > 250
            val isExpanded = expandedIds.contains(review.id)
            binding.tvContent.text = review.content
            binding.tvContent.maxLines = if (isExpanded || !isLongContent) Int.MAX_VALUE else 4
            binding.tvReadMore.isVisible = isLongContent
            binding.tvReadMore.text = if (isExpanded) "Tutup" else "Baca selengkapnya"

            binding.root.setOnClickListener {
                if (!isLongContent) return@setOnClickListener
                if (isExpanded) expandedIds.remove(review.id) else expandedIds.add(review.id)
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size
}