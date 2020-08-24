package com.example.chatappfinal.presentation.features.receiveCall

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.connectycube.videochat.RTCSession
import com.connectycube.videochat.RTCTypes
import com.example.chatappfinal.R
import com.example.chatappfinal.domain.connectyCube.getUser
import com.example.chatappfinal.domain.connectyCube.rtc.NewCall
import com.example.chatappfinal.domain.connectyCube.rtc.SessionState
import com.example.chatappfinal.domain.connectyCube.rtc.StartToClose
import com.example.chatappfinal.domain.connectyCube.rtc.sessionCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.presentation.features.CallingActivity
import com.example.chatappfinal.presentation.features.chat.AUDIO_CALL
import com.example.chatappfinal.presentation.features.chat.VIDEO_CALL
import com.example.chatappfinal.presentation.hide
import com.example.chatappfinal.presentation.loadPhoto
import kotlinx.android.synthetic.main.activity_receive_call.*
import kotlinx.android.synthetic.main.activity_receive_call.call_type_textView
import kotlinx.android.synthetic.main.activity_receive_call.recipient_imageView
import kotlinx.android.synthetic.main.activity_receive_call.recipient_name_textView
import kotlinx.android.synthetic.main.activity_receive_call.stop_call_imageView

class ReceiveCallActivity : AppCompatActivity() {
    private var session: RTCSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_call)
        sessionCallbacks.observeOnSession { observeOnCallbacks(it) }
    }

    private fun observeOnCallbacks(state: SessionState) = when (state) {
        is NewCall -> {
            session = state.session
            retrieveUser()
        }
        is StartToClose -> finish()
        else -> Unit
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveUser() = getUser(session?.callerID ?: 0, createEntityCallbacks({ user ->
        receive_call_progressBar.hide()
//        recipient_imageView.loadPhoto(user?.avatar)
        recipient_name_textView.text = "Call from ${user?.login}"
        call_type_textView.text = setCallType()
        stop_call_imageView.setOnClickListener { rejectCall() }
        accept_call_imageView.setOnClickListener { acceptCall() }
    }, {}))

    private fun setCallType() = if (
        session?.conferenceType == RTCTypes.ConferenceType.CONFERENCE_TYPE_AUDIO
    ) AUDIO_CALL else VIDEO_CALL

    private fun rejectCall() {
        session?.rejectCall(session?.userInfo)
    }

    private fun acceptCall() {
        session?.acceptCall(session?.userInfo)
        navigateToCall()
    }

    private fun navigateToCall() = startActivity(Intent(this, CallingActivity::class.java))
        .also { finish() }
}
