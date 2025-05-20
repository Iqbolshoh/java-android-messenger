package com.example.mikeygresl.template.simplegallery_v2.objects

data class Photo(
  val id: String,
  val width: Int,
  val height: Int,
  val likes: Int,
  val color: String,
  val description: String,
  val user: User,
  val urls: ImageUrls
)