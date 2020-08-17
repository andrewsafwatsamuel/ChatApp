package com.example.chatappfinal.presentation.features.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.chatappfinal.R
import kotlinx.android.synthetic.main.dialog_delete.*

const val CANCEL_DELETE = 0
const val DELETE = 1
const val DELETE_TOTALLY = 2

class DeleteDialog : DialogFragment() {

    private val deleteTotally by lazy { MutableLiveData<Int>() }
    private var isMyMessage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_delete, container, false)

    fun setState(isMyMessage: Boolean) {
        this.isMyMessage = isMyMessage
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancel_textView.setOnClickListener { deleteTotally.value = CANCEL_DELETE;dismiss() }
        delete_textView.setOnClickListener { deleteTotally.value = DELETE;dismiss() }
        delete_totally_textView.visibility = if (isMyMessage)View.VISIBLE else View.GONE
        delete_totally_textView.setOnClickListener { deleteTotally.value = DELETE_TOTALLY;dismiss() }
    }

    fun observeOnDeleteTotally(lifecycleOwner: LifecycleOwner,onSelect: (Int) -> Unit) = deleteTotally
        .observe(lifecycleOwner, Observer { onSelect(it) })

}