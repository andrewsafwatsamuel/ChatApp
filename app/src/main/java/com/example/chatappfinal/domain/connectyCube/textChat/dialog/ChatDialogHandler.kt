package com.example.chatappfinal.domain.connectyCube.textChat.dialog

import androidx.lifecycle.*
import com.connectycube.chat.ConnectycubeChatService
import com.connectycube.chat.ConnectycubeRestChatService
import com.connectycube.chat.MessageStatusesManager
import com.connectycube.chat.listeners.*
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.chat.model.ConnectycubeChatMessage
import com.connectycube.chat.request.MessageGetBuilder
import com.connectycube.core.EntityCallback
import com.example.chatappfinal.domain.connectyCube.chatInstance
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.defaultMessageListener
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.defaultMessageStatusListener
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.operationsListener
import timber.log.Timber

class ChatDialogHandler(
    private val dialog: ConnectycubeChatDialog,
    lifecycleOwner: LifecycleOwner,
    private val callBacks: EntityCallback<ArrayList<ConnectycubeChatMessage>>
) : LifecycleEventObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = when (event) {
        Lifecycle.Event.ON_RESUME -> doOnResume()
        Lifecycle.Event.ON_DESTROY -> disposeChatListeners()
        else -> Unit
    }

    private fun doOnResume() {
        initialize()
        getMessages(dialog.dialogId, callBacks)
    }

    private fun initialize() = with(dialog) {
        initChat()
        initChatListeners()
    }

    private fun ConnectycubeChatDialog.initChat(
        chatService: ConnectycubeChatService = ConnectycubeChatService.getInstance()
    ) = initForChat(chatService)

    private fun ConnectycubeChatDialog.initChatListeners(
        listener: ChatDialogMessageListener = defaultMessageListener,
        sentListener: ChatDialogMessageSentListener = defaultMessageStatusListener,
        isTypingListener: ChatDialogTypingListener = dialogTypingListener,
        updateListener: MessageUpdateListener = operationsListener,
        deleteListener: MessageDeleteListener = operationsListener,
        statusListener: MessageStatusListener = defaultMessageStatusListener,
        messageStatusManager: MessageStatusesManager = chatInstance.messageStatusesManager
    ) {
        addMessageListener(listener)
        addMessageSentListener(sentListener)
        addIsTypingListener(isTypingListener)
        messageStatusManager.addMessageUpdateListener(updateListener)
        messageStatusManager.addMessageDeleteListener(deleteListener)
        messageStatusManager.addMessageStatusListener(statusListener)
        markAsActive()
    }

    private fun disposeChatListeners(
        listener: ChatDialogMessageListener = defaultMessageListener,
        sentListener: ChatDialogMessageSentListener = defaultMessageStatusListener,
        isTypingListener: ChatDialogTypingListener = dialogTypingListener,
        deleteListener: MessageDeleteListener = operationsListener,
        updateListener: MessageUpdateListener = operationsListener,
        statusListener: MessageStatusListener = defaultMessageStatusListener,
        messageStatusManager: MessageStatusesManager = chatInstance.messageStatusesManager
    ) = with(dialog) {
        markAsInActive()
        removeMessageListrener(listener)
        removeMessageSentListener(sentListener)
        removeIsTypingListener(isTypingListener)
        messageStatusManager.removeMessageUpdateListener(updateListener)
        messageStatusManager.removeMessageDeleteListener(deleteListener)
        messageStatusManager.addMessageStatusListener(statusListener)
    }
}

fun ConnectycubeChatDialog.getPinnedMessages(
    result: MutableList<ConnectycubeChatMessage>
) = getMessages(
    dialogId,
    createEntityCallbacks({ result.addAll(filterPinnedMessages(it, pinnedMessagesIds)) }, Timber::e)
)

fun filterPinnedMessages(
    messages: MutableList<ConnectycubeChatMessage>?,
    pinnedMessageIds: List<String>
) = messages?.filter { it.id in pinnedMessageIds } ?: listOf()

fun getMessages(
    id: String,
    callBacks: EntityCallback<ArrayList<ConnectycubeChatMessage>> /*= dialogMessagesCallbacks*/,
    builder: MessageGetBuilder = MessageGetBuilder()/*.apply { limit = 100 }*/,
    chatDialog: ConnectycubeChatDialog = ConnectycubeChatDialog(id)
) = ConnectycubeRestChatService.getDialogMessages(chatDialog, builder)
    .performAsync(callBacks)