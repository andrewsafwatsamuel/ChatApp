package com.example.chatappfinal.domain.connectyCube.textChat.messaging

import com.connectycube.chat.listeners.ChatDialogMessageSentListener
import com.connectycube.chat.listeners.MessageStatusListener
import com.connectycube.chat.model.ConnectycubeChatMessage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

//messageStatus
const val SENT = "sent"
const val DELIVERED = "delivered"
const val READ = "read"

val defaultMessageStatusListener by lazy { MessageStatusChangedListener() }

sealed class MessageState(val messageId: String?)
class SentMessage(messageId: String?) : MessageState(messageId)
class MessageError(messageId: String?) : MessageState(messageId)
class DeliveredMessage(messageId: String?) : MessageState(messageId)
class ReadMessage(messageId: String?) : MessageState(messageId)

class MessageStatusChangedListener(
    private val messageStateSubject: PublishSubject<MessageState> = PublishSubject.create()
) : ChatDialogMessageSentListener, MessageStatusListener {

    override fun processMessageSent(p0: String?, message: ConnectycubeChatMessage?) =
        messageStateSubject.onNext(SentMessage(message?.id))

    override fun processMessageFailed(p0: String?, message: ConnectycubeChatMessage?) =
        messageStateSubject.onNext(MessageError(message?.id))

    override fun processMessageDelivered(messageId: String?, dialogId: String?, userId: Int?) =
        messageStateSubject.onNext(DeliveredMessage(messageId))

    override fun processMessageRead(messageId: String?, dialogId: String?, userId: Int?) =
        messageStateSubject.onNext(ReadMessage(messageId))

    fun observeOnStatesSubject(
        messagesMap: HashMap<String, InAppMessage>,
        disposable: CompositeDisposable
    ): Disposable = defaultMessageStatusListener.messageStateSubject
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe({ mapMessageStates(it, messagesMap) }, Timber::e)
        .also { disposable.add(it) }

    private fun mapMessageStates(
        state: MessageState,
        messagesMap: HashMap<String, InAppMessage>
    ) = when (state) {
        is SentMessage -> state.emmitMessageState(SENT, messagesMap)
        is MessageError -> state.emmitMessageState(SENT, messagesMap)
        is DeliveredMessage -> state.emmitMessageState(DELIVERED, messagesMap)
        is ReadMessage -> state.emmitMessageState(READ, messagesMap)
    }

    private fun MessageState.emmitMessageState(
        messageStatus: String,
        messagesMap: HashMap<String, InAppMessage>
    ) {
        messagesMap.updateMessageStatus(messageId?:"",messageStatus)
        emitMessages(messagesMap)
    }

    private fun HashMap<String, InAppMessage>.updateMessageStatus(messageId: String, status: String) =
        if (this[messageId] != null) this[messageId] = InAppMessage(status, get(messageId)!!.connectyCubeMessage)
        else Unit
}

//TODO needs to be moved to separate file
data class InAppMessage(
    val status: String,
    val connectyCubeMessage: ConnectycubeChatMessage
)

fun ConnectycubeChatMessage.getMessageStatus(): String = when {
    deliveredIds?.size ?: 0 < 2 && readIds?.size ?: 0 < 2 -> SENT
    deliveredIds?.size ?: 0 > 1 && readIds?.size ?: 0 < 2 -> DELIVERED
    deliveredIds?.size ?: 0 > 1 && readIds?.size ?: 0 > 1 -> READ
    else -> "unknown"
}
