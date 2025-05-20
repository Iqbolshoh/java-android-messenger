package com.example.mikeygresl.template.simplegallery_v2

import android.arch.paging.PositionalDataSource
import com.example.mikeygresl.template.simplegallery_v2.objects.Photo
import com.example.mikeygresl.template.simplegallery_v2.objects.SearchResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

interface UnsplashService {

  @GET("photos")
  fun photos(
    @Query("page") page: Int,
    @Query("per_page") count: Int = 10,
    @Query("order_by") orderBy: String = "latest"
  ): Call<List<Photo>>

  @GET("search")
  fun search(
    @Query("query") query: String,
    @Query("page") page: Int,
    @Query("per_page") count: Int = 10,
    @Query("orientation") orientation: String? = null,
    @Query("collections") collections: String? = null
  ): Call<SearchResults>

  data class SearchQuery(
    val text: String,
    val orientation: String?,
    val collections: String?
  )

  class UnsplashSource(val service: UnsplashService) : PositionalDataSource<Photo>() {
    private var query: SearchQuery? = null

    fun setSearchingQuery(query: SearchQuery?) {
      this.query = query
    }

    private fun getSearchingPhotos(query: SearchQuery,
      page: Int, size: Int, callback: (Int, List<Photo>) -> Unit) {

      service.search(query.text, page, size, query.orientation, query.collections)
        .enqueue(object : Callback<SearchResults> {
          override fun onFailure(call: Call<SearchResults>, t: Throwable) {
            t.printStackTrace()
          }

          override fun onResponse(call: Call<SearchResults>, response: Response<SearchResults>) {
            val result = response.body() ?: throw IOException(response.errorBody()?.string())
            callback(result.photos.total, result.photos.results)
          }
        })
    }

    private fun getPhotos(page: Int, size: Int, callback: (List<Photo>) -> Unit) {
      service.photos(page, size).enqueue(object : Callback<List<Photo>> {
        override fun onFailure(call: Call<List<Photo>>, t: Throwable) {
          t.printStackTrace()
        }

        override fun onResponse(call: Call<List<Photo>>, response: Response<List<Photo>>) {
          callback(response.body() ?: throw IOException(response.errorBody()?.string()))
        }
      })
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Photo>) {
      val page = params.startPosition / params.loadSize
      query?.let {
        getSearchingPhotos(it, page,
          params.loadSize) { _, data ->
          callback.onResult(data)
        }
      }

      if (query == null) {
        getPhotos(page, params.loadSize, callback::onResult)
      }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Photo>) {
      query?.let {
        getSearchingPhotos(it,
          params.requestedStartPosition,
          params.requestedLoadSize) { items, data ->
          callback.onResult(data, params.requestedStartPosition, items)
        }
      }

      if (query == null) {
        getPhotos(params.requestedStartPosition, params.requestedLoadSize) {
          callback.onResult(it, params.requestedStartPosition)
        }
      }
    }

  }

}
