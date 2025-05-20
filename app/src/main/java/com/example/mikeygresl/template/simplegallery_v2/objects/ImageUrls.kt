package com.example.mikeygresl.template.simplegallery_v2.objects

import android.text.TextUtils

data class ImageUrls(
  val raw: String?,
  val full: String?,
  val regular: String,
  val small: String,
  val thumb: String?
) {

  fun suggestBestQualityImage(): String {
    return when {
      !TextUtils.isEmpty(full) -> full!!
      !TextUtils.isEmpty(raw) -> raw!!
      else -> regular
    }
  }

  fun suggestSmallImage(): String {
    return when {
      !TextUtils.isEmpty(thumb) -> thumb!!
      !TextUtils.isEmpty(small) -> small
      else -> regular
    }
  }
}