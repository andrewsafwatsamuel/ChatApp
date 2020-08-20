package com.example.chatappfinal.domain.connectyCube.textChat.messaging

import android.os.Bundle
import com.connectycube.chat.exception.ChatException
import com.connectycube.chat.listeners.ChatDialogMessageListener
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.chat.model.ConnectycubeChatMessage
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.domain.connectyCube.InAppMessage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

val defaultMessageListener by lazy { MessageListener() }

class MessageListener(
    private val messageSubject: PublishSubject<ConnectycubeChatMessage> = PublishSubject.create(),
    private val users: HashMap<Int, ConnectycubeUser> = hashMapOf()
) : ChatDialogMessageListener, EntityCallback<ArrayList<ConnectycubeChatMessage>> {
    override fun processMessage(p0: String?, p1: ConnectycubeChatMessage?, p2: Int?) {
        p1?.let { messageSubject.onNext(it) }
    }

    override fun processError(
        p0: String?,
        p1: ChatException?,
        p2: ConnectycubeChatMessage?,
        p3: Int?
    ) = Timber.e(p1?.message.toString())

    override fun onSuccess(messages: ArrayList<ConnectycubeChatMessage>?, p1: Bundle?) = messages
        .takeUnless { it.isNullOrEmpty() }
        ?.forEach { defaultMessageListener.messageSubject.onNext(it) } ?: Unit

    override fun onError(p0: ResponseException?) = Timber.e(p0)

    fun observeOnMessageSubject(
        dialog: ConnectycubeChatDialog,
        messagesMap: HashMap<String, InAppMessage>
    ): Disposable = messageSubject
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe({ it?.onMessageReceived(dialog, messagesMap) }, Timber::e)

    private fun ConnectycubeChatMessage.onMessageReceived(
        dialog: ConnectycubeChatDialog,
        messagesMap: HashMap<String, InAppMessage>
    ) {
        dialog.markAsRead(this)
        messagesMap[id] =
            InAppMessage(getMessageStatus(), this, users[this.senderId]?.login ?: "Sender")
        emitMessages(messagesMap)
    }

    fun setUsers(users: ArrayList<ConnectycubeUser>?) = users
        ?.forEach { this.users[it.id] = it }
}