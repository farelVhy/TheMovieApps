package id.co.josal.merchant.themovieapps.api.model

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("id") val id: Int, // ID film
    @SerializedName("results") val results: List<Video> // Tidak ada pagination di endpoint ini
)

data class Video(
    @SerializedName("id") val id: String,
    @SerializedName("key") val key: String, // ID video YouTube, dipakai untuk membuka trailer
    @SerializedName("name") val name: String,
    @SerializedName("site") val site: String, // "YouTube", "Vimeo"
    @SerializedName("type") val type: String, // "Trailer", "Teaser", "Clip", dll
    @SerializedName("official") val official: Boolean
)