package com.example.chatappfinal.domain.connectyCube.textChat.messaging

import com.example.chatappfinal.domain.connectyCube.InAppMessage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

private val messageUpdateSubject: PublishSubject<List<InAppMessage>> = PublishSubject.create()

internal fun emitMessages(
    messagesMap: HashMap<String, InAppMessage>,
    updateSubject: PublishSubject<List<InAppMessage>> = messageUpdateSubject
) = messagesMap
    .values
    .sortedBy { it.connectyCubeMessage.dateSent }
    .toList()
    .let { updateSubject.onNext(it) }

fun updateMessages(
    disposables: CompositeDisposable,
    doOnSubscribe: (List<InAppMessage>) -> Unit
) = messageUpdateSubject
    .debounce(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
    .subscribeOn(AndroidSchedulers.mainThread())
    .subscribe({ it?.let { doOnSubscribe(it) }
        Timber.i("messages are $it")
    },{Timber.e(it)})
    .let { disposables.add(it) }