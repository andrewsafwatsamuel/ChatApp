package com.example.chatappfinal.domain.connectyCube.textChat.messaging

import com.connectycube.chat.exception.ChatException
import com.connectycube.chat.listeners.SystemMessageListener
import com.connectycube.chat.model.ConnectycubeChatMessage
import com.example.chatappfinal.domain.connectyCube.chatInstance
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

internal val systemMessageManager by lazy { chatInstance.systemMessagesManager }

val systemMessageListener by lazy { SystemMessageListener() }

class SystemMessageListener(
    private val systemMessageSubject: PublishSubject<ConnectycubeChatMessage> = PublishSubject.create()
) : SystemMessageListener {
    override fun processMessage(p0: ConnectycubeChatMessage?) {
        p0?.let { systemMessageSubject.onNext(it) }
        Timber.i("System message received ${p0?.body}")
    }

    override fun processError(p0: ChatException?, p1: ConnectycubeChatMessage?) = Timber.e(p0)

    fun onSystemMessageReceived(
        disposables: CompositeDisposable,
        onSuccess: (ConnectycubeChatMessage) -> Unit
    ) = systemMessageSubject.subscribeOn(AndroidSchedulers.mainThread())
        .subscribe({ onSuccess(it) }, Timber::e)
        .let(disposables::add)
}

fun addSystemMessageListener(
    listener: SystemMessageListener = systemMessageListener
) = systemMessageManager.addSystemMessageListener(listener)

fun removeSystemMessageListener(
    listener: SystemMessageListener = systemMessageListener
) = systemMessageManager.removeSystemMessageListener(listener)