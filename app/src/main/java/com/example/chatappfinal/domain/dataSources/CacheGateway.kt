package com.example.chatappfinal.domain.dataSources

import com.connectycube.chat.model.ConnectycubeChatDialog
import com.connectycube.users.model.ConnectycubeUser

const val CONTACTS = "contacts"
const val DIALOGS = "dialogs"

private val cache by lazy { hashMapOf<String, Any?>() }

fun addToCache(key: String, value: Any?) {
    cache[key] = value
}

@Suppress("UNCHECKED_CAST")
fun <T> getFromCache(key: String) = cache[key] as T

val runtimeCache by lazy { RuntimeCache() }

class RuntimeCache {
    fun addContactsToCache(contacts: ArrayList<ConnectycubeUser>?) = addToCache(CONTACTS, contacts)
    fun getContactsFromCache() =
        getFromCache<ArrayList<ConnectycubeUser>?>(CONTACTS) ?: arrayListOf()

    fun addDialogsToCache(dialogs: HashMap<String, ConnectycubeChatDialog>?) =
        addToCache(DIALOGS, dialogs)

    fun getDialogsFromCache(): ArrayList<ConnectycubeChatDialog> =
        arrayListOf<ConnectycubeChatDialog>()
            .apply { addAll(getFromCache<HashMap<String, ConnectycubeChatDialog>>(DIALOGS).values) }

    fun getDialogById(id: String) =
        getFromCache<HashMap<String, ConnectycubeChatDialog>>(DIALOGS)[id]

    fun updateDialog(dialog: ConnectycubeChatDialog?)=dialog?.let {
        getFromCache<HashMap<String, ConnectycubeChatDialog>?>(DIALOGS)!![it.dialogId] = it
    }

}

