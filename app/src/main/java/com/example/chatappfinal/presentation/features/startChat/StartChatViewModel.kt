package com.example.chatappfinal.presentation.features.startChat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.domain.*
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.createPrivateDialog
import com.example.chatappfinal.presentation.UNKNOWN_ERROR

sealed class StartChatState
object LoadingState : StartChatState()
object EmptyContactState : StartChatState()
class SuccessContactsState(val Contacts: ArrayList<ConnectycubeUser>) : StartChatState()
class ErrorContactsState(val message: String) : StartChatState()
class SuccessChatState(val dialog: ConnectycubeChatDialog?) : StartChatState()
class ErrorChatState(val message: String) : StartChatState()

class StartChatViewModel(
    dialogs: ArrayList<ConnectycubeChatDialog> = dialogsRepository.retrieveData(),
    private val contacts: RetrieveCacheRepository<ConnectycubeUser> = contactsRepository,
    val stateLiveData: MutableLiveData<StartChatState> = MutableLiveData()
) : ViewModel() {

    private val dialogsMap by lazy { hashMapOf<Int, ConnectycubeChatDialog>() }

    init {
        dialogs.forEach { dialogsMap[it.recipientId] = it }
        getContacts()
    }

    fun getContacts() = load { retrieveContacts() }

    private fun load(retrieve: () -> Unit) {
        if (stateLiveData.value is LoadingState) return
        stateLiveData.value = LoadingState
        retrieve()
    }

    private fun retrieveContacts() = contacts.run {
        if (retrieveData().isEmpty()) updateData { onFinishedContacts(it) }
        else stateLiveData.value = SuccessContactsState(contactsRepository.retrieveData())
    }

    private fun onFinishedContacts(message: String?) = if (message == null) {
        onContactsRetrieved(contacts.retrieveData())
    } else {
        stateLiveData.value = ErrorContactsState(message)
    }

    private fun onContactsRetrieved(contacts: ArrayList<ConnectycubeUser>) {
        stateLiveData.value = if (contacts.isEmpty()) EmptyContactState else SuccessContactsState(contacts)
    }

    fun startChat(userId: Int) = load { startPrivateChat(userId) }

    private fun startPrivateChat(userId: Int) = if (userId in dialogsMap.keys) {
        stateLiveData.value = SuccessChatState(dialogsMap[userId])
    } else {
        createChat(userId)
    }

    private fun createChat(userId: Int) = createPrivateDialog(userId, createEntityCallbacks(
        { stateLiveData.value = SuccessChatState(it) },
        { stateLiveData.value = ErrorChatState(it?.message ?: UNKNOWN_ERROR) }
    ))
}