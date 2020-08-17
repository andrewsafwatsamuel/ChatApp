package com.example.chatappfinal.domain.connectyCube.textChat.dialog

import com.connectycube.chat.listeners.ChatDialogTypingListener
import com.connectycube.users.ConnectycubeUsers
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

val dialogTypingListener by lazy { DialogTypingListener() }

class DialogTypingListener(
    private val loginSubject: PublishSubject<String> = PublishSubject.create()
) : ChatDialogTypingListener {

    override fun processUserIsTyping(dialogId: String?, senderId: Int?) = ConnectycubeUsers
        .getUser(senderId ?: 0)
        .performAsync(
            createEntityCallbacks(
            { loginSubject.onNext(it?.login?:"") })
        )

    override fun processUserStopTyping(p0: String?, p1: Int?) = loginSubject.onNext("")

    fun listenOnTypingChange(disposable:CompositeDisposable,login:(String)->Unit) = loginSubject
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe({login(it)},Timber::e)
        .let { disposable.add(it) }
}