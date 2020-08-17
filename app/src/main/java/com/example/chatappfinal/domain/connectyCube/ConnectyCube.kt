@file:Suppress("DEPRECATION")

package com.example.chatappfinal.domain.connectyCube

import android.content.Context
import com.connectycube.auth.session.ConnectycubeSettings
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.EntityCallback
import com.connectycube.users.ConnectycubeUsers
import com.connectycube.users.model.ConnectycubeUser
import com.connectycube.videochat.RTCClient
import com.connectycube.videochat.RTCMediaConfig
import java.util.ArrayList

private const val APP_ID = "2800"
private const val AUTH_KEY = "KRwqhgHm9t4B8sF"
private const val AUTH_SECRET = "SkAqgYAVNaBt-8h"
private const val ACCOUNT_KEY = "EVsZMSsaEWPusADgpmyz"

val rtcClient: RTCClient by lazy { RTCClient.getInstance(ConnectyCube.appContext) }

internal val chatService by lazy {
    ConnectycubeChatService.ConfigurationBuilder().apply {
        socketTimeout = 60
        isKeepAlive = true
        isUseTls = true
    }
}

internal val chatInstance: ConnectycubeChatService by lazy {
    ConnectycubeChatService.setConfigurationBuilder(chatService)
    ConnectycubeChatService.getInstance().apply { isReconnectionAllowed = true }
}

object ConnectyCube {

    lateinit var appContext: Context
        private set

    fun initialize(context: Context) {
        appContext = context
        ConnectycubeSettings.getInstance()
            .init(context, APP_ID, AUTH_KEY, AUTH_SECRET)
            .apply { accountKey = ACCOUNT_KEY }
        RTCMediaConfig.setVideoHeight(1280)
        RTCMediaConfig.setVideoHeight(720)
    }

}

fun getUser(id: Int, callbacks: EntityCallback<ConnectycubeUser>) =
    ConnectycubeUsers.getUser(id).performAsync(callbacks)

fun toInApp(dialogs:List<ConnectycubeChatDialog>) = ArrayList<InAppDialog>()
    .apply { dialogs.forEach { add(InAppDialog(it)) } }