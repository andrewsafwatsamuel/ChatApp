package com.example.chatappfinal.domain.connectyCube.textChat.messaging

import com.connectycube.chat.ConnectycubeRestChatService
import com.connectycube.chat.listeners.MessageUpdateListener
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.chat.model.ConnectycubeChatMessage
import com.connectycube.core.EntityCallback
import com.example.chatappfinal.domain.connectyCube.getUserFromPreference
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.doInDialog
import com.example.chatappfinal.domain.connectyCube.textChat.updateDialog
import timber.log.Timber

//pin/unpin message
fun pinMessages(
    dialog: ConnectycubeChatDialog,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    vararg messageIds: String
) = updateDialog(
    dialog,
    callBacks
) { addPinnedMessagesIds(*messageIds) }

fun unpinMessages(
    dialog: ConnectycubeChatDialog,
    callBacks: EntityCallback<ConnectycubeChatDialog>,
    vararg messageIds: String
) = updateDialog(
    dialog,
    callBacks
) { removePinnedMessagesIds(*messageIds) }

//mark message as read
fun ConnectycubeChatDialog.markAsRead(message: ConnectycubeChatMessage) {
    if (message.senderId != getUserFromPreference()?.id) doInDialog { readMessage(message) }
}

//delete messages
fun deleteMessageTotally(ids: List<String>) =
    deleteMessages(ids.toSet(), true)

fun deleteMessageSingleSide(ids: List<String>) =
    deleteMessages(ids.toSet(), false)

fun deleteMessages(
    ids: Set<String>,
    forceDelete: Boolean,
    callBacks: EntityCallback<ArrayList<String>> = operationsListener
) = ConnectycubeRestChatService.deleteMessages(ids, forceDelete).performAsync(callBacks)

//imageUrl
fun ConnectycubeChatMessage.getAttachmentUrl() = attachments
    ?.toList()
    ?.takeIf { it.isNotEmpty() }
    ?.run { get(0).url } ?: ""

//editMessage
fun editMessage(
    messageId: String,
    body: String,
    isLast: Boolean,
    dialog: ConnectycubeChatDialog,
    updateListener: MessageUpdateListener = operationsListener
) = dialog.doInDialog {
    editMessageWithId(
        messageId,
        body,
        isLast,
        createEntityCallbacks ({ updateListener.processMessageUpdated(messageId,dialogId,body,isLast) }, Timber::e )
    )
}