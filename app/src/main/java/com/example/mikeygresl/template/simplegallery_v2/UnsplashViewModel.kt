package com.example.mikeygresl.template.simplegallery_v2

import android.arch.lifecycle.ViewModel
import android.arch.paging.PagedList
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.example.mikeygresl.template.simplegallery_v2.UnsplashService.SearchQuery
import com.example.mikeygresl.template.simplegallery_v2.UnsplashService.UnsplashSource
import com.example.mikeygresl.template.simplegallery_v2.objects.Photo
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

class UnsplashViewModel : ViewModel() {
  companion object {
    const val APP_ID = "e0ce1e3f6f7ab1c7c6a95910136646b8879b9453fb4188b516f2ae3b229ce4b9"
  }

  private val okHttp = OkHttpClient.Builder()
    .addNetworkInterceptor addKeys@{
      Log.d("requests", it.request().url().toString())

      val modified = it.request().newBuilder().url(it.request().url()
        .newBuilder()
        .addEncodedQueryParameter("client_id", APP_ID)
        .build())

      return@addKeys it.proceed(modified.build())
    }
    .build()

  private val retrofit = Retrofit.Builder()
    .baseUrl("https://api.unsplash.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(okHttp)
    .build()

  private val service = retrofit.create(UnsplashService::class.java)
  private val source = UnsplashSource(service)

  private val pagesWithoutPlaceholders = PagedList.Config.Builder()
    .setPageSize(10)
    .setPrefetchDistance(10)
    .setInitialLoadSizeHint(20)
    .setEnablePlaceholders(false)
    .build()

  private val pagesWithPlaceholders = PagedList.Config.Builder()
    .setPageSize(10)
    .setPrefetchDistance(10)
    .setInitialLoadSizeHint(20)
    .setEnablePlaceholders(true)
    .build()

  private val uiExecutor = Handler(Looper.getMainLooper())
  private val backgroundExecutor = Executors.newFixedThreadPool(2)

  private var currentQuery: SearchQuery? = null

  val photos = PagedList.Builder(source, pagesWithoutPlaceholders)
    .setNotifyExecutor { command -> uiExecutor.post(command) }
    .setFetchExecutor(backgroundExecutor)
    .build()

  fun search(query: String?): PagedList<Photo>? {
    if (TextUtils.isEmpty(query) || query == null) {
      currentQuery = null
      return null
    } else {
      currentQuery = currentQuery?.copy(text = query)
          ?: SearchQuery(query, null, null)
    }

    source.setSearchingQuery(currentQuery)

    return PagedList.Builder(source, pagesWithoutPlaceholders)
      .setNotifyExecutor { command -> uiExecutor.post(command) }
      .setFetchExecutor(backgroundExecutor)
      .build()
  }
}