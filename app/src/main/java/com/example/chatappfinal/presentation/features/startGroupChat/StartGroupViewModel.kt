package com.example.chatappfinal.presentation.features.startGroupChat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.EntityCallback
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.createGroupDialog
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.createPublicDialog
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.subscribeToNonPrivateDialog
import com.example.chatappfinal.presentation.UNKNOWN_ERROR

sealed class StartGroupState
object LoadingState : StartGroupState()
class SuccessState(val dialog: ConnectycubeChatDialog?) : StartGroupState()
class ErrorState(val message: String) : StartGroupState()

class StartGroupViewModel(
    val stateLiveData: MutableLiveData<StartGroupState> = MutableLiveData()
) : ViewModel() {

    fun joinChat(dialogId: String) {
        if (stateLiveData.value is LoadingState) return
        if (dialogId.isBlank()) {
            stateLiveData.value = ErrorState("Please Type Dialog Id");return
        }
        stateLiveData.value = LoadingState
        join(dialogId)
    }

    private fun join(dialogId: String) = subscribeToNonPrivateDialog(dialogId, createEntityCallbacks(
        { stateLiveData.value = SuccessState(it) },
        { stateLiveData.value = ErrorState(it?.message ?: UNKNOWN_ERROR) }
    ))

    fun starDialog(isPublic: Boolean, name: String, occupants: List<Int>) {
        if (stateLiveData.value is LoadingState) return
        if (name.isBlank()) {
            stateLiveData.value = ErrorState("Group Must Have Name");return
        }
        if (occupants.isEmpty()) {
            stateLiveData.value = ErrorState("Please Add Members");return
        }
        stateLiveData.value = LoadingState
        createDialog(isPublic, name, occupants)
    }

    private fun createDialog(
        isPublic: Boolean,
        name: String,
        occupants: List<Int>,
        callback: EntityCallback<ConnectycubeChatDialog> = createEntityCallbacks(
            { stateLiveData.value = SuccessState(it) },
            { stateLiveData.value = ErrorState(it?.message ?: UNKNOWN_ERROR) })
    ) = if (isPublic) createPublicDialog(name, occupants, callback)
    else createGroupDialog(name, occupants, callback)

}