package com.example.mikeygresl.template.simplegallery_v2.objects

import com.google.gson.annotations.SerializedName

data class FoundPhotos(
  val total: Int,
  @SerializedName("total_pages")
  val totalPages: Int,
  val results: List<Photo>
)
