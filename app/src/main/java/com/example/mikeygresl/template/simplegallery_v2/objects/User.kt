package com.example.mikeygresl.template.simplegallery_v2.objects

import com.google.gson.annotations.SerializedName

data class User(
  val id: String,
  val username: String,
  val name: String,
  @SerializedName("portfolio_url")
  val portfolioUrl: String,
  @SerializedName("profile_image")
  val profileImage: ImageUrls
)