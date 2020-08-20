package com.example.chatappfinal.presentation.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.example.chatappfinal.domain.DefaultDialogsRepository
import com.example.chatappfinal.domain.connectyCube.InAppMessage
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.setIsTyping
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.setNotTyping
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.MessageStatusChangedListener
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.*
import com.example.chatappfinal.domain.dataSources.databaseGateway.PushObjectDao
import com.example.chatappfinal.domain.dataSources.databaseGateway.chatDatabase
import com.example.chatappfinal.domain.dialogsRepository
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

class ChatViewModel(
    val dialog: ConnectycubeChatDialog,
    messagesMap: HashMap<String, InAppMessage> = hashMapOf(),
    val disposables: CompositeDisposable = CompositeDisposable(),
    val sender: MessageSender = MessageSender(),
    messageOperationsListener: MessageOperationListener = operationsListener,
    val messageListener: MessageListener = defaultMessageListener,
    messageStatusListener: MessageStatusChangedListener = defaultMessageStatusListener,
    private val dao: PushObjectDao = chatDatabase.pushObjectDao,
    private var disposable: Disposable? = null,
    val isTypingSubject: PublishSubject<Boolean> = PublishSubject.create()
) : ViewModel() {

    init {
        messageOperationsListener.messagesMap = messagesMap
        messageStatusListener.observeOnStatesSubject(messagesMap, disposables)
        messageListener.observeOnMessageSubject(dialog, messagesMap)
        observeOnTyping()
        deleteOpenedNotifications()
    }

    private fun deleteOpenedNotifications() {
        disposable = dao.removeOpenedMessages(dialog.dialogId)
            .subscribeOn(Schedulers.io())
            .subscribe({}, Throwable::printStackTrace)
    }

    private fun observeOnTyping() = isTypingSubject
        .subscribeOn(AndroidSchedulers.mainThread())
        .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        .subscribe(
            { if (it == true) dialog.setIsTyping() else dialog.setNotTyping() },
            Throwable::printStackTrace
        )
        .also { disposables.add(it) }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
        disposable?.dispose()
    }
}

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory(private val dialog: ConnectycubeChatDialog) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) ChatViewModel(dialog) as T
        else throw IllegalStateException("Un known model class")
}