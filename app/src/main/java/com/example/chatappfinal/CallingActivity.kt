package com.example.chatappfinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.example.chatappfinal.domain.connectyCube.rtc.SessionClosed
import com.example.chatappfinal.domain.connectyCube.rtc.StartToClose
import com.example.chatappfinal.domain.connectyCube.rtc.sessionCallbacks

class CallingActivity : AppCompatActivity() {

    val recipientId:Int by lazy { intent.getIntExtra("recepientId",0) }
    val flag:String? by lazy { intent.getStringExtra("flag") }
    val dialog: ConnectycubeChatDialog by lazy { intent.getSerializableExtra("dialog") as ConnectycubeChatDialog }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)
        sessionCallbacks.observeOnSession { if (it is StartToClose) finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionCallbacks.session?.hangUp(sessionCallbacks.session?.userInfo)
    }
}