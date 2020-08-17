package com.example.chatappfinal.domain.connectyCube.textChat.messaging

import android.os.Bundle
import com.connectycube.chat.listeners.MessageDeleteListener
import com.connectycube.chat.listeners.MessageUpdateListener
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import timber.log.Timber

val operationsListener by lazy { MessageOperationListener() }

class MessageOperationListener(
    var messagesMap: HashMap<String, InAppMessage>? = null
) : MessageUpdateListener, MessageDeleteListener, EntityCallback<ArrayList<String>> {

    override fun processMessageUpdated(
        msgId: String?,
        dialogIg: String?,
        newBody: String?,
        p3: Boolean
    ) {
        messagesMap?.editBody(msgId ?: "", newBody ?: "")
    }


    private fun HashMap<String, InAppMessage>.editBody(msgId: String, newBody: String) =
        if (this[msgId] != null) {
            this[msgId] = get(msgId)!!.apply { connectyCubeMessage.body = newBody }
            emitMessages(this)
        } else Unit

    override fun processMessageDeleted(msgId: String?, dialogIg: String?) {
        //emits list multiple times
        messagesMap?.run { remove(msgId);emitMessages(this) }
    }

    //delete
    override fun onSuccess(messageIds: ArrayList<String>?, p1: Bundle?) = messageIds
        ?.takeUnless { it.isNullOrEmpty() }
        ?.forEach { messagesMap?.remove(it) }
        ?.let { messagesMap?.let { emitMessages(it) } } ?: Unit

    override fun onError(p0: ResponseException?) = Timber.e(p0)
}