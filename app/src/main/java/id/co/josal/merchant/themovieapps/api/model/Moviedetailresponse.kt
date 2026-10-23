package id.co.josal.merchant.themovieapps.api.model

import com.google.gson.annotations.SerializedName

data class MovieDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?, // Gambar besar untuk header detail
    @SerializedName("release_date") val releaseDate: String, // Format "yyyy-MM-dd"
    @SerializedName("runtime") val runtime: Int?, // Durasi dalam menit, bisa null
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    @SerializedName("genres") val genres: List<Genre>
)

data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)