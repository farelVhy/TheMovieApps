package id.co.josal.merchant.themovieapps.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.co.josal.merchant.themovieapps.api.model.Movie
import id.co.josal.merchant.themovieapps.databinding.ItemMovieBinding
import id.co.josal.merchant.themovieapps.helper.loadPoster

class MovieAdapter : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    private val movies = mutableListOf<Movie>()
    var onItemClick: ((Movie) -> Unit)? = null

    fun submitList(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    inner class MovieViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.ivPoster.loadPoster(movie.posterPath)
            binding.tvTitle.text = movie.title
            binding.tvOverview.text = movie.overview
            binding.root.setOnClickListener { onItemClick?.invoke(movie) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size
}