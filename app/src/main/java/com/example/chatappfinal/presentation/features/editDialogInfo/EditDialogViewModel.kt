package com.example.chatappfinal.presentation.features.editDialogInfo

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.domain.DialogRepository
import com.example.chatappfinal.domain.connectyCube.getUser
import com.example.chatappfinal.domain.connectyCube.getUserFromPreference
import com.example.chatappfinal.domain.connectyCube.getUsers
import com.example.chatappfinal.domain.connectyCube.textChat.createEntityCallbacks
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.*
import com.example.chatappfinal.domain.dialogsRepository
import com.example.chatappfinal.presentation.UNKNOWN_ERROR
import timber.log.Timber
import java.lang.IllegalStateException

sealed class EditDialogState
object Loading : EditDialogState()
class ErrorState(val message: String) : EditDialogState()
object SuccessInScreenState : EditDialogState()
object SuccessOutScreen : EditDialogState()
class PrivateChatState(val dialog: ConnectycubeChatDialog?) : EditDialogState()

class EditDialogViewModel(
    private val dialogId: String,
    private val repository: DialogRepository = dialogsRepository,
    val stateLiveData: MutableLiveData<EditDialogState> = MutableLiveData(),
    val adminsLiveData: MutableLiveData<MutableList<Int>> = MutableLiveData(),
    val usersLiveData: MutableLiveData<MutableList<ConnectycubeUser>> = MutableLiveData()
) : ViewModel() {


    private val dialogsMap by lazy { hashMapOf<Int, ConnectycubeChatDialog>() }

    init {
        dialogsRepository.retrieveData().forEach { dialogsMap[it.recipientId] = it }
        getDialogUsers()
    }

    fun addToAdmins(id: Int) = load {
        addDialogAdmins(getDialog(), adminsCallbacks(), id)
    }

    fun removeAdmin(id: Int) = load {
        removeDialogAdmins(getDialog(), adminsCallbacks(), id)
    }

    private fun adminsCallbacks() = createEntityCallbacks<ConnectycubeChatDialog>(
        { onAdminsSuccess(it);Timber.i("admin ids ${it?.adminsIds}") },
        { setError(it?.message);Timber.e(it) }
    )

    private fun onAdminsSuccess(dialog: ConnectycubeChatDialog?) {
        adminsLiveData.value = dialog?.adminsIds ?: mutableListOf()
        setInScreenSuccess(dialog)
    }


    fun addUser(id: Int) = load {
        addDialogUsers(
            getDialog(),
            userUpdatesCallBacks { onSuccessfulAdd(it) },
            id
        )
    }

    private fun onSuccessfulAdd(dialog: ConnectycubeChatDialog?) = dialog
        ?.occupants
        ?.filter { usersLiveData.value?.map { user -> user.id }?.contains(it) == false }
        ?.takeUnless { it.isEmpty() }
        ?.get(0)
        ?.let { getAddedUser(it, dialog) }

    private fun getAddedUser(id: Int, dialog: ConnectycubeChatDialog?) = getUser(
        id, createEntityCallbacks(
            { onUserSuccess(it ?: ConnectycubeUser(), dialog) },
            { setError(it?.message) }
        ))

    private fun onUserSuccess(user: ConnectycubeUser, dialog: ConnectycubeChatDialog?) =
        usersLiveData
            .value
            ?.apply { add(user) }
            ?.let { usersLiveData.value = it }
            .also { setInScreenSuccess(dialog) }

    fun removeUser(id: Int) = load {
        removeDialogUsers(
            getDialog(),
            userUpdatesCallBacks { onRemovedSuccess(it) },
            id
        )
    }

    private fun userUpdatesCallBacks(
        onSuccess: (ConnectycubeChatDialog?) -> Unit
    ) = object : EntityCallback<ConnectycubeChatDialog> {
        override fun onSuccess(p0: ConnectycubeChatDialog?, p1: Bundle?) = onSuccess(p0)
        override fun onError(p0: ResponseException?) = setError(p0?.message)
    }

    private fun onRemovedSuccess(dialog: ConnectycubeChatDialog?) = usersLiveData.value
        ?.filter { dialog?.occupants?.contains(it.id) == false }
        ?.takeUnless { it.isEmpty() }
        ?.get(0)
        ?.also {
            if (it.id == getUserFromPreference()?.id) outScreenSuccess() else setInScreenSuccess(
                dialog
            )
        }
        ?.let { usersLiveData.value?.apply { remove(it) } }
        ?.let { usersLiveData.value = it }

    fun clearDialog() = load {
        dialogId.deleteDialog(
            createEntityCallbacks({ outScreenSuccess() }, { setError(it?.message) })
        )
    }

    fun unsubscribe() = load {
        dialogId.removeDialogUser(
            createEntityCallbacks({ outScreenSuccess() }, { setError(it?.message) })
        )
    }

    fun startPrivateChat(userId: Int) = if (userId in dialogsMap.keys) {
        stateLiveData.value = PrivateChatState(dialogsMap[userId])
    } else {
        createChat(userId)
    }

    private fun createChat(userId: Int) = createPrivateDialog(userId, createEntityCallbacks(
        { stateLiveData.value = PrivateChatState(dialogsMap[userId]) },
        { setError(it?.message) }
    ))

    fun updateDialogInfo(params: DialogUpdateParams) = load {
        updateChatInfo(
            params,
            createEntityCallbacks({ outScreenSuccess() }, { setError(it?.message) }),
            getDialog()
        )
    }

    fun getDialogUsers() = load {
        getUsers(
            getDialog().occupants,
            createEntityCallbacks(
                {
                    adminsLiveData.value =
                        getDialog().adminsIds;usersLiveData.value =
                    it;stateLiveData.value = SuccessInScreenState
                },
                { setError(it?.message) }
            ))
    }

    private infix fun setError(message: String?) {
        stateLiveData.value = ErrorState(message ?: UNKNOWN_ERROR)
    }

    private fun load(doWork: () -> Unit) {
        if (stateLiveData.value is Loading) return
        stateLiveData.value = Loading
        doWork()
    }

    private fun setInScreenSuccess(dialog: ConnectycubeChatDialog?) {
        dialog?.let { repository.updateDialog(it) }
        stateLiveData.value = SuccessInScreenState
    }
private fun getDialog()= repository.getDialogById(dialogId) ?: ConnectycubeChatDialog()

    private fun outScreenSuccess() {
        stateLiveData.value = SuccessOutScreen
    }
}

class EditDialogFactory(private val dialogId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(EditDialogViewModel::class.java)) EditDialogViewModel(
            dialogId
        ) as T else throw IllegalStateException("Bad Model Class")
}