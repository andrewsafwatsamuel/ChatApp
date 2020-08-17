package com.example.chatappfinal.domain

import android.os.Bundle
import com.connectycube.chat.ConnectycubeRoster
import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.core.EntityCallback
import com.connectycube.core.exception.ResponseException
import com.connectycube.users.model.ConnectycubeUser
import com.example.chatappfinal.domain.connectyCube.getUsers
import com.example.chatappfinal.domain.connectyCube.textChat.chatRoster
import com.example.chatappfinal.domain.connectyCube.textChat.dialog.getDialogs
import com.example.chatappfinal.domain.dataSources.RuntimeCache
import com.example.chatappfinal.domain.dataSources.runtimeCache
import com.example.chatappfinal.presentation.UNKNOWN_ERROR

val contactsRepository by lazy { DefaultContactsRepository() }
val dialogsRepository by lazy { DefaultDialogsRepository() }

interface RetrieveCacheRepository<T> {
    fun updateData(onFinish: (String?) -> Unit)
    fun retrieveData(): ArrayList<T>
}

interface DialogRepository {
    fun getDialogById(id: String): ConnectycubeChatDialog?
    fun updateDialog(dialog: ConnectycubeChatDialog)
}

class DefaultDialogsRepository(
    val cache: RuntimeCache = runtimeCache
) : RetrieveCacheRepository<ConnectycubeChatDialog>, DialogRepository {

    override fun updateData(
        onFinish: (String?) -> Unit
    ) = getDialogs(object : EntityCallback<ArrayList<ConnectycubeChatDialog>> {
        override fun onSuccess(dialogs: ArrayList<ConnectycubeChatDialog>?, p1: Bundle?) {
            val dialogsMap = hashMapOf<String, ConnectycubeChatDialog>()
            dialogs?.forEach { dialogsMap[it.dialogId] = it }
            cache.addDialogsToCache(dialogsMap)
            onFinish(null)
        }

        override fun onError(exception: ResponseException?) {
            onFinish(exception?.message ?: UNKNOWN_ERROR)
        }
    })

    override fun retrieveData(): ArrayList<ConnectycubeChatDialog> = cache.getDialogsFromCache()
    override fun getDialogById(id: String): ConnectycubeChatDialog? = cache.getDialogById(id)
    override fun updateDialog(dialog: ConnectycubeChatDialog) = cache.updateDialog(dialog) ?: Unit
}

class DefaultContactsRepository(
    private val cache: RuntimeCache = runtimeCache,
    private val roster: ConnectycubeRoster = chatRoster
) : RetrieveCacheRepository<ConnectycubeUser> {

    val ids by lazy { roster.entries.map { it.userId } }

    override fun updateData(
        onFinish: (String?) -> Unit
    ) = getUsers(ids, object : EntityCallback<ArrayList<ConnectycubeUser>> {
        override fun onSuccess(users: ArrayList<ConnectycubeUser>?, p1: Bundle?) {
            cache.addContactsToCache(users)
            onFinish(null)
        }

        override fun onError(exception: ResponseException?) {
            onFinish(exception?.message ?: UNKNOWN_ERROR)
        }
    })

    override fun retrieveData(): ArrayList<ConnectycubeUser> = cache.getContactsFromCache()

}