package com.example.mikeygresl.template.simplegallery_v2

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.graphics.ColorUtils
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.mikeygresl.template.R
import com.example.mikeygresl.template.simplegallery_v2.objects.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import java.lang.Exception

class GalleryActivity : AppCompatActivity() {
  companion object {
    fun <T : Activity> Context.getActivity(): T {
      if (this is Activity) {
        return this as T;
      } else if (this is ContextWrapper) {
        return baseContext.getActivity();
      } else {
        throw IllegalArgumentException("Not an activity context")
      }
    }
  }

  private val galleryView = PhotosGalleryUi()
  private val fullscreenPhotoView by lazy { FullscreenPhotoView(this) }

  private val controller: UnsplashViewModel by lazy {
    ViewModelProviders.of(this)
      .get(UnsplashViewModel::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setBackgroundDrawable(null)
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

    galleryView.create(this)
    galleryView.setPhotos(controller.photos)

    setContentView(LinearLayout(this).also { parent ->
      parent.orientation = LinearLayout.VERTICAL

      parent.setBackgroundColor(0xFFEAEAEA.toInt())
      parent.addView(AppCompatEditText(this).apply {
        hint = "Search for image..."
        minHeight = dp(56f)
        textSize = 14f
        typeface = Typeface.MONOSPACE
        inputType = EditorInfo.TYPE_CLASS_TEXT
        imeOptions = EditorInfo.IME_ACTION_SEARCH

        val padding = dp(8f)

        layoutParams = LinearLayout.LayoutParams(fill, wrap).also {
          it.setMargins(padding, padding, padding, padding * 2)
        }

        setPadding(padding * 2, padding, padding * 2, padding)
        setBackgroundColor(Color.WHITE)

        ViewCompat.setElevation(this, dp(4f).toFloat())

        setOnEditorActionListener search@{ v, actionId, event ->
          if (actionId == EditorInfo.IME_ACTION_SEARCH
              || event.keyCode == KeyEvent.KEYCODE_ENTER) {

            v.clearFocus()

            val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, 0)

            galleryView.setPhotos(null)
            galleryView.setPhotos(controller.search(v.text.toString()) ?: controller.photos)
            return@search true
          }
          return@search false
        }
      })
      parent.addView(galleryView.getRoot().also {
        it.isFocusableInTouchMode = true
        it.requestFocusFromTouch()
      })
    })
  }

  override fun onBackPressed() {
    if (fullscreenPhotoView.isAttached()) {
      fullscreenPhotoView.detach()
    } else {
      super.onBackPressed()
    }
  }

  class PhotosGalleryUi {
    private lateinit var gridView: RecyclerView
    private lateinit var adapter: PhotosAdapter
    private var data: PagedList<Photo>? = null

    fun getRoot(): View = gridView

    fun create(context: Context) {
      gridView = RecyclerView(context).also {
        it.layoutManager = GridLayoutManager(context, 2)
        it.addOnScrollListener(object : OnScrollListener() {
          override fun onScrolled(parent: RecyclerView, dx: Int, dy: Int) {
            data?.let { pages ->
              if (pages.isEmpty()) {
                return
              }

              // When pages available
              if (pages.config.pageSize * 2 > adapter.itemCount - getScrolledItemsCount()) {
                pages.loadAround(pages.lastKey as Int + 1)
              }
            }
          }
        })
      }
    }

    private fun getScrolledItemsCount(): Int {
      val lm = gridView.layoutManager
      return when (lm) {
        is GridLayoutManager -> lm.findLastVisibleItemPosition()
        is LinearLayoutManager -> lm.findLastVisibleItemPosition()
        else -> {
          val target = gridView.getChildAt(gridView.childCount - 1)
          gridView.getChildAdapterPosition(target)
        }
      }
    }

    fun setPhotos(data: PagedList<Photo>?) {
      this.data = data

      if (gridView.adapter == null) {
        adapter = PhotosAdapter()
        gridView.adapter = adapter
      }

      adapter.submitList(data)
    }
  }

  class PhotosAdapter : PagedListAdapter<Photo, PhotoHolder> {
    constructor() : super(object : DiffUtil.ItemCallback<Photo>() {
      override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem == newItem
      }

      override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem.id == newItem.id
      }
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
      return PhotoHolder.create(parent, 300)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
      val photo = getItem(position) ?: throw NullPointerException("photo is null")

      holder.bind(photo)
      holder.photoView.setOnClickListener {
        val activity = it.context as GalleryActivity
        activity.fullscreenPhotoView.attachTo(activity)
        activity.fullscreenPhotoView.setPhoto(photo)
      }
    }

    override fun onViewDetachedFromWindow(holder: PhotoHolder) {
      super.onViewDetachedFromWindow(holder)
      holder.photoView.setOnClickListener(null)

      Picasso.get().cancelRequest(holder.photoView.photo)
    }

