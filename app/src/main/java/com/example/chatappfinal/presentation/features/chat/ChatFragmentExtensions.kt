package com.example.chatappfinal.presentation.features.chat

import com.connectycube.chat.model.ConnectycubeChatMessage
import com.example.chatappfinal.domain.connectyCube.getUserFromPreference
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.deleteMessageSingleSide
import com.example.chatappfinal.domain.connectyCube.textChat.messaging.deleteMessageTotally
import com.example.chatappfinal.presentation.onClicked
import kotlinx.android.synthetic.main.content_conversation.*

fun ChatFragment.onItemsSelected(messages: List<ConnectycubeChatMessage>) {
    setEdit(messages)
    setDelete(messages.size)
    deleteItem().onClicked {
        deleteDialog.setState(isMyMessage(messages));childFragmentManager.let {
        deleteDialog.show(it, "fragment") }
        onDeleteClicked(messages)
    }
    editItem().onClicked { et_message.setText(messages[0].body) }
}

private fun ChatFragment.setEdit(messages: List<ConnectycubeChatMessage>) {
    editItem().isVisible = isMyMessage(messages) && messages.size==1
}

private fun isMyMessage(messages: List<ConnectycubeChatMessage>) :Boolean{

    for (id in messages.map { it.senderId }){
        if (id!=getUserFromPreference()?.id) return false
    }

    return true
}

private fun ChatFragment.setDelete(selectionCount: Int) {
    deleteItem().isVisible = selectionCount > 0
}

private fun ChatFragment.onDeleteClicked(messages: List<ConnectycubeChatMessage>) = deleteDialog
    .observeOnDeleteTotally(viewLifecycleOwner) {
        when (it) {
            1 -> deleteMessageSingleSide(messages.map { m -> m.id })
            2 -> deleteMessageTotally(messages.map { m -> m.id })
            else -> Unit
        }
    }