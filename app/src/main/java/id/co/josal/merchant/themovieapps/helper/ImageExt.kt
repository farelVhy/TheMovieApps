package id.co.josal.merchant.themovieapps.helper

import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import id.co.josal.merchant.themovieapps.R

private const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

fun ImageView.loadPoster(posterPath: String?, size: String = "w500") {
    load(posterPath?.let { "$TMDB_IMAGE_BASE_URL$size$it" }) {
        crossfade(true)
        placeholder(R.color.tmdb_light_blue)
        error(R.color.tmdb_light_blue)
    }
}

fun ImageView.loadAvatar(avatarPath: String?) {
    // TMDB punya quirk: avatar_path kadang berisi URL Gravatar penuh yang diawali "/"
    // contoh: "/https://secure.gravatar.com/avatar/abc.jpg"
    val url = when {
        avatarPath.isNullOrBlank() -> null
        avatarPath.startsWith("/http") -> avatarPath.removePrefix("/")
        else -> "${TMDB_IMAGE_BASE_URL}w185$avatarPath"
    }
    load(url) {
        crossfade(true)
        placeholder(R.drawable.ic_person)
        error(R.drawable.ic_person)
        transformations(CircleCropTransformation())
    }
}