    override fun onViewRecycled(holder: PhotoHolder) {
      super.onViewRecycled(holder)
      Picasso.get().cancelRequest(holder.photoView.photo)
    }
  }

  class PhotoHolder(val photoView: PhotoView) : ViewHolder(photoView) {

    fun bind(photo: Photo) {
      photoView.title.movementMethod = LinkMovementMethod.getInstance()
      photoView.title.text = SpannableString(photo.user.name).apply {
        val openLink = object : ClickableSpan() {
          override fun onClick(view: View) {
            view.context.startActivity(Intent(Intent.ACTION_VIEW)
              .setData(Uri.parse(photo.user.portfolioUrl)))
          }
        }
        setSpan(openLink, 0, length, 0)
        setSpan(UnderlineSpan(), 0, length, 0)
        setSpan(ForegroundColorSpan(Color.WHITE), 0, length, 0)
      }

      val placeholder = if (!TextUtils.isEmpty(photo.color)) {
        ColorDrawable(Color.parseColor(photo.color))
      } else {
        ColorDrawable(Color.BLACK)
      }

      photoView.photo.onLaidOut {
        Picasso.get().load(photo.urls.suggestSmallImage())
          .placeholder(placeholder)
          .centerCrop()
          .resize(width, height)
          .into(photoView.photo)
      }
    }

    companion object {
      fun create(parent: ViewGroup, height: Int): PhotoHolder {
        return PhotoHolder(PhotoView(parent.context).also {
          it.layoutParams = ViewGroup.MarginLayoutParams(fill, height)
        })
      }
    }
  }

  class PhotoView(context: Context) : ViewGroup(context) {

    private val textLayerMask = GradientDrawable(
      GradientDrawable.Orientation.TOP_BOTTOM,
      intArrayOf(Color.TRANSPARENT, Color.BLACK.alpha(0.3f))
    )

    private fun Int.alpha(value: Float): Int {
      return ColorUtils.setAlphaComponent(this, (value * 255).toInt())
    }

    val photo = AppCompatImageView(context)
    val title = AppCompatTextView(context)

    private val tmpRect = Rect()

    init {
      title.textSize = 16f
      title.setTextColor(Color.WHITE)
      title.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL)

      title.layoutParams = LayoutParams(fill, wrap)
      photo.layoutParams = LayoutParams(fill, fill)

      addView(photo)
      addView(title)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
      tmpRect.set(0, 0, r - l, b - t)

      textLayerMask.bounds = tmpRect

      Gravity.apply(Gravity.RIGHT or Gravity.BOTTOM,
        title.measuredWidth, title.measuredHeight, textLayerMask.bounds, tmpRect)

      photo.layout(0, 0, r - l, b - t)
      title.layout(tmpRect.left, tmpRect.top, tmpRect.right, tmpRect.bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
      measureChildren(widthMeasureSpec, heightMeasureSpec)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
      if (child == title)
        textLayerMask.draw(canvas)

      return super.drawChild(canvas, child, drawingTime)
    }
  }

  class FullscreenPhotoView(context: Context) : FrameLayout(context) {

    val progress = ProgressBar(context, null, android.R.attr.progressBarStyleSmall)
    val photo = AppCompatImageView(context)

    val target = object : Target {
      override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        photo.setImageDrawable(placeHolderDrawable)
      }

      override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        progress.visibility = View.GONE
      }

      override fun onBitmapLoaded(bitmap: Bitmap?, from: LoadedFrom?) {
        progress.visibility = View.GONE
        photo.setImageBitmap(bitmap)
      }
    }

    init {
      layoutParams = LayoutParams(fill, fill)
      setBackgroundColor(Color.BLACK)

      progress.layoutParams = FrameLayout.LayoutParams(dp(56f), dp(56f), Gravity.CENTER)
      progress.indeterminateTintList = ColorStateList.valueOf(Color.LTGRAY)
      progress.setInterpolator(FastOutSlowInInterpolator())

      val select = TextView(context)
      TextViewCompat.setTextAppearance(select, R.style.TextAppearance_AppCompat_Body1)
      select.setText("select")
      select.setTextColor(Color.WHITE)
      select.isAllCaps = true
      select.setOnClickListener {
        val photo = getTag() as Photo

        val activity = it.context.getActivity<GalleryActivity>()
        activity.setResult(Activity.RESULT_OK, Intent().setData(Uri.parse(photo.urls.regular)))
        activity.finish()
      }

      photo.layoutParams = LayoutParams(fill, fill)

      isClickable = true

      addView(photo)
      addView(progress)
      addView(select, FrameLayout.LayoutParams(wrap, wrap).apply {
        gravity = Gravity.RIGHT or Gravity.TOP
      })
    }

    fun isAttached(): Boolean {
      return parent != null
    }

    fun detach() {
      (parent as ViewGroup).removeView(this)
    }

    fun attachTo(activity: Activity) {
      if (isAttached())
        detach()

      activity.findViewById<FrameLayout>(android.R.id.content)
        .addView(this)
    }

    fun setPhoto(photo: Photo) {
      setTag(photo)

      this.photo.setImageDrawable(null)
      this.progress.visibility = View.VISIBLE

      Picasso.get().cancelRequest(target)

      if (TextUtils.isEmpty(photo.color)) {
        background = ColorDrawable(Color.BLACK)
      } else {
        background = ColorDrawable(Color.parseColor(photo.color))
      }

      onLaidOut {
        val height = 1f * width / photo.width * photo.height

        Picasso.get().load(photo.urls.suggestBestQualityImage())
          .centerInside()
          .resize(width, height.toInt())
          .into(target)
      }
    }
  }
}
