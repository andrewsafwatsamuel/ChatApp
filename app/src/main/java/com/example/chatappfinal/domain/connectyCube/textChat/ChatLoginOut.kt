package com.example.chatappfinal.domain.connectyCube.textChat

import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.domain.connectyCube.*
import com.example.chatappfinal.domain.connectyCube.chatInstance
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.markAsInActive
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.removeSystemMessageListener
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.addSystemMessageListener
import java.lang.Exception

fun chatLogin(
    user: ConnectycubeUser? = getUserFromPreference(),
    chatService: ConnectycubeChatService = chatInstance,
    onComplete: (Exception?) -> Unit
) = user?.apply { this.id = id; this.password = password }
    ?.let {
        //for message status sent, delivered and read
        if (!isLoggedIn()) {
            chatService.setUseStreamManagement(true)
            chatService.login(it, createEntityCallbacks<Unit>({ initOnLogin();onComplete(null) }, onComplete))
        } else onComplete(null)
    }

fun chatLogout(onComplete: (Exception?) -> Unit) = chatInstance
    .logout(createEntityCallbacks({ onComplete(null) }, onComplete))

fun initOnLogin() {
    initializeRtc()
    addRosterListener()
    addSystemMessageListener()
    addPrivacyListener()
    markAsInActive()
}

fun disposeOnQuit() {
    disposeRtc()
    removeRosterListener()
    removeSystemMessageListener()
    removePrivacyListListener()
}

fun isLoggedIn() = ConnectycubeChatService.getInstance().isLoggedIn