package com.example.chatappfinal.presentation.features.startCall

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.helper.StringifyArrayList
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.pushNotifications.CALL
import com.example.chatappfinal.domain.connectyCube.pushNotifications.composeChatNotification
import com.example.chatappfinal.domain.connectyCube.pushNotifications.createEvent
import com.example.chatappfinal.domain.connectyCube.rtc.StartToClose
import com.example.chatappfinal.domain.connectyCube.rtc.sessionCallbacks
import com.example.chatappfinal.presentation.enable
import com.example.chatappfinal.presentation.features.CallingActivity
import com.example.chatappfinal.presentation.hide
import com.example.chatappfinal.presentation.show
import kotlinx.android.synthetic.main.activity_start_call.*

class StartCallActivity : AppCompatActivity() {

    private val recipientId by lazy { intent.getIntExtra("recepientId", 0) }
    private val flag by lazy { intent.getStringExtra("flag") }
    private val dialog by lazy { intent.getSerializableExtra("dialog") as ConnectycubeChatDialog }


    private val viewModel by lazy {
        StartCallViewModelFactory(recipientId, flag ?: "").let {
            ViewModelProvider(
                this,
                it
            )[StartCallViewModel::class.java]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_call)
        viewModel.stateLiveData.observe(this, Observer { it?.let { drawStates(it) } })
        createEvent(
            StringifyArrayList<Int>().apply { add(recipientId) },
            composeChatNotification(CALL, dialog.dialogId, dialog.name)
        )
        stop_call_imageView.setOnClickListener { endCall() }
        sessionCallbacks.observeOnSession { if (it is StartToClose) finish() }
    }


    private fun drawStates(state: StartCallState) = when (state) {
        is LoadingState -> onLoading()
        is SuccessUserState -> successUser(state.user)
        is ErrorState -> endCall()
        is SuccessCall -> successCall()
    }

    private fun onLoading() {
        start_call_progressBar.show()
    }

    private fun endCall() {
        viewModel.session?.hangUp(viewModel.session?.userInfo)
    }

    private fun successCall() = Intent(this, CallingActivity::class.java).let {
        startActivity(it)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun successUser(user: ConnectycubeUser?) = user?.run {
        start_call_progressBar.hide()
        stop_call_imageView.enable()
        Glide.with(this@StartCallActivity)
            .load(avatar)
            .placeholder(R.drawable.ic_person)
            .into(recipient_imageView)
        recipient_name_textView.text = "Calling $login"
        call_type_textView.text = flag
    }

}
