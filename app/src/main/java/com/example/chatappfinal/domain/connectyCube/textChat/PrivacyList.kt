package com.example.chatappfinal.domain.connectyCube.textChat

import com.connectycube.chat.PrivacyListsManager
import com.connectycube.chat.listeners.PrivacyListListener
import com.connectycube.chat.model.ConnectycubePrivacyList
import com.connectycube.chat.model.ConnectycubePrivacyListItem
import com.example.chatappfinal.domain.connectyCube.chatInstance
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import timber.log.Timber


val privacyListsManager: PrivacyListsManager = chatInstance.privacyListsManager


fun makePrivacyList(name: String) = ConnectycubePrivacyList()
    .apply { this.name = name }

/*
type - use USER_ID to block a user in 1-1 chat or GROUP_USER_ID to block in a group chat.
valueForType - ID of a user to apply an action
allow - can be true/false.
mutualBlock - can be true/false - to block user's message in both directions or not.
* */
fun makePrivacyItem(
    isAllowed: Boolean,
    itemType: ConnectycubePrivacyListItem.Type,
    value: String,
    mutualBlock: Boolean
) = ConnectycubePrivacyListItem().apply {
    isAllow = isAllowed
    type = itemType//ConnectycubePrivacyListItem.Type.USER_ID
    valueForType = value
    isMutualBlock = mutualBlock
}

fun createPrivacyList(
    privacyList: ConnectycubePrivacyList,
    vararg privacyListItem: ConnectycubePrivacyListItem
) {
    privacyList.items = listOf(*privacyListItem)
    doOnPrivacyManager { privacyListsManager.createPrivacyList(privacyList) }
}

fun activatePrivacyList(name: String) = doOnPrivacyManager { applyPrivacyList(name) }

//create new list
fun updateList(
    privacyList: ConnectycubePrivacyList,
    vararg privacyListItem: ConnectycubePrivacyListItem
) {
    privacyListsManager.declinePrivacyList()
    createPrivacyList(privacyList, *privacyListItem)
    activatePrivacyList(privacyList.name)
}

fun retrievePrivacyLists(lists:MutableList<ConnectycubePrivacyList>) =
    doOnPrivacyManager { lists.addAll(privacyLists)}

fun retrievePrivacyList(listName:String) = doOnPrivacyManager { getPrivacyList(listName).also { Timber.i(it.toString()) }  }

fun removePrivacyList(listName: String) =
    doOnPrivacyManager { declinePrivacyList();deletePrivacyList(listName) }

private fun doOnPrivacyManager(
    manager: PrivacyListsManager = privacyListsManager,
    method: PrivacyListsManager.() -> Unit
) = try {
    manager.method()
} catch (e: SmackException.NotConnectedException) {
    e.printStackTrace()
} catch (e: XMPPException.XMPPErrorException) {
    e.printStackTrace()
} catch (e: SmackException.NoResponseException) {
    e.printStackTrace()
}


val privacyListListener by lazy { PrivacyListListener() }

class PrivacyListListener:PrivacyListListener{
    override fun setPrivacyList(p0: String?, p1: MutableList<ConnectycubePrivacyListItem>?) {
        Timber.i("privacy list is $p1")
    }

    override fun updatedPrivacyList(p0: String?) {
        Timber.i("updatedPrivacyList $p0")
    }
}

fun addPrivacyListener(
    manager: PrivacyListsManager = privacyListsManager,
    listener: PrivacyListListener = privacyListListener
) = manager.addPrivacyListListener(listener)

fun removePrivacyListListener(
    manager: PrivacyListsManager = privacyListsManager,
    listener: PrivacyListListener = privacyListListener
) = manager.removePrivacyListListener(listener)