package com.example.chatappfinal.presentation.features.dialogs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.example.chatappfinal.domain.RetrieveCacheRepository
import com.example.chatappfinal.domain.connectyCube.textChat.chatLogin
import com.example.chatappfinal.domain.contactsRepository
import com.example.chatappfinal.domain.dialogsRepository
import com.example.chatappfinal.presentation.UNKNOWN_ERROR

sealed class DialogState
object DialogsLoading : DialogState()
class DialogsSuccess(val dialogs: ArrayList<ConnectycubeChatDialog>) : DialogState()
class DialogsError(val message: String) : DialogState()

class DialogsViewModel(
    val dialogStates: MutableLiveData<DialogState> = MutableLiveData(),
    private val repository: RetrieveCacheRepository<ConnectycubeChatDialog> = dialogsRepository
) : ViewModel() {

    init {
        login()
    }

    fun login() {
        dialogStates.value = DialogsLoading
        chatLogin { onFinishedLogin(it) }
    }

    private fun onFinishedLogin(exception: Exception?) = if (exception == null) {
        contactsRepository.retrieveData()
        retrieveDialogs()
    } else {
        dialogStates.value = DialogsError(exception.message ?: UNKNOWN_ERROR)
    }

    fun retrieveDialogs() = repository.updateData {
        dialogStates.value = if (it == null) DialogsSuccess(repository.retrieveData())
        else DialogsError(it)
    }


}