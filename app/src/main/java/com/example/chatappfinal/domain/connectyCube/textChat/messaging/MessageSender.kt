package com.example.chatappfinal.domain.connectyCube.textChat.messaging

import com.connectycube.chat.listeners.ChatDialogMessageListener
import com.connectycube.chat.model.ConnectycubeAttachment
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.chat.model.ConnectycubeChatMessage
import com.connectycube.core.ConnectycubeProgressCallback
import com.connectycube.core.EntityCallback
import com.connectycube.storage.model.ConnectycubeFile
import com.example.chatappfinal.domain.connectyCube.getUserFromPreference
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import timber.log.Timber
import java.io.File

@Suppress("ComplexRedundantLet")
class MessageSender(private var attachment: ConnectycubeAttachment? = null) {

    fun sendMessage(
        body: String,
        dialog: ConnectycubeChatDialog,
        attachment: ConnectycubeAttachment? = this.attachment
    ) = if (attachment != null) sendMessageWithAttachment(body, dialog, attachment)
    else senMessageNoAttachment(body, dialog)


    private fun senMessageNoAttachment(
        body: String,
        dialog: ConnectycubeChatDialog,
        message: ConnectycubeChatMessage = composeMessage(body)
    ) = message.takeIf { body.isNotEmpty() }?.also { dialog.send(it) }

    private fun sendMessageWithAttachment(
        body: String,
        dialog: ConnectycubeChatDialog,
        attachment: ConnectycubeAttachment,
        message: ConnectycubeChatMessage = composeMessage(body)
    ) = message.apply { addAttachment(attachment) }
        .also { dialog.send(it) }
        .also { this.attachment = null }

    private fun ConnectycubeChatDialog.send(message: ConnectycubeChatMessage) = message
        .apply {
            val userId = getUserFromPreference()?.id
            message.senderId = userId
            message.dialogId = dialogId
            message.deliveredIds = listOf(userId)
            message.readIds = listOf(userId)
        }.let { sendMessage(it, createEntityCallbacks({ message.onSent() }, {Timber.e("Error while sending message caused by ${it?.message}")})) }

    private fun ConnectycubeChatMessage.onSent(
        chatDialogListener: ChatDialogMessageListener = defaultMessageListener
    ) = chatDialogListener.processMessage(id, this, senderId)

    private fun composeMessage(
        messageBody: String,
        message: ConnectycubeChatMessage = ConnectycubeChatMessage()
    ) = message.apply {
        dateSent = System.currentTimeMillis()
        body = messageBody
        setSaveToHistory(true)
        isMarkable = true
    }

    fun attachPhoto(
        pathName: String,
        progressCallback: ConnectycubeProgressCallback
    ) = attachPhoto(pathName, progressCallback){ attachment = it }

}