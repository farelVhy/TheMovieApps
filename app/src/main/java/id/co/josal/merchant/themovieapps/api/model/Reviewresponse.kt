package id.co.josal.merchant.themovieapps.api.model

import com.google.gson.annotations.SerializedName

data class ReviewResponse(
    @SerializedName("id") val id: Int, // ID film
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<Review>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

data class Review(
    @SerializedName("id") val id: String, // ID review berupa String, bukan Int
    @SerializedName("author") val author: String,
    @SerializedName("author_details") val authorDetails: AuthorDetails,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String // Format "2025-07-20T10:15:30.123Z"
)

data class AuthorDetails(
    @SerializedName("username") val username: String,
    @SerializedName("avatar_path") val avatarPath: String?, // Bisa null
    @SerializedName("rating") val rating: Double? // Skala 0-10, sering null
)