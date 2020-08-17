package com.example.chatappfinal.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.example.chatappfinal.R
import com.example.chatappfinal.presentation.features.chat.FILES_CODE
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

const val UNKNOWN_ERROR = "UN KNOWN ERROR"

fun TextInputLayout.text() = editText?.text.toString()

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.disable() {
    isEnabled = false
}

fun View.enable() {
    isEnabled = true
}

fun ViewGroup.inflateView(layout: Int): View = LayoutInflater
    .from(context)
    .inflate(layout, this, false)

fun MenuItem.onClicked(onClicked: () -> Unit): MenuItem =
    setOnMenuItemClickListener { onClicked();true }

fun ImageView.loadPhoto(url: String?) = Glide.with(context)
    .load(url)
    .placeholder(R.drawable.ic_person)
    .into(this)

@SuppressLint("SimpleDateFormat")
fun formatDate(date: Long): String = SimpleDateFormat("hh:mm aa")
    .format(Date(date))

fun Fragment.openPhotos() = Intent(Intent.ACTION_GET_CONTENT)
    .apply {
        type = "image/*"
        putExtra(Intent.EXTRA_LOCAL_ONLY, true)
    }.let { startActivityForResult(it, FILES_CODE) }

fun Context.makeToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun View.createSnackBar(message: String) = Snackbar.make(
    this,
    message,
    Snackbar.LENGTH_LONG
).show()

class SoftKeyboardListener(
    private val view: View,
    lifecycleOwner: LifecycleOwner,
    private val rect: Rect = Rect(),
    private val layoutListener: OnGlobalLayoutListener? = OnGlobalLayoutListener {
        view.getWindowVisibleDisplayFrame(rect)
        isVisible(view.rootView.height - rect.bottom > view.rootView.height * 0.15)
    },
    private val inputMethodManager: InputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager,
    private val isVisible: (Boolean) -> Unit
) : LifecycleEventObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = when (event) {
        Lifecycle.Event.ON_RESUME -> view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        Lifecycle.Event.ON_PAUSE -> doOnPause()
        else -> Unit
    }

    private fun doOnPause() {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        isVisible(false)
    }

}

fun typingListener(isTyping: (Int) -> Unit = {}): TextWatcher = object : TextWatcher {
    override fun afterTextChanged(p0: Editable?) = Unit
    override fun beforeTextChanged(s:CharSequence,start:Int, count:Int,  after:Int){
    }
    override fun onTextChanged(s:CharSequence ,start:Int,before:Int, count:Int) {
        isTyping(s.length)
    }
}