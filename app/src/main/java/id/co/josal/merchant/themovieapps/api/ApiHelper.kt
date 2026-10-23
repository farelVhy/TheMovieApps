import id.co.josal.merchant.themovieapps.api.ApiClient
import id.co.josal.merchant.themovieapps.api.service.MasterService
import id.co.josal.merchant.themovieapps.helper.Resource
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

object ApiHelper {
    suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> = try {
        Resource.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Resource.Error("Tidak ada koneksi internet. Periksa jaringan Anda.")
    } catch (e: HttpException) {
        Resource.Error(
            when (e.code()) {
                401 -> "API key tidak valid"
                404 -> "Data tidak ditemukan"
                in 500..599 -> "Server sedang bermasalah, coba lagi nanti"
                else -> "Terjadi kesalahan (${e.code()})"
            }
        )
    } catch (e: Exception) {
        Resource.Error("Terjadi kesalahan tak terduga")
    }
}

class MovieRepository(private val service: MasterService = ApiClient.service) {
    suspend fun getNowPlaying(page: Int) = ApiHelper.safeApiCall { service.getNowPlaying(page) }
}