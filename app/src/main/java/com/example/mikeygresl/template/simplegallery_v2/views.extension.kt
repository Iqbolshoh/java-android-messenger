package com.example.mikeygresl.template.simplegallery_v2

import android.support.v4.view.ViewCompat
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener

val wrap = ViewGroup.LayoutParams.WRAP_CONTENT
val fill = ViewGroup.LayoutParams.MATCH_PARENT

inline fun View.onLaidOut(crossinline run: View.() -> Unit) {
  if (this.width > 0 && this.height > 0) run() else {
    viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        run()
        viewTreeObserver.removeOnGlobalLayoutListener(this)
      }
    })
  }
}

fun View.dp(value: Float): Int {
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()
}